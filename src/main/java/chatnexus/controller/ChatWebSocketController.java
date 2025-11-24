package chatnexus.controller;

import chatnexus.dto.MessageDTO;
import chatnexus.model.ChatRoom;
import chatnexus.model.Message;
import chatnexus.model.User;
import chatnexus.service.ChatRoomService;
import chatnexus.service.MessageService;
import chatnexus.service.RoomMembershipService;
import chatnexus.repository.UserRepository;

import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomService chatRoomService;
    private final MessageService messageService;
    private final RoomMembershipService membershipService;
    private final UserRepository userRepository;
    private final ConcurrentHashMap<String, Long> rateLimit = new ConcurrentHashMap<>();

    public ChatWebSocketController(SimpMessagingTemplate messagingTemplate,
                                   ChatRoomService chatRoomService,
                                   MessageService messageService,
                                   UserRepository userRepository,
                                   RoomMembershipService membershipService) {
        this.messagingTemplate = messagingTemplate;
        this.chatRoomService = chatRoomService;
        this.messageService = messageService;
        this.userRepository = userRepository;
        this.membershipService = membershipService;
    }

    @MessageMapping("/chat.send/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId,
                            MessageDTO messageDTO,
                            Authentication authentication) {

        // Obtener usuario autenticado
        String principal = authentication.getName();
        User sender = userRepository.findByEmail(principal)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Obtener sala
        ChatRoom room = chatRoomService.getRoomById(roomId);
        if (room == null) {
            throw new RuntimeException("Sala no encontrada");
        }

        if (!membershipService.isMember(sender, room)) {
            throw new RuntimeException("No autorizado");
        }

        String key = sender.getId() + ":" + roomId;
        long now = System.currentTimeMillis();
        long last = rateLimit.getOrDefault(key, 0L);
        if (now - last < 1000) {
            throw new RuntimeException("Demasiadas solicitudes");
        }
        rateLimit.put(key, now);

        // Guardar mensaje en la BD
        Message savedMessage = messageService.saveMessage(
                messageDTO.getContent(), sender, room
        );

        // Preparar DTO que será enviado al frontend
        MessageDTO outgoing = new MessageDTO();
        outgoing.setId(savedMessage.getId());
        outgoing.setRoomId(roomId);
        outgoing.setContent(savedMessage.getContent());
        outgoing.setSender(sender.getUsername());

        // Enviar a los usuarios suscritos
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + roomId,
                outgoing
        );
    }
}
