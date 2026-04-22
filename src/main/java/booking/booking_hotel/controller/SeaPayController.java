package booking.booking_hotel.controller;

import booking.booking_hotel.model.Payment;
import booking.booking_hotel.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sepay")
@RequiredArgsConstructor
public class SeaPayController {
    private final PaymentRepository paymentRepository;

    @GetMapping("/payment/{bookingId}/qr")
    public Map<String, String> generateSepayQR(@PathVariable Long bookingId) {

        Payment payment = paymentRepository
                .findByBookedRoom_BookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        String account = "96247BOOKING123";
        String bank = "BIDV"; // mã bank
        String amount = payment.getAmount().toString();
        String description = payment.getPaymentId().toString();


        String qrUrl = "https://qr.sepay.vn/img"
            + "?bank=" + bank
            + "&acc=" + account
            + "&template="
                + "&amount=" + amount
            + "&des=PAY" + description;

        Map<String, String> result = new HashMap<>();
        result.put("qrUrl", qrUrl);
        result.put("paymentId", payment.getPaymentId().toString());
        result.put("bookingId", bookingId.toString());

        return result;
    }

    @GetMapping("/payment/{paymentId}/status")
    public Map<String, Object> getPaymentStatus(@PathVariable Long paymentId) {
        Payment payment = paymentRepository
                .findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        Map<String, Object> result = new HashMap<>();
        result.put("paymentId", payment.getPaymentId());
        result.put("status", payment.getPaymentStatus());
        result.put("amount", payment.getAmount());
        result.put("bookingId", payment.getBookedRoom().getBookingId());

        return result;
    }
}
