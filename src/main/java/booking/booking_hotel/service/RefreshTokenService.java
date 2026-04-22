package booking.booking_hotel.service;

import booking.booking_hotel.model.RefreshToken;
import booking.booking_hotel.model.UserInfo;
import booking.booking_hotel.repository.RefreshTokenRepository;
import booking.booking_hotel.repository.UserInfoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.refresh.expiration:604800000}") // 7 ngày
    private long REFRESH_EXPIRATION;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserInfoRepository userInfoRepository;

    @Transactional
    public RefreshToken createRefreshToken(String username) {
        UserInfo user = userInfoRepository.findByName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Tìm token cũ → update, không có → tạo mới
        RefreshToken refreshToken = refreshTokenRepository
                .findByUser(user)
                .orElse(new RefreshToken());

        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));

        return refreshTokenRepository.save(refreshToken);
    }
    public RefreshToken verifyToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token không tồn tại"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token đã hết hạn, vui lòng đăng nhập lại");
        }

        return refreshToken;
    }
}