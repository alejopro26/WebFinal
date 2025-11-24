package chatnexus.controller;

import chatnexus.dto.RoomDTO;
import chatnexus.dto.MemberDTO;
import chatnexus.model.Server;
import chatnexus.model.User;
import chatnexus.repository.UserRepository;
import chatnexus.service.ServerMembershipService;
import chatnexus.service.ServerService;
import chatnexus.service.ChannelService;
import chatnexus.model.Channel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servers")
public class ServerController {

    private final ServerService serverService;
    private final ServerMembershipService membershipService;
    private final ChannelService channelService;
    private final UserRepository userRepository;

    public ServerController(ServerService serverService, ServerMembershipService membershipService,
                            ChannelService channelService, UserRepository userRepository) {
        this.serverService = serverService;
        this.membershipService = membershipService;
        this.channelService = channelService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<Server> create(@RequestBody RoomDTO dto, Authentication authentication) {
        Server s = serverService.create(dto.getName());
        User u = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (u != null) membershipService.joinAsOwner(u, s);
        return ResponseEntity.ok(s);
    }

    @GetMapping
    public ResponseEntity<List<Server>> list() { return ResponseEntity.ok(serverService.list()); }

    @PostMapping("/{id}/join")
    public ResponseEntity<?> join(@PathVariable Long id, Authentication authentication) {
        Server s = serverService.get(id);
        if (s == null) return ResponseEntity.notFound().build();
        User u = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (u == null) return ResponseEntity.status(401).build();
        membershipService.join(u, s);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<?> leave(@PathVariable Long id, Authentication authentication) {
        Server s = serverService.get(id);
        if (s == null) return ResponseEntity.notFound().build();
        User u = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (u == null) return ResponseEntity.status(401).build();
        membershipService.leave(u, s);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/channels")
    public ResponseEntity<Channel> createChannel(@PathVariable Long id, @RequestBody RoomDTO dto, Authentication authentication) {
        Server s = serverService.get(id);
        if (s == null) return ResponseEntity.notFound().build();
        User u = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (u == null) return ResponseEntity.status(401).build();
        if (!membershipService.isOwner(u, s)) return ResponseEntity.status(403).build();
        Channel c = channelService.create(s, dto.getName());
        return ResponseEntity.ok(c);
    }

    @GetMapping("/{id}/channels")
    public ResponseEntity<List<Channel>> listChannels(@PathVariable Long id) {
        Server s = serverService.get(id);
        if (s == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(channelService.list(s));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<java.util.List<MemberDTO>> listMembers(@PathVariable Long id) {
        Server s = serverService.get(id);
        if (s == null) return ResponseEntity.notFound().build();
        var members = membershipService.listMembers(s).stream().map(m -> {
            MemberDTO dto = new MemberDTO();
            dto.setUserId(m.getUser().getId());
            dto.setUsername(m.getUser().getUsername());
            dto.setRole(m.getRole().name());
            return dto;
        }).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(members);
    }
}