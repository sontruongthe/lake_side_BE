package booking.booking_hotel.controller;

import booking.booking_hotel.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:4200"})
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getDashboardStatistics() {
        Map<String, Object> statistics = dashboardService.getDashboardStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/revenue/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyRevenue(
            @RequestParam(required = false) Integer year) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        Map<String, Object> revenue = dashboardService.getMonthlyRevenue(year);
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/bookings/by-room-type")
    public ResponseEntity<Map<String, Long>> getBookingsByRoomType() {
        Map<String, Long> bookings = dashboardService.getBookingsByRoomType();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/rooms/status")
    public ResponseEntity<Map<String, Long>> getRoomStatusCount() {
        Map<String, Long> status = dashboardService.getRoomStatusCount();
        return ResponseEntity.ok(status);
    }

    @GetMapping("/recent-bookings")
    public ResponseEntity<Object> getRecentBookings(@RequestParam(defaultValue = "10") int limit) {
        Object bookings = dashboardService.getRecentBookings(limit);
        return ResponseEntity.ok(bookings);
    }
}
