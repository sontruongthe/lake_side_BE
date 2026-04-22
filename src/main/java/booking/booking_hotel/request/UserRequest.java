package booking.booking_hotel.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data

public class UserRequest {
    private String name;
    private String password;
    private String email;
}
