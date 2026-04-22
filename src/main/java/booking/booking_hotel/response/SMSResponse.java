package booking.booking_hotel.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class SMSResponse {
    private String phone;
    private String guestName;
    private String roomType;
    private BigDecimal price;
    private String checkIn;
    private String checkOut;
    private String confirmCode;
}

