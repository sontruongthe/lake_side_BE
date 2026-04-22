package booking.booking_hotel.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class ChatRequest {
    private String message;
    @JsonIgnore
    private String userId;
    private String sessionId;
}
