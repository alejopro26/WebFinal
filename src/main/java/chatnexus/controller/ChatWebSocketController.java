package chatnexus.controller;

import chatnexus.dto.MessageDTO;
import chatnexus.model.ChatRoom;
import chatnexus.model.Message;
import chatnexus.model.User;
import chatnexus.service.ChatRoomService;
import chatnexus.service.MessageService;
import chatnexus.repository.UserRepository;

import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;

@Controller
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomService chatRoomService;
    private final MessageService messageService;
    private final UserRepository userRepository;

    public ChatWebSocketController(SimpMessagingTemplate messagingTemplate,
                                   ChatRoomService chatRoomService,
                                   MessageService messageService,
                                   UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.chatRoomService = chatRoomService;
        this.messageService = messageService;
        this.userRepository = userRepository;
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

        // Guardar mensaje en la BD
        Message savedMessage = messageService.saveMessage(
                messageDTO.getContent(), sender, room
        );

        // Preparar DTO que será enviado al frontend
        MessageDTO outgoing = new MessageDTO();
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
