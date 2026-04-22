package booking.booking_hotel.repository;

import booking.booking_hotel.model.ConversationMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {
    List<ConversationMessage> findTop10ByUserIdAndSessionIdOrderByCreatedAtAsc(String userId, String sessionId);
}
