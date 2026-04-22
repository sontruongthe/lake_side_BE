package booking.booking_hotel.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
@Entity
@Setter
@Getter
@AllArgsConstructor
public class BookedRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @Column(name ="check_in")
    private LocalDate checkInDate;

    @Column(name ="check_out")
    private  LocalDate checkoutDate;

    @Column(name ="guest_FullName")
    private String guestFullName;

    @Column(name ="guest_Email")
    private  String guestEmail;

    @Column(name ="adults")
    private int numOfAdults;


    @Column(name ="children")
    private int numOfChildren;

    @Column(name ="total_guest")
    private int totalNumOfGuest;

    @Column(name ="confirmation_Code")
    private String bookingConfirmationCode;
    
    @Column(name = "payment_status")
    private String paymentStatus = "UNPAID"; // UNPAID, PAID, REFUNDED

    @ManyToOne(fetch =FetchType.LAZY )
    @JoinColumn(name ="room_id")
    private  Room room;

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
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

    public String getGuestFullName() {
        return guestFullName;
    }

    public void setGuestFullName(String guestFullName) {
        this.guestFullName = guestFullName;
    }

    public String getGuestEmail() {
        return guestEmail;
    }

    public void setGuestEmail(String guestEmail) {
        this.guestEmail = guestEmail;
    }

    public int getNumOfChildren() {
        return numOfChildren;
    }

    public int getNumOfAdults() {
        return numOfAdults;
    }

    public int getTotalNumOfGuest() {
        return totalNumOfGuest;
    }

    public void setTotalNumOfGuest(int totalNumOfGuest) {
        this.totalNumOfGuest = totalNumOfGuest;
    }

    public String getBookingConfirmationCode() {
        return bookingConfirmationCode;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) { // Thêm setter cho room
        this.room = room;
    }
    private void calculateTotalNumberOfGuest(){
        this.totalNumOfGuest=this.numOfAdults+numOfChildren;
    }

    public void setNumOfChildren(int numOfChildren) {
        this.numOfChildren = numOfChildren;
        calculateTotalNumberOfGuest();
    }

    public void setNumOfAdults(int numOfAdults) {
        this.numOfAdults = numOfAdults;
        calculateTotalNumberOfGuest();
    }

    public BookedRoom() {
    }

    public BookedRoom(String bookingConfirmationCode) {
        this.bookingConfirmationCode = bookingConfirmationCode;
    }

    public void setBookingConfirmationCode(String bookingConfirmationCode) {
        this.bookingConfirmationCode = bookingConfirmationCode;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
