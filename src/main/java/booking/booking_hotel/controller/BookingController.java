package booking.booking_hotel.controller;

import booking.booking_hotel.exception.InvalidBookingRequestException;
import booking.booking_hotel.exception.ResourceNotFoundException;
import booking.booking_hotel.model.BookedRoom;

import booking.booking_hotel.model.Room;
import booking.booking_hotel.response.BookingResponse;
import booking.booking_hotel.response.RoomResponse;
import booking.booking_hotel.service.IBookingService;
import booking.booking_hotel.service.IRoomService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final IBookingService bookingService;
    private final IRoomService roomService;


    public BookingController(IBookingService bookingService, IRoomService roomService) {
        this.bookingService = bookingService;
        this.roomService = roomService;
    }



    @GetMapping("/all-bookings")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        List<BookedRoom> bookings = bookingService.getAllBookings();
        List<BookingResponse> bookingResponses = new ArrayList<>();
        for (BookedRoom booking : bookings) {
            bookingResponses.add(getBookingResponse(booking));
        }
        return ResponseEntity.ok(bookingResponses);
    }

    @GetMapping("/confirmation/{confirmationCode}")
    public ResponseEntity<?> getBookingByConfirmationCode(@PathVariable String confirmationCode) {
        try {
            BookedRoom booking = bookingService.findByBookingConfimationCode(confirmationCode);
            if (booking == null || booking.getRoom() == null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Khong co ma phong");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            return ResponseEntity.ok(getBookingResponse(booking));
        } catch (ResourceNotFoundException ex) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Khong co ma phong");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping("/room/{roomId}/booking")
    public ResponseEntity<?> saveBooking(@PathVariable Long roomId, @RequestBody BookedRoom bookingRequest) {
        try {
            String confirmationCode = bookingService.saveBooking(roomId, bookingRequest);
            
            // Tìm booking vừa tạo để lấy bookingId
            BookedRoom booking = bookingService.findByBookingConfimationCode(confirmationCode);
            
            // Trả về object với cả confirmationCode và bookingId
            Map<String, Object> response = new HashMap<>();
            response.put("confirmationCode", confirmationCode);
            response.put("bookingId", booking.getBookingId());
            response.put("message", "Room booked successfully");
            
            return ResponseEntity.ok(response);
        } catch (InvalidBookingRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/booking/{bookingId}/delete")
    public void cancelBooking(@PathVariable Long bookingId) {
        bookingService.cancelBooking(bookingId);
    }

    /**
     * API để reset (làm mới) trạng thái phòng cho các booking đã quá hạn
     * Khi admin click button "Reset Expired Rooms" thì gọi API này
     * POST: http://localhost:9192/bookings/reset-expired-rooms
     */
    @PostMapping("/reset-expired-rooms")
    public ResponseEntity<Map<String, Object>> resetExpiredRooms() {
        LocalDate today = LocalDate.now(); // Lấy ngày hôm nay
        List<BookedRoom> allBookings = bookingService.getAllBookings(); // Lấy tất cả booking
        
        int resetCount = 0; // Đếm số phòng đã reset
        List<Long> resetRoomIds = new ArrayList<>(); // Lưu danh sách ID phòng đã reset
        
        // Duyệt qua tất cả booking
        for (BookedRoom booking : allBookings) {
            // Nếu ngày checkout < hôm nay (tức là đã quá hạn)
            if (booking.getCheckoutDate().isBefore(today)) {
                Room room = booking.getRoom();
                // Nếu phòng vẫn đang ở trạng thái booked (đang bị khóa)
                if (room != null && room.isBooked()) {
                    // Reset phòng về available (có thể đặt lại)
                    room.setBooked(false);
                    roomService.updateRoom(room.getId(), room.getRoomType(), room.getRoomPrice(), null);
                    resetCount++;
                    resetRoomIds.add(room.getId());
                }
            }
        }
        
        // Tạo response trả về cho frontend
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Đã reset " + resetCount + " phòng thành công");
        response.put("resetCount", resetCount);
        response.put("resetRoomIds", resetRoomIds);
        response.put("status", "success");
        
        return ResponseEntity.ok(response);
    }

    /**
     * API để kiểm tra có bao nhiêu phòng cần reset (chưa reset)
     * Hiển thị badge số phòng cần reset trên UI
     * GET: http://localhost:9192/bookings/check-expired-rooms
     */
    @GetMapping("/check-expired-rooms")
    public ResponseEntity<Map<String, Object>> checkExpiredRooms() {
        LocalDate today = LocalDate.now();
        List<BookedRoom> allBookings = bookingService.getAllBookings();
        
        List<Map<String, Object>> expiredRooms = new ArrayList<>();
        
        // Tìm các phòng đã quá checkout nhưng vẫn đang booked
        for (BookedRoom booking : allBookings) {
            if (booking.getCheckoutDate().isBefore(today)) {
                Room room = booking.getRoom();
                if (room != null && room.isBooked()) {
                    // Thêm thông tin phòng vào danh sách
                    Map<String, Object> roomInfo = new HashMap<>();
                    roomInfo.put("roomId", room.getId());
                    roomInfo.put("roomType", room.getRoomType());
                    roomInfo.put("checkoutDate", booking.getCheckoutDate());
                    roomInfo.put("guestName", booking.getGuestFullName());
                    roomInfo.put("bookingId", booking.getBookingId());
                    expiredRooms.add(roomInfo);
                }
            }
        }
        
        // Trả về số lượng và danh sách phòng cần reset
        Map<String, Object> response = new HashMap<>();
        response.put("expiredRoomsCount", expiredRooms.size());
        response.put("expiredRooms", expiredRooms);
        
        return ResponseEntity.ok(response);
    }

    private BookingResponse getBookingResponse(BookedRoom booking) {
        Room theRoom = roomService.getRoomById(booking.getRoom().getId()).get();
        RoomResponse room = new RoomResponse(
                theRoom.getId(),
                theRoom.getRoomType(),
                theRoom.getRoomPrice()
        );
        return new BookingResponse(
                booking.getBookingId(),
                booking.getCheckInDate(),
                booking.getCheckoutDate(),
                booking.getGuestFullName(),
                booking.getGuestEmail(),
                booking.getNumOfAdults(),
                booking.getNumOfChildren(),
                booking.getBookingConfirmationCode(),
                booking.getTotalNumOfGuest(),
                room
        );
    }
}