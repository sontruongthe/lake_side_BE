package booking.booking_hotel.service;

import booking.booking_hotel.exception.InvalidBookingRequestException;
import booking.booking_hotel.model.BookedRoom;
import booking.booking_hotel.model.Payment;
import booking.booking_hotel.model.Room;
import booking.booking_hotel.repository.BookingRepository;
import booking.booking_hotel.repository.PaymentRepository;
import booking.booking_hotel.repository.ReviewRepository;
import booking.booking_hotel.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService implements IBookingService {
    private final BookingRepository bookingRepository;
    private final IRoomService roomService;
    private final RoomRepository roomRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;

    public BookingService(BookingRepository bookingRepository, IRoomService roomService, RoomRepository roomRepository,
                          PaymentRepository paymentRepository, ReviewRepository reviewRepository) {
        this.bookingRepository = bookingRepository;
        this.roomService = roomService;
        this.roomRepository = roomRepository;
        this.paymentRepository = paymentRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public List<BookedRoom> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<BookedRoom> getALlBookingsByRoomId(Long roomId) {
        return bookingRepository.findByRoomId(roomId);
    }

    @Override
    public void cancelBooking(Long bookingId) {
        BookedRoom booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));
        
        // Delete associated Payment record if exists
        paymentRepository.findByBookedRoom_BookingId(bookingId).ifPresent(paymentRepository::delete);
        
        // Delete all associated Review records
        reviewRepository.deleteByBooking_BookingId(bookingId);
        
        // Now delete the booking
        bookingRepository.delete(booking);
    }

    @Override
    public String saveBooking(Long roomId, BookedRoom bookingRequest) {
        if (bookingRequest.getCheckInDate().isAfter(bookingRequest.getCheckoutDate())) {
            throw new InvalidBookingRequestException("Check-in date must come before check-out date");
        }

        Room room = roomService.getRoomById(roomId).get();
        List<BookedRoom> existingBookings = room.getBookings();
        boolean roomIsAvailable = roomIsAvailable(bookingRequest, existingBookings);
        if (roomIsAvailable) {
            room.addBooking(bookingRequest);  // Set isBooked = true trong Room entity
            bookingRepository.save(bookingRequest);  // Save booking vào database
            roomRepository.save(room);  // QUAN TRỌNG: Save room để update isBooked vào database!
            //  TẠO PAYMENT
            Payment payment = new Payment();
            payment.setBookedRoom(bookingRequest);
            payment.setPaymentMethod("SEPAY");
            payment.setPaymentStatus("PENDING");

            // ví dụ: BOOKING_ + id
            String content = "PAY" + bookingRequest.getBookingId();
            payment.setTransferContent(content);

            payment.setAmount(room.getRoomPrice()); // hoặc tính theo ngày
            paymentRepository.save(payment);
        } else {
            throw new InvalidBookingRequestException("Sorry, This room is not available for the selected dates");
        }
        return bookingRequest.getBookingConfirmationCode();
    }

    @Override
    public BookedRoom findByBookingConfimationCode(String confimationCode) {
        return bookingRepository.findByBookingConfirmationCode(confimationCode);
    }

    private boolean roomIsAvailable(BookedRoom bookingRequest, List<BookedRoom> existingBookings) {
        return existingBookings.stream()
                .noneMatch(existingBooking ->
                        bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckoutDate()) &&
                                bookingRequest.getCheckoutDate().isAfter(existingBooking.getCheckInDate())
                );
    }
}
