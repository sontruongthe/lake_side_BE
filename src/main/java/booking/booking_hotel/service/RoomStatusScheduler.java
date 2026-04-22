package booking.booking_hotel.service;

import booking.booking_hotel.model.BookedRoom;
import booking.booking_hotel.model.Room;
import booking.booking_hotel.repository.BookingRepository;
import booking.booking_hotel.repository.RoomRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service tự động cập nhật trạng thái phòng theo lịch
 * Chạy tự động mỗi ngày để quản lý phòng
 */
@Service
@RequiredArgsConstructor
public class RoomStatusScheduler {
    
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    
    /**
     * Chạy NGAY KHI Spring Boot khởi động
     * Để đồng bộ lại trạng thái phòng với database
     * 
     * Nếu KHÔNG MUỐN tự động chạy khi start → Comment dòng @PostConstruct
     */
    // @PostConstruct  // ← Comment dòng này để TẮT auto-run khi start
    @Transactional
    public void initRoomStatus() {
        System.out.println("========================================");
        System.out.println("🔄 Đang đồng bộ trạng thái phòng lúc khởi động...");
        updateRoomStatusAfterCheckout();
        updateRoomStatusForCheckIn();
        System.out.println("✅ Hoàn tất đồng bộ trạng thái phòng!");
        System.out.println("========================================");
    }

    /**
     * Tự động reset phòng về available sau khi khách checkout
     * Chạy mỗi ngày lúc 00:01 (12:01 AM)
     * Cron format: giây phút giờ ngày tháng thứ
     */
    @Scheduled(cron = "0 1 0 * * ?") // 00:01 mỗi ngày
    @Transactional
    public void updateRoomStatusAfterCheckout() {
        LocalDate today = LocalDate.now(); // Lấy ngày hôm nay
        
        // Tìm tất cả booking đã hết hạn (checkout date < hôm nay)
        List<BookedRoom> expiredBookings = bookingRepository.findByCheckoutDateBefore(today);
        
        System.out.println("📋 Tìm thấy " + expiredBookings.size() + " booking đã hết hạn");
        
        int resetCount = 0;
        int skippedAlreadyAvailable = 0;
        int skippedHasNewBooking = 0;
        
        // Duyệt qua từng booking đã hết hạn
        for (BookedRoom booking : expiredBookings) {
            Room room = booking.getRoom();
            
            if (room == null) continue;
            
            // Nếu phòng đã ở trạng thái available rồi
            if (!room.isBooked()) {
                skippedAlreadyAvailable++;
                System.out.println("⏭️  Phòng " + room.getId() + " đã ở trạng thái available, bỏ qua");
                continue;
            }
            
            // Kiểm tra xem phòng có booking mới không
            boolean hasActiveBooking = bookingRepository
                .existsByRoomAndCheckInDateLessThanEqualAndCheckoutDateGreaterThanEqual(
                    room, today, today
                );
            
            if (hasActiveBooking) {
                skippedHasNewBooking++;
                System.out.println("⏭️  Phòng " + room.getId() + " có booking mới, không reset");
            } else {
                room.setBooked(false);
                roomRepository.save(room);
                resetCount++;
                System.out.println("✅ Phòng " + room.getId() + " đã được reset về trạng thái available");
            }
        }
        
        System.out.println("📊 Tổng kết: Reset " + resetCount + " phòng | " +
                         "Bỏ qua " + skippedAlreadyAvailable + " phòng (đã available) | " +
                         "Bỏ qua " + skippedHasNewBooking + " phòng (có booking mới)");
    }

    /**
     * Tự động set phòng thành booked khi có khách check-in hôm nay
     * Chạy mỗi ngày lúc 00:05 (12:05 AM)
     */
    @Scheduled(cron = "0 5 0 * * ?") // 00:05 mỗi ngày
    @Transactional
    public void updateRoomStatusForCheckIn() {
        LocalDate today = LocalDate.now();
        
        // Tìm TẤT CẢ booking đang active (hôm nay nằm trong khoảng check-in đến checkout)
        // Không chỉ tìm booking check-in hôm nay, mà tìm cả booking đang ở giữa kỳ
        List<BookedRoom> activeBookings = bookingRepository
            .findByCheckInDateLessThanEqualAndCheckoutDateGreaterThanEqual(today, today);
        
        // Đánh dấu phòng là đã booked
        for (BookedRoom booking : activeBookings) {
            Room room = booking.getRoom();
            if (room != null && !room.isBooked()) {
                room.setBooked(true);
                roomRepository.save(room);
                System.out.println("Phòng " + room.getId() + " đã được đánh dấu booked (booking active từ " 
                    + booking.getCheckInDate() + " đến " + booking.getCheckoutDate() + ")");
            }
        }
    }

    /**
     * Method test - có thể gọi thủ công để kiểm tra nếu cần
     * Không tự động chạy - chỉ dùng khi cần test thủ công
     */
    public void testUpdateRoomStatus() {
        System.out.println("========================================");
        System.out.println("🔄 Chạy test cập nhật trạng thái phòng lúc: " + LocalDate.now());
        updateRoomStatusAfterCheckout();
        updateRoomStatusForCheckIn();
        System.out.println("✅ Hoàn tất kiểm tra");
        System.out.println("========================================");
    }
}
