package booking.booking_hotel.service;

import booking.booking_hotel.config.TwilioConfig;
import booking.booking_hotel.response.SMSResponse;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private final TwilioConfig twilioConfig;
    private static final DateTimeFormatter BOOKING_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public boolean sendOtp(String phoneNumber, String otp) {
        String message = "Ma OTP cua ban la: " + otp + ". Het han sau 5 phut. Khong chia se ma nay.";
        return sendSms(phoneNumber, message);
    }

    public boolean sendBookingConfirmation(SMSResponse body) {
        String resolvedBookingDate = isBlank(body.getBookingDate())
            ? LocalDate.now().format(BOOKING_DATE_FORMAT)
            : body.getBookingDate();

        String message = String.format(
            "LAKESIDE HOTEL\n"
                + "XAC NHAN DAT PHONG THANH CONG\n"
                + "Khach hang: %s\n"
                + "Loai phong: %s\n"
                + "Ngay dat: %s\n"
                + "Nhan phong: %s\n"
                + "Tra phong: %s\n"
                + "Ma xac nhan: %s\n"
                + "Cam on ban da dat phong!",
            body.getGuestName(), body.getRoomType(), resolvedBookingDate, body.getCheckIn(), body.getCheckOut(), body.getConfirmCode()
        );
        return sendSms(body.getPhone(), message);
    }

    public boolean sendBookingCancellation(String phoneNumber,
                                           String guestName, String confirmCode) {
        String message = String.format(
                "Chao %s! Dat phong %s da duoc huy thanh cong.",
                guestName, confirmCode
        );
        return sendSms(phoneNumber, message);
    }

    private boolean sendSms(String phoneNumber, String message) {
        try {
            if (isBlank(twilioConfig.getPhoneNumber())) {
                log.error("Twilio phone number is missing.");
                return false;
            }

            String formattedPhone = formatPhone(phoneNumber);

            Message sms = Message.creator(
                    new PhoneNumber(formattedPhone),
                    new PhoneNumber(twilioConfig.getPhoneNumber()),
                    message
            ).create();

            log.info("SMS sent to {} | SID: {}", formattedPhone, sms.getSid());
            return true;
        } catch (Exception e) {
            log.error("SMS failed to {}: {}", phoneNumber, e.getMessage());
            return false;
        }
    }

    // 0912345678 -> +84912345678
    private String formatPhone(String phone) {
        String normalized = phone == null ? "" : phone.replaceAll("[\\s-]", "");
        if (normalized.startsWith("0")) return "+84" + normalized.substring(1);
        if (normalized.startsWith("84")) return "+" + normalized;
        return normalized;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}