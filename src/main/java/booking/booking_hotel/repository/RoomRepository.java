package booking.booking_hotel.repository;

import booking.booking_hotel.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;


public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT r.roomType FROM Room r")
    List<String> findDistinctRoomTypes();

    @Query("SELECT r FROM Room r WHERE r.roomType = :roomType AND r.id NOT IN " +
            "(SELECT br.room.id FROM BookedRoom br WHERE " +
            "(br.checkInDate <= :checkOutDate AND br.checkoutDate >= :checkInDate))")
    List<Room> findAvailableRoomsByDatesAndType(LocalDate checkInDate, LocalDate checkOutDate, String roomType);
}
