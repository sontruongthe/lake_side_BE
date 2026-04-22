package booking.booking_hotel.repository;

import booking.booking_hotel.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByBookedRoom_BookingId(Long bookingId);

    List<Payment> findByPaymentStatusAndCreatedAtBefore(String pending, LocalDateTime cutoff);
}
