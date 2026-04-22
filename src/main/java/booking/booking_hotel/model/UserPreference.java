package booking.booking_hotel.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_preferences")
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId; // email hoặc username

    private String preferredRoomType;     // "SUITE", "DELUXE", "STANDARD"
    private String preferredPriceRange;  // "budget", "mid", "luxury"
    private String preferredCheckInTime; // "morning", "afternoon", "evening"
    private Integer totalBookings;       // đếm số lần đặt phòng
    private String lastBookedRoomType;

    @Column(columnDefinition = "TEXT")
    private String notes; // AI ghi chú thêm về user

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // getters, setters
}
