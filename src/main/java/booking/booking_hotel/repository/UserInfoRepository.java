package booking.booking_hotel.repository;

import booking.booking_hotel.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface    UserInfoRepository extends JpaRepository<UserInfo, Long> {
    @Query("SELECT u FROM UserInfo u LEFT JOIN FETCH u.roles WHERE u.name = :username")
    Optional<UserInfo> findByName(@Param("username") String username);

    UserInfo findByEmail(String email);
}
