package booking.booking_hotel.response;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@AllArgsConstructor
@NoArgsConstructor

public class BookingResponse {
    private Long id;

    private LocalDate checkInDate;

    private  LocalDate checkoutDate;

    private String guestName;

    private  String guestEmail;

    private int NumOfAdults;

    private int NumOfChildren;

    private int totalNumOfGuest;

    private String bookingConfirmationCode;


    private RoomResponse room;


    public int getTotalNumOfGuest() {
        return totalNumOfGuest;
    }

    public void setTotalNumOfGuest(int totalNumOfGuest) {
        this.totalNumOfGuest = totalNumOfGuest;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getNumOfChildren() {
        return NumOfChildren;
    }

    public void setNumOfChildren(int numOfChildren) {
        NumOfChildren = numOfChildren;
    }

    public int getNumOfAdults() {
        return NumOfAdults;
    }

    public void setNumOfAdults(int numOfAdults) {
        NumOfAdults = numOfAdults;
    }

    public String getGuestEmail() {
        return guestEmail;
    }

    public void setGuestEmail(String guestEmail) {
        this.guestEmail = guestEmail;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckoutDate() {
        return checkoutDate;
    }

    public void setCheckoutDate(LocalDate checkoutDate) {
        this.checkoutDate = checkoutDate;
    }

    public RoomResponse getRoom() {
        return room;
    }

    public void setRoom(RoomResponse room) {
        this.room = room;
    }

    public String getBookingConfirmationCode() {
        return bookingConfirmationCode;
    }

    public void setBookingConfirmationCode(String bookingConfirmationCode) {
        this.bookingConfirmationCode = bookingConfirmationCode;
    }

    // Constructor cho getBookingResponse
    public BookingResponse(Long id, LocalDate checkInDate, LocalDate checkoutDate, String guestName, String guestEmail, int NumOfAdults, int NumOfChildren, String bookingConfirmationCode, int totalNumOfGuest) {
        this.id = id;
        this.checkInDate = checkInDate;
        this.checkoutDate = checkoutDate;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.NumOfAdults = NumOfAdults;
        this.NumOfChildren = NumOfChildren;
        this.bookingConfirmationCode = bookingConfirmationCode;
        this.totalNumOfGuest = totalNumOfGuest;
    }

    // Constructor đầy đủ với room
    public BookingResponse(Long id, LocalDate checkInDate, LocalDate checkoutDate, String guestName, String guestEmail, int NumOfAdults, int NumOfChildren, String bookingConfirmationCode, int totalNumOfGuest, RoomResponse room) {
        this.id = id;
        this.checkInDate = checkInDate;
        this.checkoutDate = checkoutDate;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.NumOfAdults = NumOfAdults;
        this.NumOfChildren = NumOfChildren;
        this.bookingConfirmationCode = bookingConfirmationCode;
        this.totalNumOfGuest = totalNumOfGuest;
        this.room = room;
    }
}
