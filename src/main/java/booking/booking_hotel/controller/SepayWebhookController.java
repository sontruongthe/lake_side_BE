package booking.booking_hotel.controller;

import booking.booking_hotel.model.BookedRoom;
import booking.booking_hotel.model.Payment;
import booking.booking_hotel.repository.BookingRepository;
import booking.booking_hotel.repository.PaymentRepository;
import booking.booking_hotel.response.SMSResponse;
import booking.booking_hotel.service.SmsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;


import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class SepayWebhookController {
    @Value("${sepay.webhook.token}")
    private String sepayWebhookToken;

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final SmsService smsService;

    public SepayWebhookController(PaymentRepository paymentRepository,
                                  BookingRepository bookingRepository,
                                  SmsService smsService) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.smsService = smsService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody Map<String, Object> payload, @RequestHeader("Authorization") String authHeader) {

        String content = (String) payload.get("content");
        Double amount = ((Number) payload.get("transferAmount")).doubleValue();
        String expectedToken = "Apikey " + sepayWebhookToken;
        if (!expectedToken.equals(authHeader)) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }

        //  tìm payment theo content
        Pattern pattern = Pattern.compile("PAY(\\d+)");
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) {
            return ResponseEntity.ok("NOT FOUND");
        }
        Long paymentId = Long.parseLong(matcher.group(1));
        Payment payment = paymentRepository.findById(paymentId).orElse(null);
        if (payment == null) {
            return ResponseEntity.ok("NOT FOUND");
        }

        //  tránh xử lý lại
        if ("SUCCESS".equals(payment.getPaymentStatus())) {
            return ResponseEntity.ok("ALREADY PAID");
        }

        //  check amount (rất quan trọng)
        if (payment.getAmount().doubleValue() != amount) {
            return ResponseEntity.badRequest().body("INVALID AMOUNT");
        }

        //  UPDATE PAYMENT
        payment.setPaymentStatus("SUCCESS");
        paymentRepository.save(payment);

        // UPDATE BOOKING
        BookedRoom booking = payment.getBookedRoom();
        booking.setPaymentStatus("PAID");
        bookingRepository.save(booking);

        // Send SMS only after payment success and only when customer phone exists.
        if (booking.getGuestPhone() != null && !booking.getGuestPhone().isBlank()) {
            SMSResponse smsBody = new SMSResponse();
            smsBody.setPhone(booking.getGuestPhone());
            smsBody.setBookingDate(String.valueOf(LocalDateTime.now()));
            smsBody.setCheckIn(String.valueOf(booking.getCheckInDate()));
            smsBody.setCheckOut(String.valueOf(booking.getCheckoutDate()));
            smsBody.setRoomType(booking.getRoom() != null ? booking.getRoom().getRoomType() : "N/A");
            smsBody.setConfirmCode(booking.getBookingConfirmationCode());
            smsBody.setGuestName(booking.getGuestFullName());
            smsService.sendBookingConfirmation(smsBody);
        }

        return ResponseEntity.ok("OK");
    }

}
