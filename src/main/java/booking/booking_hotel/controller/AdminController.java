package booking.booking_hotel.controller;

import booking.booking_hotel.model.Room;
import booking.booking_hotel.repository.RoomRepository;
import booking.booking_hotel.service.RoomStatusScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final RoomStatusScheduler roomStatusScheduler;
    private final RoomRepository roomRepository;
    
    /**
     * Manual trigger để update room status (dùng cho testing)
     * POST: http://localhost:9192/api/admin/update-room-status
     */
    @PostMapping("/update-room-status")
    public ResponseEntity<Map<String, String>> manualUpdateRoomStatus() {
        // Chạy cả 2 function: reset expired và set check-in
        roomStatusScheduler.updateRoomStatusAfterCheckout();
        roomStatusScheduler.updateRoomStatusForCheckIn();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Đã cập nhật trạng thái phòng thành công");
        response.put("status", "success");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Kiểm tra trạng thái của các phòng cụ thể
     * GET: http://localhost:9192/api/admin/check-rooms?ids=30,31,32
     */
    @GetMapping("/check-rooms")
    public ResponseEntity<List<Map<String, Object>>> checkRoomStatus(@RequestParam String ids) {
        String[] roomIds = ids.split(",");
        List<Map<String, Object>> rooms = new ArrayList<>();
        
        for (String idStr : roomIds) {
            Long roomId = Long.parseLong(idStr.trim());
            Room room = roomRepository.findById(roomId).orElse(null);
            
            if (room != null) {
                Map<String, Object> roomData = new HashMap<>();
                roomData.put("roomId", room.getId());
                roomData.put("roomType", room.getRoomType());
                roomData.put("isBooked", room.isBooked());
                roomData.put("bookingsCount", room.getBookings() != null ? room.getBookings().size() : 0);
                rooms.add(roomData);
            }
        }
        
        return ResponseEntity.ok(rooms);
    }
}
