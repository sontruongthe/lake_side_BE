package booking.booking_hotel.controller;

import booking.booking_hotel.config.UserInfoDetails;
import booking.booking_hotel.config.UserInfoDetailsService;
import booking.booking_hotel.model.RefreshToken;
import booking.booking_hotel.model.UserInfo;
import booking.booking_hotel.repository.RefreshTokenRepository;
import booking.booking_hotel.request.UserRequest;
import booking.booking_hotel.response.ApiResponse;
import booking.booking_hotel.service.JwtService;
import booking.booking_hotel.service.RefreshTokenService;
import booking.booking_hotel.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("api")
public class UserController {
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final UserInfoDetailsService userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;
    @PostMapping("/dangki")
    public ResponseEntity<ApiResponse<Void>> dangki(@RequestBody UserRequest request) {
        userService.dangki(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    @PostMapping("/dangnhap")
    public ResponseEntity<ApiResponse<Object>> dangnhap(@RequestBody UserRequest request) {
        if (request.getName() == null || request.getPassword() == null) {
            Map<String,String> map = new HashMap<>();
            map.put("message","Tên và mật khẩu không được bỏ trống");
            return  ResponseEntity.ok(ApiResponse.success(map));
        }
        // Thực hiện xác thực bằng AuthenticationManager của Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getName(),
                        request.getPassword()
                )
        );
        // Dùng thông tin đã xác thực để tránh truy vấn DB lần 2
        Object principal = authentication.getPrincipal();
        List<String> roles = new ArrayList<>();
        List<String> permissions = new ArrayList<>();

        if (principal instanceof UserInfoDetails userDetails) {
            roles = new ArrayList<>(userDetails.getRoles());

        } else {

        }
        // Tạo JWT token từ username
        String jwt = jwtService.generateToken(request.getName(),permissions);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(request.getName());

        Map<String,Object> map = new HashMap<>();
        map.put("jwt",jwt);
        map.put("refreshToken",refreshToken);
        map.put("roles",roles);

        return ResponseEntity.ok(ApiResponse.success(map));

    }
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
        String requestToken = request.get("refreshToken");

        RefreshToken refreshToken = refreshTokenService.verifyToken(requestToken);
        UserInfo user = refreshToken.getUser();

        UserInfoDetails userDetails = (UserInfoDetails) userDetailsService
                .loadUserByUsername(user.getName());

        List<String> roles = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList());

        String newAccessToken = jwtService.generateToken(user.getName(), roles);

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken,
                "refreshToken", requestToken // giữ nguyên refreshToken cũ
        ));
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        String token = request.get("refreshToken");
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshTokenRepository::delete);
        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
    }

}
