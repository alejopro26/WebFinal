package chatnexus.security;

import chatnexus.model.ChatRoom;
import chatnexus.model.User;
import chatnexus.model.Channel;
import chatnexus.service.ChannelService;
import chatnexus.service.ModerationService;
import chatnexus.service.ChannelAclService;
import chatnexus.repository.ChatRoomRepository;
import chatnexus.repository.UserRepository;
import chatnexus.service.RoomMembershipService;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class SubscribeAuthChannelInterceptor implements ChannelInterceptor {

    private final RoomMembershipService membershipService;
    private final ChannelService channelService;
    private final ChannelAclService channelAclService;
    private final ModerationService moderationService;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    public SubscribeAuthChannelInterceptor(RoomMembershipService membershipService,
                                           ChatRoomRepository chatRoomRepository,
                                           UserRepository userRepository,
                                           ChannelService channelService,
                                           ChannelAclService channelAclService,
                                           ModerationService moderationService) {
        this.membershipService = membershipService;
        this.chatRoomRepository = chatRoomRepository;
        this.userRepository = userRepository;
        this.channelService = channelService;
        this.channelAclService = channelAclService;
        this.moderationService = moderationService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            if (destination != null && destination.startsWith("/topic/rooms/")) {
                String idStr = destination.substring("/topic/rooms/".length());
                try {
                    Long roomId = Long.valueOf(idStr);
                    String principal = accessor.getUser() != null ? accessor.getUser().getName() : null;
                    if (principal == null) throw new RuntimeException("no auth");
                    User user = userRepository.findByEmail(principal).orElseThrow();
                    ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow();
                    if (!membershipService.isMember(user, room)) throw new RuntimeException("no member");
                } catch (RuntimeException ex) {
                    throw ex;
                }
            }
            if (destination != null && destination.startsWith("/topic/dm/")) {
                String idStr = destination.substring("/topic/dm/".length());
                Long userId = Long.valueOf(idStr);
                String principal = accessor.getUser() != null ? accessor.getUser().getName() : null;
                if (principal == null) throw new RuntimeException("no auth");
                User user = userRepository.findByEmail(principal).orElseThrow();
                if (!user.getId().equals(userId)) throw new RuntimeException("No autorizado");
            }
            if (destination != null && destination.startsWith("/topic/channels/")) {
                String idStr = destination.substring("/topic/channels/".length());
                Long channelId = Long.valueOf(idStr);
                String principal = accessor.getUser() != null ? accessor.getUser().getName() : null;
                if (principal == null) throw new RuntimeException("no auth");
                User user = userRepository.findByEmail(principal).orElseThrow();
                Channel ch = channelService.get(channelId);
                if (ch == null) throw new RuntimeException("Canal no encontrado");
                if (moderationService.isBanned(user, ch.getServer())) throw new RuntimeException("Baneado");
                if (!channelAclService.canSubscribe(user, ch)) throw new RuntimeException("No autorizado");
            }
        }
        return message;
    }
}