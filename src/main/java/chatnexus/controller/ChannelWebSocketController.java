package chatnexus.controller;

import chatnexus.dto.MessageDTO;
import chatnexus.model.Channel;
import chatnexus.model.Server;
import chatnexus.model.User;
import chatnexus.repository.UserRepository;
import chatnexus.service.ChannelMessageService;
import chatnexus.service.ChannelService;
import chatnexus.service.ServerMembershipService;
import chatnexus.service.ModerationService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
public class ChannelWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChannelService channelService;
    private final ChannelMessageService messageService;
    private final ServerMembershipService membershipService;
    private final chatnexus.service.ChannelAclService channelAclService;
    private final ModerationService moderationService;
    private final UserRepository userRepository;

    public ChannelWebSocketController(SimpMessagingTemplate messagingTemplate,
                                      ChannelService channelService,
                                      ChannelMessageService messageService,
                                      ServerMembershipService membershipService,
                                      chatnexus.service.ChannelAclService channelAclService,
                                      ModerationService moderationService,
                                      UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.channelService = channelService;
        this.messageService = messageService;
        this.membershipService = membershipService;
        this.channelAclService = channelAclService;
        this.moderationService = moderationService;
        this.userRepository = userRepository;
    }

    @MessageMapping("/channel.send/{channelId}")
    public void send(@DestinationVariable Long channelId, MessageDTO dto, Authentication authentication) {
        User sender = userRepository.findByEmail(authentication.getName()).orElseThrow();
        Channel channel = channelService.get(channelId);
        if (channel == null) throw new RuntimeException("Canal no encontrado");
        Server server = channel.getServer();
        if (!membershipService.isMember(sender, server)) throw new RuntimeException("No autorizado");
        if (moderationService.isBanned(sender, server)) throw new RuntimeException("Baneado");
        if (moderationService.isMuted(sender, channel)) throw new RuntimeException("Muteado");
        if (!channelAclService.canSend(sender, channel)) throw new RuntimeException("No autorizado");
        var saved = messageService.save(dto.getContent(), sender, channel);
        MessageDTO out = new MessageDTO();
        out.setId(saved.getId());
        out.setContent(saved.getContent());
        out.setSender(sender.getUsername());
        messagingTemplate.convertAndSend("/topic/channels/" + channelId, out);
    }
}