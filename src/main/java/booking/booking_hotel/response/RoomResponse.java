package booking.booking_hotel.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
@Data
@NoArgsConstructor
public class RoomResponse {
    private Long id;

    private String roomType;
    private BigDecimal roomPrice;
    private boolean isBooked;

    private String photo;
    private List<BookingResponse> bookings;
    
    // Rating fields
    private Double averageRating = 0.0;
    private Integer reviewCount = 0;

    public Long getId() {
        return id;
    }

    public String getRoomType() {
        return roomType;
    }

    public BigDecimal getRoomPrice() {
        return roomPrice;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public String getPhoto() {
        return photo;
    }

    public List<BookingResponse> getBookings() {
        return bookings;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRoomPrice(BigDecimal roomPrice) {
        this.roomPrice = roomPrice;
    }

    public void setBooked(boolean booked) {
        isBooked = booked;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setBookings(List<BookingResponse> bookings) {
        this.bookings = bookings;
    }
    
    public Double getAverageRating() {
        return averageRating;
    }
    
    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
    
    public Integer getReviewCount() {
        return reviewCount;
    }
    
    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public RoomResponse(String roomType, BigDecimal roomPrice, boolean isBooked, Long id, byte[] photoBytes) {
        this.roomType = roomType;
        this.roomPrice = roomPrice;
        this.isBooked = isBooked;
        this.id = id;
        this.photo = photoBytes != null ? Base64.getEncoder().encodeToString(photoBytes) : null;
    }

    public RoomResponse(Long id, String roomType, BigDecimal roomPrice) {
        this.id = id;
        this.roomPrice=roomPrice;
        this.roomType=roomType;
    }


}
