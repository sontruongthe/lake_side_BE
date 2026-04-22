package booking.booking_hotel.service;

import java.util.List;
import java.util.Map;

public interface DashboardService {
    Map<String, Object> getDashboardStatistics();
    Map<String, Object> getMonthlyRevenue(int year);
    Map<String, Long> getBookingsByRoomType();
    Map<String, Long> getRoomStatusCount();
    Object getRecentBookings(int limit);
}
