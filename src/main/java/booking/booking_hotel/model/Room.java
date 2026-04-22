package booking.booking_hotel.model;

import jakarta.persistence.*;

import org.apache.commons.lang3.RandomStringUtils;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

import java.math.BigDecimal;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;
@Entity
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomType;
    private BigDecimal roomPrice;
    private boolean isBooked=false;
    @Lob
    private Blob photo;
    
    // Rating fields
    @Column(name = "average_rating")
    @Min(value = 0, message = "Điểm trung bình không thể âm")
    @Max(value = 5, message = "Điểm trung bình tối đa 5 sao")
    private Double averageRating = 0.0; // Điểm trung bình 0.0 - 5.0 (tính từ reviews)
    
    @Column(name = "review_count")
    @Min(value = 0, message = "Số lượng đánh giá không thể âm")
    private Integer reviewCount = 0; // Số lượng đánh giá

    @OneToMany(mappedBy="room", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<BookedRoom>bookings;
    
    @OneToMany(mappedBy="room", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Review> reviews;

    public Long getId() {
        return id;
    }

    public Blob getPhoto() {
        return photo;
    }

    public List<BookedRoom> getBookings() {
        return bookings;
    }

    public String getRoomType() {
        return roomType;
    }

    public BigDecimal getRoomPrice() {
        return roomPrice;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public void setRoomPrice(BigDecimal roomPrice) {
        this.roomPrice = roomPrice;
    }

    public void setBookings(List<BookedRoom> bookings) {
        this.bookings = bookings;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public void setBooked(boolean booked) {
        isBooked = booked;
    }

    public void setPhoto(Blob photo) {
        this.photo = photo;
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
    
    public List<Review> getReviews() {
        return reviews;
    }
    
    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public Room( String roomType, BigDecimal roomPrice, Blob photo) {
        this.roomType = roomType;
        this.roomPrice = roomPrice;
        this.photo = photo;
    }

    public Room() {
        this.bookings=new ArrayList<>();
    }
    public void addBooking(BookedRoom booking){
        if(bookings==null){
            bookings=new ArrayList<>();
        }
        bookings.add(booking);
        booking.setRoom(this);
        isBooked=true;
        String bookingCode = RandomStringUtils.randomNumeric(10);
        booking.setBookingConfirmationCode(bookingCode);
    }

}
