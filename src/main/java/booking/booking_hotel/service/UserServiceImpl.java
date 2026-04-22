package booking.booking_hotel.service;

import booking.booking_hotel.exception.UserAlreadyExistsException;
import booking.booking_hotel.model.UserInfo;
import booking.booking_hotel.repository.UserInfoRepository;
import booking.booking_hotel.request.UserRequest;
import lombok.RequiredArgsConstructor;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private  UserInfoRepository userInfoRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void dangki(UserRequest request) {
        UserInfo userInfo = userInfoRepository.findByEmail(request.getEmail());
        if (userInfo == null) {
            UserInfo newUser = new UserInfo();
            newUser.setEmail(request.getEmail());
            newUser.setPassword(passwordEncoder.encode(request.getPassword()));
            newUser.setName(request.getName());
            userInfoRepository.save(newUser);
        } else {
            throw new UserAlreadyExistsException("Email already exists: " + request.getEmail());
        }
    }
}
