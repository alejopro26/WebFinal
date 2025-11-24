package chatnexus.controller;

import chatnexus.dto.MessageDTO;
import chatnexus.model.DirectMessage;
import chatnexus.model.User;
import chatnexus.repository.UserRepository;
import chatnexus.service.DirectMessageService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
public class DirectMessageWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final DirectMessageService dmService;
    private final UserRepository userRepository;

    public DirectMessageWebSocketController(SimpMessagingTemplate messagingTemplate,
                                            DirectMessageService dmService,
                                            UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.dmService = dmService;
        this.userRepository = userRepository;
    }

    @MessageMapping("/dm.send/{receiverId}")
    public void send(@DestinationVariable Long receiverId,
                     MessageDTO body,
                     Authentication authentication) {
        User sender = userRepository.findByEmail(authentication.getName()).orElseThrow();
        User receiver = userRepository.findById(receiverId).orElseThrow();

        DirectMessage saved = dmService.send(body.getContent(), sender, receiver);

        MessageDTO dto = new MessageDTO();
        dto.setId(saved.getId());
        dto.setContent(saved.getContent());
        dto.setSender(sender.getUsername());

        messagingTemplate.convertAndSend("/topic/dm/" + receiver.getId(), dto);
        messagingTemplate.convertAndSend("/topic/dm/" + sender.getId(), dto);
    }
}