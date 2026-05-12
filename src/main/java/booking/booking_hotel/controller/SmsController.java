package booking.booking_hotel.controller;

import booking.booking_hotel.response.SMSResponse;
import booking.booking_hotel.service.OtpService;
import booking.booking_hotel.service.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
public class SmsController {

    private final OtpService otpService;
    private final SmsService smsService;
    private static final DateTimeFormatter BOOKING_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

//    @PostMapping("/otp/send")
//    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> body) {
//        String phone = body.get("phone");
//        if (phone == null || phone.isBlank()) {
//            return ResponseEntity.badRequest().body(Map.of("message", "Thiếu số điện thoại!"));
//        }
//
//        boolean sent = otpService.sendOtp(phone);
//        if (!sent) {
//            return ResponseEntity.status(502).body(Map.of(
//                    "message", "Gửi OTP thất bại từ nhà cung cấp SMS, vui lòng thử lại."
//            ));
//        }
//
//        return ResponseEntity.ok(Map.of("message", "OTP đã gửi!"));
//    }
//
//    @PostMapping("/otp/verify")
//    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
//        boolean valid = otpService.verifyOtp(body.get("phone"), body.get("otp"));
//        if (valid) return ResponseEntity.ok(Map.of("message", "Xác thực thành công!"));
//        return ResponseEntity.badRequest().body(Map.of("message", "OTP sai hoặc hết hạn!"));
//    }

    @PostMapping("/booking/confirmation")
    public ResponseEntity<?> sendBookingConfirmation(@RequestBody SMSResponse body) {
//        String phone = body.get("phone");
//        String guestName = body.get("guestName");
//        String roomType = body.get("roomType");
//        String bookingDate = body.get("bookingDate");
//        String checkIn = body.get("checkIn");
//        String checkOut = body.get("checkOut");
//        String confirmCode = body.get("confirmCode");

        if (isBlank(body.getPhone()) || isBlank(body.getGuestName()) || isBlank(body.getRoomType())
                || isBlank(body.getCheckIn()) || isBlank(body.getCheckOut()) || isBlank(body.getConfirmCode())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Thiếu dữ liệu gửi SMS xác nhận đặt phòng!"
            ));
        }

        boolean sent = smsService.sendBookingConfirmation(
               body
        );

        if (!sent) {
            return ResponseEntity.status(502).body(Map.of(
                    "message", "Gửi SMS xác nhận đặt phòng thất bại, vui lòng thử lại."
            ));
        }

        String resolvedBookingDate = isBlank(body.getBookingDate())
            ? LocalDate.now().format(BOOKING_DATE_FORMAT)
            : body.getBookingDate();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Đã gửi SMS xác nhận đặt phòng thành công!");
        response.put("phone", body.getPhone());
        response.put("guestName", body.getGuestName());
        response.put("roomType", body.getRoomType());
        response.put("bookingDate", resolvedBookingDate);
        response.put("checkIn", body.getCheckIn());
        response.put("checkOut", body.getCheckOut());
        response.put("confirmCode", body.getConfirmCode());
        return ResponseEntity.ok(response);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}