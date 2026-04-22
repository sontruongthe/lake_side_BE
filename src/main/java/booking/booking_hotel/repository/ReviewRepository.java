package booking.booking_hotel.repository;

import booking.booking_hotel.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // Lấy tất cả reviews của một phòng
    List<Review> findByRoomIdOrderByCreatedAtDesc(Long roomId);
    
    // Kiểm tra user đã review phòng này chưa (theo bookingId của BookedRoom)
    Optional<Review> findByBookingBookingId(Long bookingId);
    
    // Kiểm tra email đã review phòng này chưa
    boolean existsByRoomIdAndGuestEmail(Long roomId, String guestEmail);
    
    // Tính điểm trung bình của phòng
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.room.id = :roomId")
    Double calculateAverageRating(@Param("roomId") Long roomId);
    
    // Đếm số lượng reviews của phòng
    Long countByRoomId(Long roomId);
    
    // Delete all reviews for a booking
    void deleteByBooking_BookingId(Long bookingId);
}
