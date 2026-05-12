package booking.booking_hotel.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SMSResponse {
   private String bookingDate;
    private String phone;
    private String guestName;
    private String roomType;
    private String checkIn;
    private String checkOut;
    private String confirmCode;



}

