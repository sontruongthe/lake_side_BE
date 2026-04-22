package booking.booking_hotel.repository;

import booking.booking_hotel.model.BookedRoom;
import booking.booking_hotel.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository để truy vấn database cho Booking
 */
public interface BookingRepository extends JpaRepository<BookedRoom,Long> {

    // Tìm tất cả booking của 1 phòng theo roomId
    @Query("SELECT b FROM BookedRoom b WHERE b.room.id = :roomId")
    List<BookedRoom> findByRoomId(@Param("roomId") Long roomId);

    // Tìm booking theo mã xác nhận
    @Query("SELECT b FROM BookedRoom b WHERE b.bookingConfirmationCode = :code")
    BookedRoom findByBookingConfirmationCode(@Param("code") String confimationCode);
    
    // Tìm các booking đã hết hạn (ngày checkout < ngày được truyền vào)
    // VD: findByCheckoutDateBefore(hôm nay) -> tìm booking đã checkout
    // JOIN FETCH để load luôn Room, tránh LazyInitializationException
    @Query("SELECT b FROM BookedRoom b JOIN FETCH b.room WHERE b.checkoutDate < :date")
    List<BookedRoom> findByCheckoutDateBefore(@Param("date") LocalDate date);
    
    // Kiểm tra phòng có booking đang active không
    // Check xem phòng có khách đang ở (trong khoảng check-in và checkout) không
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
           "FROM BookedRoom b WHERE b.room = :room " +
           "AND b.checkInDate <= :checkInDate AND b.checkoutDate >= :checkoutDate")
    boolean existsByRoomAndCheckInDateLessThanEqualAndCheckoutDateGreaterThanEqual(
        @Param("room") Room room, 
        @Param("checkInDate") LocalDate checkInDate, 
        @Param("checkoutDate") LocalDate checkoutDate);
    
    // Tìm các booking có ngày check-in = ngày được truyền và chưa checkout
    // VD: tìm khách check-in hôm nay
    @Query("SELECT b FROM BookedRoom b " +
           "WHERE b.checkInDate = :checkInDate AND b.checkoutDate >= :checkoutDate")
    List<BookedRoom> findByCheckInDateAndCheckoutDateGreaterThanEqual(
        @Param("checkInDate") LocalDate checkInDate, 
        @Param("checkoutDate") LocalDate checkoutDate);
    
    // Tìm TẤT CẢ các booking đang active (hôm nay nằm trong khoảng check-in đến checkout)
    // VD: findByCheckInDateLessThanEqualAndCheckoutDateGreaterThanEqual(hôm nay, hôm nay)
    // -> Tìm booking mà check-in <= hôm nay VÀ checkout >= hôm nay (đang ở)
    // JOIN FETCH để load luôn Room, tránh LazyInitializationException
    @Query("SELECT b FROM BookedRoom b JOIN FETCH b.room " +
           "WHERE b.checkInDate <= :checkInDate AND b.checkoutDate >= :checkoutDate")
    List<BookedRoom> findByCheckInDateLessThanEqualAndCheckoutDateGreaterThanEqual(
        @Param("checkInDate") LocalDate checkInDate, 
        @Param("checkoutDate") LocalDate checkoutDate);
}
