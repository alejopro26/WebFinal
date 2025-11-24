package chatnexus.controller;

import chatnexus.model.*;
import chatnexus.repository.ChannelAclRepository;
import chatnexus.service.ChannelService;
import chatnexus.service.ServerMembershipService;
import chatnexus.service.ServerService;
import chatnexus.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/channels/{channelId}/acl")
public class ChannelAclController {

    private final ChannelService channelService;
    private final ChannelAclRepository aclRepo;
    private final ServerMembershipService membershipService;
    private final UserRepository userRepository;
    private final chatnexus.service.ChannelAclService channelAclService;
    private final chatnexus.service.AuditService auditService;

    public ChannelAclController(ChannelService channelService,
                                ChannelAclRepository aclRepo,
                                ServerMembershipService membershipService,
                                UserRepository userRepository,
                                chatnexus.service.ChannelAclService channelAclService,
                                chatnexus.service.AuditService auditService) {
        this.channelService = channelService;
        this.aclRepo = aclRepo;
        this.membershipService = membershipService;
        this.userRepository = userRepository;
        this.channelAclService = channelAclService;
        this.auditService = auditService;
    }

    @PostMapping
    public ResponseEntity<ChannelAcl> setAcl(@PathVariable Long channelId,
                                             @RequestParam ServerRole role,
                                             @RequestParam boolean canSend,
                                             @RequestParam boolean canSubscribe,
                                             @RequestParam(defaultValue = "false") boolean canManage,
                                             Authentication authentication) {
        Channel channel = channelService.get(channelId);
        if (channel == null) return ResponseEntity.notFound().build();
        Server server = channel.getServer();
        User user = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();
        boolean allowed = membershipService.isOwner(user, server) || channelAclService.canManage(user, channel);
        if (!allowed) return ResponseEntity.status(403).build();

        ChannelAcl acl = aclRepo.findByChannelAndRole(channel, role).orElseGet(() -> {
            ChannelAcl a = new ChannelAcl();
            a.setChannel(channel);
            a.setRole(role);
            return a;
        });
        acl.setCanSend(canSend);
        acl.setCanSubscribe(canSubscribe);
        acl.setCanManage(canManage);
        ChannelAcl saved = aclRepo.save(acl);
        auditService.record("ACL", "SET", user.getEmail(), String.valueOf(channelId), "role=" + role.name());
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<java.util.List<ChannelAcl>> list(@PathVariable Long channelId) {
        Channel channel = channelService.get(channelId);
        if (channel == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(aclRepo.findAll().stream()
                .filter(a -> a.getChannel().getId().equals(channelId))
                .collect(java.util.stream.Collectors.toList()));
    }
}