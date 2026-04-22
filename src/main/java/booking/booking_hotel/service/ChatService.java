package booking.booking_hotel.service;


import booking.booking_hotel.model.BookedRoom;
import booking.booking_hotel.model.ConversationMessage;
import booking.booking_hotel.model.ChatRequest;
import booking.booking_hotel.model.ChatResponse;
import booking.booking_hotel.model.Room;
import booking.booking_hotel.model.UserPreference;
import booking.booking_hotel.repository.ConversationMessageRepository;
import booking.booking_hotel.repository.UserPreferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final int MAX_HISTORY = 10;

    private final IBookingService bookingService;
    private final IRoomService roomService;
    private final ChatClient chatClient;
    private final UserPreferenceRepository preferenceRepo;
    private final ConversationMessageRepository conversationRepo;
    private volatile long aiQuotaBackoffUntilMs = 0;

    @Value("${chat.ai.enabled:true}")
    private boolean aiEnabled;

    @Value("${chat.ai.quota-backoff-ms:300000}")
    private long quotaBackoffMs;

    public ChatService(IBookingService bookingService,
                       IRoomService roomService,
                       ChatClient.Builder chatClientBuilder,
                       UserPreferenceRepository preferenceRepo,
                       ConversationMessageRepository conversationRepo) {
        this.bookingService = bookingService;
        this.roomService = roomService;
        this.chatClient = chatClientBuilder.build();
        this.preferenceRepo = preferenceRepo;
        this.conversationRepo = conversationRepo;
    }

    public ChatResponse processChat(ChatRequest chatRequest) {
        try {
            String message = chatRequest.getMessage();
            if (message == null || message.isBlank()) {
                return new ChatResponse("Vui lòng nhập nội dung câu hỏi.");
            }

            String userId = normalizeUserId(chatRequest.getUserId());
            String sessionId = normalizeSessionId(chatRequest.getSessionId());

            UserPreference userPref = preferenceRepo.findByUserId(userId)
                    .orElseGet(() -> createNewPreference(userId));

            List<ConversationMessage> history = conversationRepo
                    .findTop10ByUserIdAndSessionIdOrderByCreatedAtAsc(userId, sessionId);

            saveMessage(userId, sessionId, "user", message);

            // Bước 1: Lấy danh sách phòng
            List<Room> rooms = roomService.getAllRooms();
            StringBuilder roomInfo = new StringBuilder("Danh sách phòng hiện tại:\n");
            for (Room room : rooms) {
                if (room != null && room.getId() != null) {
                    roomInfo.append(String.format("Phòng ID: %d, Loại: %s, Giá: %s\n",
                            room.getId(), room.getRoomType(), room.getRoomPrice()));
                }
            }

            // Bước 2: Phân tích lịch đặt phòng
            List<BookedRoom> bookings = bookingService.getAllBookings();
            Map<LocalDate, List<String>> bookingByDate = new HashMap<>();
            for (BookedRoom booking : bookings) {
                if (booking != null && booking.getRoom() != null && booking.getCheckInDate() != null && booking.getCheckoutDate() != null) {
                    LocalDate checkIn = booking.getCheckInDate();
                    LocalDate checkOut = booking.getCheckoutDate();
                    for (LocalDate date = checkIn; !date.isAfter(checkOut); date = date.plusDays(1)) {
                        bookingByDate.computeIfAbsent(date, k -> new ArrayList<>())
                                .add(String.format("Phòng ID: %d (Loại: %s)",
                                        booking.getRoom().getId(), booking.getRoom().getRoomType()));
                    }
                }
            }

            // Bước 3: Tạo chuỗi lịch đặt phòng
            StringBuilder bookingInfo = new StringBuilder("Lịch đặt phòng theo ngày:\n");
            if (bookingByDate.isEmpty()) {
                bookingInfo.append("Hiện không có lịch đặt phòng nào.\n");
            } else {
                for (Map.Entry<LocalDate, List<String>> entry : bookingByDate.entrySet()) {
                    String dateStr = entry.getKey().format(DateTimeFormatter.ofPattern("d/M/yyyy"));
                    String bookedRooms = String.join(", ", entry.getValue());
                    bookingInfo.append(String.format("- %s: %s\n", dateStr, bookedRooms));
                }
            }
            log.debug("Booking Info: {}", bookingInfo);

            // Bước 4: Xử lý câu hỏi về phòng trống hoặc đặt phòng
            String availableRoomsInfo = "";
            String bookingInstruction = "";
            String messageLower = message.toLowerCase();
            boolean isBookingRequest = messageLower.contains("tôi muốn đặt") || messageLower.contains("đặt phòng");
            Map<Long, String> availableRooms = new HashMap<>();

            if (messageLower.contains("phòng trống") || messageLower.contains("còn phòng") ||
                    messageLower.contains("phòng nào chưa đặt") || isBookingRequest) {
                // Trích xuất khoảng thời gian
                String[] dates = extractDateRange(message);
                LocalDate startDate;
                LocalDate endDate;
                if (dates.length == 2) {
                    try {
                        startDate = LocalDate.parse(dates[0], DateTimeFormatter.ofPattern("d/M/yyyy"));
                        endDate = LocalDate.parse(dates[1], DateTimeFormatter.ofPattern("d/M/yyyy"));
                        log.debug("Checking availability from {} to {}", startDate, endDate);
                    } catch (DateTimeParseException e) {
                        log.debug("Error parsing dates, using default: {}", e.getMessage());
                        startDate = LocalDate.now();
                        endDate = startDate.plusDays(3);
                    }
                } else {
                    startDate = LocalDate.now();
                    endDate = startDate.plusDays(3);
                    log.debug("No date range found, using default: {} to {}", startDate, endDate);
                }

                // Kiểm tra phòng trống
                Set<Long> bookedRoomIds = new HashSet<>();
                for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                    List<String> bookedRooms = bookingByDate.getOrDefault(date, new ArrayList<>());
                    for (String booked : bookedRooms) {
                        Long roomId = Long.parseLong(booked.split("Phòng ID: ")[1].split(" ")[0]);
                        bookedRoomIds.add(roomId);
                    }
                }

                for (Room room : rooms) {
                    if (room != null && room.getId() != null && !bookedRoomIds.contains(room.getId())) {
                        availableRooms.put(room.getId(),
                                String.format("Phòng ID: %d (Loại: %s)",
                                        room.getId(), room.getRoomType()));
                    }
                }

                // Tạo thông tin phòng trống (chỉ ID và loại phòng)
                if (availableRooms.isEmpty()) {
                    availableRoomsInfo = String.format("Không có phòng trống từ %s đến %s.",
                            startDate.format(DateTimeFormatter.ofPattern("d/M/yyyy")),
                            endDate.format(DateTimeFormatter.ofPattern("d/M/yyyy")));
                } else {
                    availableRoomsInfo = String.format("Phòng trống từ %s đến %s:\n%s",
                            startDate.format(DateTimeFormatter.ofPattern("d/M/yyyy")),
                            endDate.format(DateTimeFormatter.ofPattern("d/M/yyyy")),
                            String.join("\n", availableRooms.values()));
                }
                log.debug("Available Rooms Info: {}", availableRoomsInfo);

                // Xử lý yêu cầu đặt phòng (chỉ ID và loại phòng)
                if (isBookingRequest) {
                    if (availableRooms.isEmpty()) {
                        bookingInstruction = "Hiện không có phòng trống trong khoảng thời gian bạn yêu cầu.";
                    } else {
                        bookingInstruction = "Bạn có thể đặt một trong các phòng trống sau:\n" +
                                String.join("\n", availableRooms.values()) +
                                "\nVui lòng cung cấp ID phòng bạn muốn đặt hoặc liên hệ trực tiếp để được hỗ trợ.";
                    }
                }

                // Với câu hỏi phòng trống/đặt phòng, trả kết quả tính toán trực tiếp để tránh AI trả lệch
                String directReply = isBookingRequest ? bookingInstruction : availableRoomsInfo;
                if (directReply == null || directReply.isBlank()) {
                    directReply = buildFallbackReply(isBookingRequest, bookingInstruction, availableRoomsInfo);
                }

                saveMessage(userId, sessionId, "assistant", directReply);
                updatePreferenceFromMessage(userPref, message, directReply);
                return new ChatResponse(directReply);
            }

            // Bước 5: Tạo prompt và gọi Gemini API
            String prompt = buildPersonalizedPrompt(
                    message,
                    userPref,
                    history,
                    roomInfo.toString(),
                    bookingInfo.toString(),
                    availableRoomsInfo,
                    bookingInstruction
            );

            String botReply;
            try {
                if (!aiEnabled || System.currentTimeMillis() < aiQuotaBackoffUntilMs) {
                    botReply = buildFallbackReply(isBookingRequest, bookingInstruction, availableRoomsInfo);
                } else {
                    botReply = callGemini(prompt);
                }
                if (botReply.length() < availableRoomsInfo.length() && !availableRooms.isEmpty()) {
                    botReply = bookingInstruction;
                    log.debug("Gemini response truncated, using fallback.");
                }
            } catch (Exception e) {
                if (isQuotaExceeded(e)) {
                    aiQuotaBackoffUntilMs = System.currentTimeMillis() + Math.max(quotaBackoffMs, 30_000L);
                }
                botReply = buildFallbackReply(isBookingRequest, bookingInstruction, availableRoomsInfo);
                log.warn("Gemini API failed, using fallback: {}", e.getMessage());
            }

            saveMessage(userId, sessionId, "assistant", botReply);
            updatePreferenceFromMessage(userPref, message, botReply);
            return new ChatResponse(botReply);
        } catch (Exception e) {
            return new ChatResponse("Lỗi khi xử lý yêu cầu: " + e.getMessage());
        }
    }

    private String normalizeUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return "guest";
        }
        return userId.trim();
    }

    private String normalizeSessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return sessionId.trim();
    }

    private String buildPersonalizedPrompt(String message,
                                           UserPreference pref,
                                           List<ConversationMessage> history,
                                           String roomInfo,
                                           String bookingInfo,
                                           String availableRoomsInfo,
                                           String bookingInstruction) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Bạn là trợ lý đặt phòng thông minh của khách sạn, trả lời bằng tiếng Việt, thân thiện, ngắn gọn.\n\n");

        prompt.append("=== THÔNG TIN KHÁCH HÀNG ===\n");
        prompt.append(String.format("Mã khách: %s\n", pref.getUserId()));
        prompt.append(String.format("Số lần đặt phòng: %d\n", pref.getTotalBookings() == null ? 0 : pref.getTotalBookings()));
        if (pref.getPreferredRoomType() != null) {
            prompt.append(String.format("Loại phòng hay quan tâm: %s\n", pref.getPreferredRoomType()));
        }
        if (pref.getPreferredPriceRange() != null) {
            prompt.append(String.format("Mức giá ưa thích: %s\n", pref.getPreferredPriceRange()));
        }
        if (pref.getPreferredCheckInTime() != null) {
            prompt.append(String.format("Khung giờ check-in ưa thích: %s\n", pref.getPreferredCheckInTime()));
        }
        if (pref.getLastBookedRoomType() != null) {
            prompt.append(String.format("Loại phòng đặt gần nhất: %s\n", pref.getLastBookedRoomType()));
        }
        if (pref.getNotes() != null && !pref.getNotes().isBlank()) {
            prompt.append(String.format("Ghi chú: %s\n", pref.getNotes()));
        }

        if (!history.isEmpty()) {
            prompt.append("\n=== LỊCH SỬ HỘI THOẠI GẦN ĐÂY ===\n");
            history.stream()
                    .skip(Math.max(0, history.size() - MAX_HISTORY))
                    .forEach(msg -> prompt.append(String.format("[%s]: %s\n",
                            "user".equalsIgnoreCase(msg.getRole()) ? "Khách" : "Bot",
                            msg.getContent())));
        }

        prompt.append("\n=== DỮ LIỆU PHÒNG KHÁCH SẠN ===\n");
        prompt.append(roomInfo).append("\n");
        prompt.append("=== LỊCH ĐẶT PHÒNG ===\n");
        prompt.append(bookingInfo).append("\n");

        if (availableRoomsInfo != null && !availableRoomsInfo.isBlank()) {
            prompt.append("=== THÔNG TIN PHÒNG TRỐNG TÍNH TOÁN SẴN ===\n");
            prompt.append(availableRoomsInfo).append("\n");
        }
        if (bookingInstruction != null && !bookingInstruction.isBlank()) {
            prompt.append("=== GỢI Ý ĐẶT PHÒNG TÍNH TOÁN SẴN ===\n");
            prompt.append(bookingInstruction).append("\n");
        }

        prompt.append(String.format("\n=== CÂU HỎI HIỆN TẠI ===\n%s\n", message));
        prompt.append("""
                === HƯỚNG DẪN TRẢ LỜI ===
                - Gọi khách theo mã khách khi phù hợp
                - Ưu tiên đề xuất theo sở thích đã lưu nếu có
                - Nếu có ngữ cảnh lịch sử liên quan thì nhắc lại ngắn gọn
                - Chỉ dùng dữ liệu đã cung cấp, không suy luận thêm
                - Không tiết lộ dữ liệu nhạy cảm
                """);

        return prompt.toString();
    }

    private void updatePreferenceFromMessage(UserPreference pref, String message, String reply) {
        String msgLower = message == null ? "" : message.toLowerCase();

        if (msgLower.contains("suite")) {
            pref.setPreferredRoomType("SUITE");
        } else if (msgLower.contains("deluxe")) {
            pref.setPreferredRoomType("DELUXE");
        } else if (msgLower.contains("standard")) {
            pref.setPreferredRoomType("STANDARD");
        }

        if (msgLower.contains("rẻ") || msgLower.contains("tiết kiệm") || msgLower.contains("giá thấp")) {
            pref.setPreferredPriceRange("budget");
        } else if (msgLower.contains("cao cấp") || msgLower.contains("sang") || msgLower.contains("luxury")) {
            pref.setPreferredPriceRange("luxury");
        } else if (msgLower.contains("vừa túi tiền") || msgLower.contains("tầm trung") || msgLower.contains("mid")) {
            pref.setPreferredPriceRange("mid");
        }

        if (msgLower.contains("sáng")) {
            pref.setPreferredCheckInTime("morning");
        } else if (msgLower.contains("chiều")) {
            pref.setPreferredCheckInTime("afternoon");
        } else if (msgLower.contains("tối")) {
            pref.setPreferredCheckInTime("evening");
        }

        if (msgLower.contains("đặt phòng") || msgLower.contains("tôi muốn đặt")) {
            Integer totalBookings = pref.getTotalBookings() == null ? 0 : pref.getTotalBookings();
            pref.setTotalBookings(totalBookings + 1);
            if (pref.getPreferredRoomType() != null) {
                pref.setLastBookedRoomType(pref.getPreferredRoomType());
            }
        }

        if (reply != null && !reply.isBlank()) {
            String shortNote = reply.length() > 300 ? reply.substring(0, 300) : reply;
            pref.setNotes(shortNote);
        }

        preferenceRepo.save(pref);
    }

    private UserPreference createNewPreference(String userId) {
        UserPreference pref = new UserPreference();
        pref.setUserId(userId);
        pref.setTotalBookings(0);
        return preferenceRepo.save(pref);
    }

    private void saveMessage(String userId, String sessionId, String role, String content) {
        ConversationMessage msg = new ConversationMessage();
        msg.setUserId(userId);
        msg.setSessionId(sessionId);
        msg.setRole(role);
        msg.setContent(content);
        conversationRepo.save(msg);
    }

    private String[] extractDateRange(String message) {
        Pattern pattern = Pattern.compile("\\d{1,2}/\\d{1,2}(/\\d{4})?");
        Matcher matcher = pattern.matcher(message.toLowerCase());
        List<String> dates = new ArrayList<>();
        while (matcher.find()) {
            String date = matcher.group();
            if (!date.contains("/202")) {
                date += "/" + LocalDate.now().getYear();
            }
            dates.add(date);
        }
        return dates.size() >= 2 ? new String[]{dates.get(0), dates.get(1)} : new String[]{};
    }

    private String buildFallbackReply(boolean isBookingRequest, String bookingInstruction, String availableRoomsInfo) {
        if (isBookingRequest && bookingInstruction != null && !bookingInstruction.isBlank()) {
            return bookingInstruction;
        }
        if (availableRoomsInfo != null && !availableRoomsInfo.isBlank()) {
            return availableRoomsInfo;
        }
        return "Hệ thống AI đang quá tải tạm thời. Bạn vui lòng thử lại sau vài phút.";
    }

    private boolean isQuotaExceeded(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                String lower = message.toLowerCase();
                if (lower.contains("429") || lower.contains("quota") || lower.contains("rate limit")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    private String callGemini(String message) throws Exception {
        String response = chatClient.prompt()
                .user(message)
                .call()
                .content();

        if (response == null || response.isBlank()) {
            throw new Exception("Không nhận được phản hồi hợp lệ từ Gemini API");
        }
        return response;
    }
}