package chatnexus.controller;

import chatnexus.dto.MessageDTO;
import chatnexus.model.Channel;
import chatnexus.service.ChannelMessageService;
import chatnexus.service.ChannelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/channels")
public class ChannelController {

    private final ChannelService channelService;
    private final ChannelMessageService messageService;
    private final chatnexus.service.ServerMembershipService membershipService;

    public ChannelController(ChannelService channelService, ChannelMessageService messageService,
                             chatnexus.service.ServerMembershipService membershipService) {
        this.channelService = channelService;
        this.messageService = messageService;
        this.membershipService = membershipService;
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<MessageDTO>> history(@PathVariable Long id,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "50") int size) {
        Channel channel = channelService.get(id);
        if (channel == null) return ResponseEntity.notFound().build();
        var list = messageService.history(channel, page, size).stream().map(m -> {
            MessageDTO dto = new MessageDTO();
            dto.setId(m.getId());
            dto.setContent(m.getContent());
            dto.setSender(m.getSender().getUsername());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<java.util.List<chatnexus.dto.MemberDTO>> members(@PathVariable Long id) {
        Channel channel = channelService.get(id);
        if (channel == null) return ResponseEntity.notFound().build();
        var server = channel.getServer();
        var members = membershipService.listMembers(server).stream().map(m -> {
            chatnexus.dto.MemberDTO dto = new chatnexus.dto.MemberDTO();
            dto.setUserId(m.getUser().getId());
            dto.setUsername(m.getUser().getUsername());
            dto.setRole(m.getRole().name());
            return dto;
        }).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(members);
    }
}