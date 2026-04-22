package booking.booking_hotel.service;

import booking.booking_hotel.exception.ResourceNotFoundException;
import booking.booking_hotel.model.BookedRoom;
import booking.booking_hotel.model.Review;
import booking.booking_hotel.model.Room;
import booking.booking_hotel.repository.BookingRepository;
import booking.booking_hotel.repository.ReviewRepository;
import booking.booking_hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    
    /**
     * Thêm review mới
     */
    @Transactional
    public Review addReview(Long roomId, Long bookingId, Integer rating, 
                           String comment, String guestName, String guestEmail) {
        
        // Validate rating
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        
        // Tìm phòng
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        
        // Kiểm tra user đã review chưa (nếu có bookingId)
        if (bookingId != null) {
            BookedRoom booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
            
            // Kiểm tra booking có thuộc phòng này không
            if (!booking.getRoom().getId().equals(roomId)) {
                throw new IllegalArgumentException("Booking does not belong to this room");
            }
            
            // Kiểm tra đã review chưa
            if (reviewRepository.findByBookingBookingId(bookingId).isPresent()) {
                throw new IllegalArgumentException("You have already reviewed this booking");
            }
        } else {
            // Nếu không có booking, check theo email
            if (reviewRepository.existsByRoomIdAndGuestEmail(roomId, guestEmail)) {
                throw new IllegalArgumentException("You have already reviewed this room");
            }
        }
        
        // Tạo review mới
        Review review = new Review();
        review.setRoom(room);
        if (bookingId != null) {
            BookedRoom booking = bookingRepository.findById(bookingId).get();
            review.setBooking(booking);
        }
        review.setRating(rating);
        review.setComment(comment);
        review.setGuestName(guestName);
        review.setGuestEmail(guestEmail);
        review.setCreatedAt(LocalDateTime.now());
        
        // Lưu review
        Review savedReview = reviewRepository.save(review);
        
        // Cập nhật rating của phòng
        updateRoomRating(roomId);
        
        return savedReview;
    }
    
    /**
     * Lấy tất cả reviews của phòng
     */
    public List<Review> getReviewsByRoom(Long roomId) {
        return reviewRepository.findByRoomIdOrderByCreatedAtDesc(roomId);
    }
    
    /**
     * Cập nhật rating trung bình của phòng
     */
    @Transactional
    public void updateRoomRating(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        
        Double averageRating = reviewRepository.calculateAverageRating(roomId);
        Long reviewCount = reviewRepository.countByRoomId(roomId);
        
        room.setAverageRating(averageRating != null ? averageRating : 0.0);
        room.setReviewCount(reviewCount.intValue());
        
        roomRepository.save(room);
    }
    
    /**
     * Xóa review
     */
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        
        Long roomId = review.getRoom().getId();
        reviewRepository.deleteById(reviewId);
        
        // Cập nhật lại rating
        updateRoomRating(roomId);
    }
    
    /**
     * Cập nhật review
     */
    @Transactional
    public Review updateReview(Long reviewId, Integer rating, String comment) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        
        if (rating != null) {
            review.setRating(rating);
        }
        if (comment != null) {
            review.setComment(comment);
        }
        review.setUpdatedAt(LocalDateTime.now());
        
        Review updatedReview = reviewRepository.save(review);
        
        // Cập nhật rating của phòng
        updateRoomRating(review.getRoom().getId());
        
        return updatedReview;
    }
}
