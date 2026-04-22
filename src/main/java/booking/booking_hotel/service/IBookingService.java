package booking.booking_hotel.service;

import booking.booking_hotel.model.BookedRoom;

import java.util.List;

public interface IBookingService {
    void cancelBooking(Long bookingId);

    String saveBooking(Long roomId, BookedRoom bookingRequest);

    BookedRoom findByBookingConfimationCode(String confimationCode);

    List<BookedRoom> getAllBookings();
}
