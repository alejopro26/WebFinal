package chatnexus.controller;

import chatnexus.dto.TypingDTO;
import chatnexus.model.ChatRoom;
import chatnexus.model.User;
import chatnexus.repository.UserRepository;
import chatnexus.service.ChatRoomService;
import chatnexus.service.RoomMembershipService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class TypingController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomService chatRoomService;
    private final RoomMembershipService membershipService;
    private final UserRepository userRepository;

    public TypingController(SimpMessagingTemplate messagingTemplate,
                            ChatRoomService chatRoomService,
                            RoomMembershipService membershipService,
                            UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.chatRoomService = chatRoomService;
        this.membershipService = membershipService;
        this.userRepository = userRepository;
    }

    @MessageMapping("/typing/{roomId}")
    public void typing(@DestinationVariable Long roomId, TypingDTO dto, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        ChatRoom room = chatRoomService.getRoomById(roomId);
        if (room == null) throw new RuntimeException("Sala no encontrada");
        if (!membershipService.isMember(user, room)) throw new RuntimeException("No autorizado");
        messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/typing", Map.of("user", user.getUsername(), "typing", dto.isTyping()));
    }
}