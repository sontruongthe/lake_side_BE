package booking.booking_hotel.controller;

import booking.booking_hotel.exception.PhotoRetrievalExcetion;
import booking.booking_hotel.exception.ResourceNotFoundException;
import booking.booking_hotel.model.BookedRoom;
import booking.booking_hotel.model.Room;
import booking.booking_hotel.response.RoomResponse;
import booking.booking_hotel.service.BookingService;
import booking.booking_hotel.service.IRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/rooms")
public class RoomController {
    @Autowired

    private final IRoomService roomService;
    private final BookingService bookingService;
    @Autowired
    public RoomController(IRoomService roomService, BookingService bookingService) {
        this.roomService = roomService;
        this.bookingService = bookingService;
    }
    @PostMapping(value = "/add/new-room")
    public ResponseEntity<RoomResponse>addNewRoom(
            @RequestParam("photo") MultipartFile photo,
            @RequestParam("roomType") String roomType,
            @RequestParam("roomPrice") BigDecimal roomPrice) throws SQLException, IOException {
        Room saveRoom= roomService.addNewRoom(photo,roomType,roomPrice);
        RoomResponse response=new RoomResponse(saveRoom.getId(),saveRoom.getRoomType(),saveRoom.getRoomPrice());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/room/types")
    public List<String> getRoomTypes(){
        return roomService.getAllRoomTypes();
    }

    @GetMapping("/all-rooms")
    public ResponseEntity<List<RoomResponse>>getAllRooms() throws SQLException {
        List<Room>rooms=roomService.getAllRooms();
        List<RoomResponse>roomResponses=new ArrayList<>();
        for(Room room :rooms){
           byte[] photoBytes = roomService.getRoomPhotoByRoomId(room.getId());
           if(photoBytes != null && photoBytes.length>0){
               String base64Photo = Base64.getEncoder().encodeToString(photoBytes);
               RoomResponse roomResponse = getRoomResponse(room);
               roomResponse.setPhoto(base64Photo);
               roomResponses.add(roomResponse);
           }
        }
        return ResponseEntity.ok(roomResponses);
    }

    @DeleteMapping("/delete/room/{roomId}")
    public ResponseEntity<Void>deleteRoom(@PathVariable Long roomId){
        roomService.deleteRoom(roomId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/update/{roomId}")
    public ResponseEntity<RoomResponse>update(@PathVariable Long roomId,
                                              @RequestParam(required = false) String roomType,
                                              @RequestParam(required = false)BigDecimal roomPrice,
                                              @RequestParam(required = false) MultipartFile photo) throws IOException, SQLException {
       byte[] photoBytes = photo != null && !photo.isEmpty()?
               photo.getBytes() : roomService.getRoomPhotoByRoomId(roomId);
       Blob photoBlob = photoBytes!= null &&photoBytes.length>0 ? new SerialBlob(photoBytes):null;
       Room theRoom=roomService.updateRoom(roomId,roomType,roomPrice,photoBytes);
       theRoom.setPhoto(photoBlob);
       RoomResponse roomResponse= getRoomResponse(theRoom);
       return ResponseEntity.ok(roomResponse);
    }
    @GetMapping ("/room/{roomId}")
    public ResponseEntity<Optional<RoomResponse>> getRoomById(@PathVariable Long roomId){
        Optional<Room>theRoom=roomService.getRoomById(roomId);
        return theRoom.map(room ->{
            RoomResponse roomResponse =getRoomResponse(room);
            return ResponseEntity.ok(Optional.of(roomResponse));
        } ).orElseThrow(()->new ResourceNotFoundException("Room not found"));
    }

    @GetMapping("/available-rooms")
    public ResponseEntity<List<RoomResponse>>getAvailableRooms(
           @RequestParam("checkInDate")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
           @RequestParam("checkoutDate")  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDate checkoutDate,
           @RequestParam("roomType") String roomType) throws SQLException {
        List<Room>availableRooms=roomService.getAvailableRooms(checkInDate,checkoutDate,roomType);
        List<RoomResponse>roomResponses=new ArrayList<>();
        for(Room room : availableRooms){
            byte[]photoBytes = roomService.getRoomPhotoByRoomId(room.getId());
            if(photoBytes != null && photoBytes.length>0){
                String photoBase64 = Base64.getEncoder().encodeToString(photoBytes);
                RoomResponse roomResponse = getRoomResponse(room);
                roomResponse.setPhoto(photoBase64);
                roomResponses.add(roomResponse);

            }
        }
        if(roomResponses.isEmpty()){
            return ResponseEntity.noContent().build();
        }else{
            return ResponseEntity.ok(roomResponses);
        }
    }

    private RoomResponse getRoomResponse(Room room) {
        List<BookedRoom> bookings = getAllBookingsByRoomId(room.getId());

        // Kiểm tra null trước khi gọi stream()
      /*  List<BookingResponse> bookingInfo = (bookings != null) ? bookings.stream()
                .map(booking -> new BookingResponse(
                        booking.getBookingId(),
                        booking.getCheckInDate(),
                        booking.getCheckoutDate(),
                        booking.getBookingConfirmationCode()))
                .toList()
                : new ArrayList<>(); // Nếu null thì trả về danh sách rỗng */

        byte[] photoBytes = null;
        Blob photoBlob = room.getPhoto();
        if (photoBlob != null) {
            try {
                photoBytes = photoBlob.getBytes(1, (int) photoBlob.length());
            } catch (SQLException e) {
                throw new PhotoRetrievalExcetion("Error retrieving photo");
            }
        }

        RoomResponse roomResponse = new RoomResponse(
                room.getRoomType(),
                room.getRoomPrice(),
                room.isBooked(),
                room.getId(),
                photoBytes
                //bookingInfo
        );
        
        // Set rating data
        roomResponse.setAverageRating(room.getAverageRating());
        roomResponse.setReviewCount(room.getReviewCount());
        
        return roomResponse;
    }




    private List<BookedRoom> getAllBookingsByRoomId(Long roomId) {
        return bookingService.getALlBookingsByRoomId(roomId);
    }
}
