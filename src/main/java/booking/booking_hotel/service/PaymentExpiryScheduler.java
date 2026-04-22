package booking.booking_hotel.service;

import booking.booking_hotel.model.BookedRoom;
import booking.booking_hotel.model.Payment;
import booking.booking_hotel.repository.BookingRepository;
import booking.booking_hotel.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

// Tạo file mới: PaymentExpiryScheduler.java
@Component
@RequiredArgsConstructor
public class PaymentExpiryScheduler {
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    @Scheduled(fixedDelay = 60000)
    public void cancelUnpaidBookings() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);
        List<Payment> expiredPayments = paymentRepository
                .findByPaymentStatusAndCreatedAtBefore("PENDING", cutoff);

        for (Payment p : expiredPayments) {
            p.setPaymentStatus("EXPIRED");
            paymentRepository.save(p);

            BookedRoom booking = p.getBookedRoom();
            booking.setPaymentStatus("CANCELLED");
            bookingRepository.save(booking);
        }
    }
}