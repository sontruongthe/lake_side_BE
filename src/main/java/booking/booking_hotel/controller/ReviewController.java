package booking.booking_hotel.controller;

import booking.booking_hotel.model.Review;
import booking.booking_hotel.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    
    private final ReviewService reviewService;
    
    /**
     * Thêm review mới cho phòng
     * POST /api/reviews
     */
    @PostMapping
    public ResponseEntity<?> addReview(@RequestBody Map<String, Object> reviewData) {
        try {
            Long roomId = Long.valueOf(reviewData.get("roomId").toString());
            Long bookingId = reviewData.get("bookingId") != null ? 
                    Long.valueOf(reviewData.get("bookingId").toString()) : null;
            Integer rating = Integer.valueOf(reviewData.get("rating").toString());
            
            // Validate rating
            if (rating < 1 || rating > 5) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Đánh giá phải từ 1 đến 5 sao"
                ));
            }
            
            String comment = reviewData.get("comment") != null ? 
                    reviewData.get("comment").toString() : "";
            String guestName = reviewData.get("guestName").toString();
            String guestEmail = reviewData.get("guestEmail").toString();
            
            Review review = reviewService.addReview(roomId, bookingId, rating, 
                    comment, guestName, guestEmail);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Review added successfully");
            response.put("review", mapReviewToResponse(review));
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Error adding review: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Lấy tất cả reviews của một phòng
     * GET /api/reviews/room/{roomId}
     */
    @GetMapping("/room/{roomId}")
    public ResponseEntity<?> getReviewsByRoom(@PathVariable Long roomId) {
        try {
            List<Review> reviews = reviewService.getReviewsByRoom(roomId);
            
            List<Map<String, Object>> reviewsResponse = reviews.stream()
                    .map(this::mapReviewToResponse)
                    .toList();
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "reviews", reviewsResponse,
                    "count", reviews.size()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Error fetching reviews: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Cập nhật review
     * PUT /api/reviews/{reviewId}
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable Long reviewId,
            @RequestBody Map<String, Object> updateData) {
        try {
            Integer rating = updateData.get("rating") != null ? 
                    Integer.valueOf(updateData.get("rating").toString()) : null;
            
            // Validate rating if provided
            if (rating != null && (rating < 1 || rating > 5)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Đánh giá phải từ 1 đến 5 sao"
                ));
            }
            
            String comment = updateData.get("comment") != null ? 
                    updateData.get("comment").toString() : null;
            
            Review review = reviewService.updateReview(reviewId, rating, comment);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Review updated successfully",
                    "review", mapReviewToResponse(review)
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Error updating review: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Xóa review
     * DELETE /api/reviews/{reviewId}
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId) {
        try {
            reviewService.deleteReview(reviewId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Review deleted successfully"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Error deleting review: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Helper: Map Review entity to response
     */
    private Map<String, Object> mapReviewToResponse(Review review) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", review.getId());
        response.put("roomId", review.getRoom().getId());
        response.put("bookingId", review.getBooking() != null ? review.getBooking().getBookingId() : null);
        response.put("rating", review.getRating());
        response.put("comment", review.getComment());
        response.put("guestName", review.getGuestName());
        response.put("guestEmail", review.getGuestEmail());
        response.put("createdAt", review.getCreatedAt().toString());
        response.put("updatedAt", review.getUpdatedAt() != null ? review.getUpdatedAt().toString() : null);
        return response;
    }
}
