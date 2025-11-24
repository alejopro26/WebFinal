package chatnexus.controller;

import chatnexus.model.*;
import chatnexus.repository.UserRepository;
import chatnexus.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mod")
public class ModerationController {

    private final ServerService serverService;
    private final ChannelService channelService;
    private final ServerMembershipService membershipService;
    private final ModerationService moderationService;
    private final UserRepository userRepository;
    private final chatnexus.service.AuditService auditService;

    public ModerationController(ServerService serverService,
                                ChannelService channelService,
                                ServerMembershipService membershipService,
                                ModerationService moderationService,
                                UserRepository userRepository,
                                chatnexus.service.AuditService auditService) {
        this.serverService = serverService;
        this.channelService = channelService;
        this.membershipService = membershipService;
        this.moderationService = moderationService;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @PostMapping("/servers/{serverId}/ban/{userId}")
    public ResponseEntity<?> ban(@PathVariable Long serverId, @PathVariable Long userId, Authentication authentication) {
        Server s = serverService.get(serverId);
        if (s == null) return ResponseEntity.notFound().build();
        User actor = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (actor == null) return ResponseEntity.status(401).build();
        if (!membershipService.isOwner(actor, s)) return ResponseEntity.status(403).build();
        User target = userRepository.findById(userId).orElse(null);
        if (target == null) return ResponseEntity.notFound().build();
        moderationService.ban(target, s);
        auditService.record("MOD", "BAN", actor.getEmail(), String.valueOf(serverId), String.valueOf(userId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/servers/{serverId}/unban/{userId}")
    public ResponseEntity<?> unban(@PathVariable Long serverId, @PathVariable Long userId, Authentication authentication) {
        Server s = serverService.get(serverId);
        if (s == null) return ResponseEntity.notFound().build();
        User actor = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (actor == null) return ResponseEntity.status(401).build();
        if (!membershipService.isOwner(actor, s)) return ResponseEntity.status(403).build();
        User target = userRepository.findById(userId).orElse(null);
        if (target == null) return ResponseEntity.notFound().build();
        moderationService.unban(target, s);
        auditService.record("MOD", "UNBAN", actor.getEmail(), String.valueOf(serverId), String.valueOf(userId));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/servers/{serverId}/ban/{userId}")
    public ResponseEntity<java.util.Map<String, Boolean>> isBanned(@PathVariable Long serverId, @PathVariable Long userId) {
        Server s = serverService.get(serverId);
        if (s == null) return ResponseEntity.notFound().build();
        User target = userRepository.findById(userId).orElse(null);
        if (target == null) return ResponseEntity.notFound().build();
        boolean banned = moderationService.isBanned(target, s);
        return ResponseEntity.ok(java.util.Map.of("banned", banned));
    }

    @PostMapping("/channels/{channelId}/mute/{userId}")
    public ResponseEntity<?> mute(@PathVariable Long channelId, @PathVariable Long userId, Authentication authentication) {
        Channel c = channelService.get(channelId);
        if (c == null) return ResponseEntity.notFound().build();
        Server s = c.getServer();
        User actor = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (actor == null) return ResponseEntity.status(401).build();
        if (!membershipService.isOwner(actor, s)) return ResponseEntity.status(403).build();
        User target = userRepository.findById(userId).orElse(null);
        if (target == null) return ResponseEntity.notFound().build();
        moderationService.mute(target, c);
        auditService.record("MOD", "MUTE", actor.getEmail(), String.valueOf(channelId), String.valueOf(userId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/channels/{channelId}/unmute/{userId}")
    public ResponseEntity<?> unmute(@PathVariable Long channelId, @PathVariable Long userId, Authentication authentication) {
        Channel c = channelService.get(channelId);
        if (c == null) return ResponseEntity.notFound().build();
        Server s = c.getServer();
        User actor = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (actor == null) return ResponseEntity.status(401).build();
        if (!membershipService.isOwner(actor, s)) return ResponseEntity.status(403).build();
        User target = userRepository.findById(userId).orElse(null);
        if (target == null) return ResponseEntity.notFound().build();
        moderationService.unmute(target, c);
        auditService.record("MOD", "UNMUTE", actor.getEmail(), String.valueOf(channelId), String.valueOf(userId));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/channels/{channelId}/mute/{userId}")
    public ResponseEntity<java.util.Map<String, Boolean>> isMuted(@PathVariable Long channelId, @PathVariable Long userId) {
        Channel c = channelService.get(channelId);
        if (c == null) return ResponseEntity.notFound().build();
        User target = userRepository.findById(userId).orElse(null);
        if (target == null) return ResponseEntity.notFound().build();
        boolean muted = moderationService.isMuted(target, c);
        return ResponseEntity.ok(java.util.Map.of("muted", muted));
    }
}