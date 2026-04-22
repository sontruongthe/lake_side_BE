package booking.booking_hotel.exception;

import booking.booking_hotel.response.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ApiResponse<?> handleNotFound(ResourceNotFoundException ex) {
        return ApiResponse.error(404, null, ex.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ApiResponse<?> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return ApiResponse.error(400, null, ex.getMessage());
    }
}
