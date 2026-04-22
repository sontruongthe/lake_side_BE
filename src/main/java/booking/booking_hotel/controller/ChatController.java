package booking.booking_hotel.controller;

import booking.booking_hotel.model.ChatRequest;
import booking.booking_hotel.model.ChatResponse;
import booking.booking_hotel.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/AI")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest chatRequest,
                                             Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ChatResponse("Bạn cần đăng nhập để sử dụng chat cá nhân hóa."));
        }

        // Always trust authenticated identity over userId from client payload.
        chatRequest.setUserId(authentication.getName());

        ChatResponse response = chatService.processChat(chatRequest);
        if (response.getReply().startsWith("Lỗi khi xử lý yêu cầu:")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        return ResponseEntity.ok(response);
    }
}