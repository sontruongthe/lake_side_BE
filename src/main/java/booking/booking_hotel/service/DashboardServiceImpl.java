package booking.booking_hotel.service;

import booking.booking_hotel.model.BookedRoom;
import booking.booking_hotel.model.Room;
import booking.booking_hotel.repository.BookingRepository;
import booking.booking_hotel.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Override
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // Tổng số phòng
        long totalRooms = roomRepository.count();
        statistics.put("totalRooms", totalRooms);
        
        // Tổng số booking
        long totalBookings = bookingRepository.count();
        statistics.put("totalBookings", totalBookings);
        
        // Tính tổng doanh thu
        List<BookedRoom> allBookings = bookingRepository.findAll();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        
        for (BookedRoom booking : allBookings) {
            if (booking.getRoom() != null && booking.getRoom().getRoomPrice() != null) {
                long days = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckoutDate());
                if (days <= 0) days = 1;
                BigDecimal bookingRevenue = booking.getRoom().getRoomPrice().multiply(BigDecimal.valueOf(days));
                totalRevenue = totalRevenue.add(bookingRevenue);
            }
        }
        statistics.put("totalRevenue", totalRevenue);
        
        // Tính tỷ lệ lấp đầy phòng HÔM NAY (%)
        // Công thức: (Số phòng có khách hôm nay / Tổng số phòng) × 100
        LocalDate today = LocalDate.now();
        List<Room> allRooms = roomRepository.findAll();
        
        // Đếm số phòng có booking đang active hôm nay
        long bookedRoomsToday = allRooms.stream()
            .filter(room -> {
                List<BookedRoom> roomBookings = bookingRepository.findByRoomId(room.getId());
                return roomBookings.stream()
                    .anyMatch(booking -> 
                        // Check: check-in <= hôm nay <= checkout
                        !booking.getCheckInDate().isAfter(today) && 
                        !booking.getCheckoutDate().isBefore(today)
                    );
            })
            .count();
        
        // Tính tỷ lệ % và làm tròn 2 chữ số thập phân
        double occupancyRate = totalRooms > 0 ? (bookedRoomsToday * 100.0 / totalRooms) : 0;
        statistics.put("occupancyRate", Math.round(occupancyRate * 100.0) / 100.0);
        
        return statistics;
    }

    @Override
    public Map<String, Object> getMonthlyRevenue(int year) {
        Map<String, Object> result = new HashMap<>();
        List<BookedRoom> bookings = bookingRepository.findAll();
        
        // Tạo map với 12 tháng
        Map<Integer, BigDecimal> monthlyData = new LinkedHashMap<>();
        for (int i = 1; i <= 12; i++) {
            monthlyData.put(i, BigDecimal.ZERO);
        }
        
        for (BookedRoom booking : bookings) {
            if (booking.getCheckInDate().getYear() == year && booking.getRoom() != null) {
                int month = booking.getCheckInDate().getMonthValue();
                long days = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckoutDate());
                if (days <= 0) days = 1;
                
                BigDecimal revenue = booking.getRoom().getRoomPrice().multiply(BigDecimal.valueOf(days));
                monthlyData.put(month, monthlyData.get(month).add(revenue));
            }
        }
        
        result.put("year", year);
        result.put("data", monthlyData);
        return result;
    }

    @Override
    public Map<String, Long> getBookingsByRoomType() {
        List<BookedRoom> bookings = bookingRepository.findAll();
        
        return bookings.stream()
                .filter(booking -> booking.getRoom() != null)
                .collect(Collectors.groupingBy(
                        booking -> booking.getRoom().getRoomType(),
                        Collectors.counting()
                ));
    }

    @Override
    public Map<String, Long> getRoomStatusCount() {
        List<Room> rooms = roomRepository.findAll();
        LocalDate today = LocalDate.now();
        
        // Đếm số phòng có booking đang active (check-in <= hôm nay <= checkout)
        long bookedToday = rooms.stream()
            .filter(room -> {
                // Lấy tất cả booking của phòng này
                List<BookedRoom> roomBookings = bookingRepository.findByRoomId(room.getId());
                // Kiểm tra có booking nào đang active hôm nay không
                return roomBookings.stream()
                    .anyMatch(booking -> 
                        !booking.getCheckInDate().isAfter(today) && 
                        !booking.getCheckoutDate().isBefore(today)
                    );
            })
            .count();
        
        long totalRooms = rooms.size();
        long available = totalRooms - bookedToday;
        
        Map<String, Long> status = new HashMap<>();
        status.put("available", available);
        status.put("booked", bookedToday);
        
        return status;
    }

    @Override
    public Object getRecentBookings(int limit) {
        List<BookedRoom> allBookings = bookingRepository.findAll();
        
        // Sắp xếp theo booking ID giảm dần (mới nhất trước)
        return allBookings.stream()
                .sorted((b1, b2) -> Long.compare(b2.getBookingId(), b1.getBookingId()))
                .limit(limit)
                .map(booking -> {
                    Map<String, Object> bookingInfo = new HashMap<>();
                    bookingInfo.put("bookingId", booking.getBookingId());
                    bookingInfo.put("confirmationCode", booking.getBookingConfirmationCode());
                    bookingInfo.put("guestFullName", booking.getGuestFullName());
                    bookingInfo.put("checkInDate", booking.getCheckInDate());
                    bookingInfo.put("checkoutDate", booking.getCheckoutDate());
                    
                    if (booking.getRoom() != null) {
                        bookingInfo.put("roomType", booking.getRoom().getRoomType());
                        bookingInfo.put("roomPrice", booking.getRoom().getRoomPrice());
                    }
                    
                    return bookingInfo;
                })
                .collect(Collectors.toList());
    }
}
