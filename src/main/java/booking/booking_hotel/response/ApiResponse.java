package booking.booking_hotel.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Data

public class ApiResponse<T> {
    private String message;
    private String status;
    private T data;
    private int code;

    public ApiResponse(String message, int code, T data, String status) {
        this.message = message;
        this.code = code;
        this.data = data;
        this.status = status;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("sucess", HttpStatus.OK.value(),data,"SUCCESS");
    }
    public static <T> ApiResponse<T> error(int code, T data, String message) {
        return new ApiResponse<>(message, code, data, "FAIL");
    }

}
