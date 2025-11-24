package chatnexus.controller;

import chatnexus.dto.MessageDTO;
import chatnexus.model.DirectMessage;
import chatnexus.model.User;
import chatnexus.repository.UserRepository;
import chatnexus.service.DirectMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dm")
public class DirectMessageController {

    private final DirectMessageService dmService;
    private final UserRepository userRepository;

    public DirectMessageController(DirectMessageService dmService, UserRepository userRepository) {
        this.dmService = dmService;
        this.userRepository = userRepository;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<MessageDTO>> history(@PathVariable Long userId,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "50") int size,
                                                    Authentication authentication) {
        User me = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (me == null) return ResponseEntity.status(401).build();
        User other = userRepository.findById(userId).orElse(null);
        if (other == null) return ResponseEntity.notFound().build();

        var list = dmService.history(me, other, page, size).stream().map(dm -> {
            MessageDTO dto = new MessageDTO();
            dto.setId(dm.getId());
            dto.setContent(dm.getContent());
            dto.setSender(dm.getSender().getUsername());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}