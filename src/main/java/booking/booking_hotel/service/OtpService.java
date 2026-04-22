package booking.booking_hotel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final SmsService smsService;
    private static final SecureRandom OTP_RANDOM = new SecureRandom();

    // Lưu OTP tạm trong memory (sau này thay bằng Redis)
    private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();

    // Tạo và gửi OTP
    public boolean sendOtp(String phone) {
        String otp = String.format("%06d", OTP_RANDOM.nextInt(1_000_000));
        boolean sent = smsService.sendOtp(phone, otp);
        if (sent) {
            otpStore.put(phone, new OtpData(otp, LocalDateTime.now().plusMinutes(5)));
        }
        return sent;
    }

    // Xác thực OTP
    public boolean verifyOtp(String phone, String inputOtp) {
        OtpData data = otpStore.get(phone);

        // Không tìm thấy hoặc hết hạn
        if (data == null || LocalDateTime.now().isAfter(data.expiredAt())) {
            otpStore.remove(phone);
            return false;
        }

        // Đúng OTP → xóa khỏi store (chỉ dùng 1 lần)
        if (data.otp().equals(inputOtp)) {
            otpStore.remove(phone);
            return true;
        }

        return false;
    }

    public record OtpData(String otp, LocalDateTime expiredAt) {
    }
}