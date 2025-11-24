package chatnexus.security;

import chatnexus.service.PresenceService;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class PresenceChannelInterceptor implements ChannelInterceptor {

    private final PresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;

    public PresenceChannelInterceptor(PresenceService presenceService, SimpMessagingTemplate messagingTemplate) {
        this.presenceService = presenceService;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String userId = accessor.getUser() != null ? accessor.getUser().getName() : null;
            if (userId != null) {
                presenceService.setOnline(userId);
                messagingTemplate.convertAndSend("/topic/presence", presenceService.onlineUsers());
            }
        }
        if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            String userId = accessor.getUser() != null ? accessor.getUser().getName() : null;
            if (userId != null) {
                presenceService.setOffline(userId);
                messagingTemplate.convertAndSend("/topic/presence", presenceService.onlineUsers());
            }
        }
        return message;
    }
}