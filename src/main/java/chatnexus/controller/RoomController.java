package chatnexus.controller;

import chatnexus.dto.RoomDTO;
import chatnexus.dto.RoomAccessDTO;
import chatnexus.dto.MessageDTO;
import chatnexus.dto.RoomMembershipDTO;
import chatnexus.model.ChatRoom;
import chatnexus.model.User;
import chatnexus.service.ChatRoomService;
import chatnexus.service.MessageService;
import chatnexus.service.RoomMembershipService;
import chatnexus.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final ChatRoomService chatRoomService;
    private final MessageService messageService;
    private final RoomMembershipService membershipService;
    private final UserRepository userRepository;

    public RoomController(ChatRoomService chatRoomService, MessageService messageService,
                          RoomMembershipService membershipService, UserRepository userRepository) {
        this.chatRoomService = chatRoomService;
        this.messageService = messageService;
        this.membershipService = membershipService;
        this.userRepository = userRepository;
    }

    // Crear sala
    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody @jakarta.validation.Valid RoomDTO roomDTO,
                                        Authentication authentication) {

        ChatRoom room = chatRoomService.createRoom(
                roomDTO.getName(),
                roomDTO.isPrivate(),
                roomDTO.getPassword()
        );

        User user = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (user != null) {
            membershipService.joinAsOwner(user, room);
        }
        return ResponseEntity.ok(room);
    }

    // Listar todas las salas
    @GetMapping
    public ResponseEntity<List<ChatRoom>> getAllRooms() {
        return ResponseEntity.ok(chatRoomService.getAllRooms());
    }

    // Obtener una sala por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getRoomById(@PathVariable Long id) {
        ChatRoom room = chatRoomService.getRoomById(id);
        if (room == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(room);
    }

    // Validar entrada a sala privada
    @PostMapping("/{id}/access")
    public ResponseEntity<?> accessRoom(@PathVariable Long id,
                                        @RequestBody @jakarta.validation.Valid RoomAccessDTO accessDTO) {

        ChatRoom room = chatRoomService.getRoomById(id);
        if (room == null) return ResponseEntity.notFound().build();

        boolean allowed = chatRoomService.canAccess(room, accessDTO.getPassword());
        if (!allowed) return ResponseEntity.status(403).body("Contraseña incorrecta");

        return ResponseEntity.ok("Acceso permitido");
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<?> join(@PathVariable Long id,
                                  @RequestBody(required = false) RoomAccessDTO accessDTO,
                                  Authentication authentication) {
        ChatRoom room = chatRoomService.getRoomById(id);
        if (room == null) return ResponseEntity.notFound().build();
        if (room.isPrivate()) {
            String pwd = accessDTO == null ? null : accessDTO.getPassword();
            if (!chatRoomService.canAccess(room, pwd)) return ResponseEntity.status(403).body("Contraseña incorrecta");
        }
        User user = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();
        membershipService.join(user, room);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<?> leave(@PathVariable Long id, Authentication authentication) {
        ChatRoom room = chatRoomService.getRoomById(id);
        if (room == null) return ResponseEntity.notFound().build();
        User user = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();
        membershipService.leave(user, room);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/promote/{userId}")
    public ResponseEntity<?> promote(@PathVariable Long id, @PathVariable Long userId,
                                     Authentication authentication) {
        ChatRoom room = chatRoomService.getRoomById(id);
        if (room == null) return ResponseEntity.notFound().build();
        User requester = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (requester == null) return ResponseEntity.status(401).build();
        if (!membershipService.isOwner(requester, room)) return ResponseEntity.status(403).build();
        User target = userRepository.findById(userId).orElse(null);
        if (target == null) return ResponseEntity.notFound().build();
        membershipService.promoteToModerator(target, room);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/kick/{userId}")
    public ResponseEntity<?> kick(@PathVariable Long id, @PathVariable Long userId,
                                  Authentication authentication) {
        ChatRoom room = chatRoomService.getRoomById(id);
        if (room == null) return ResponseEntity.notFound().build();
        User requester = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (requester == null) return ResponseEntity.status(401).build();
        if (!membershipService.isOwner(requester, room)) return ResponseEntity.status(403).build();
        User target = userRepository.findById(userId).orElse(null);
        if (target == null) return ResponseEntity.notFound().build();
        membershipService.leave(target, room);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<String>> listMembers(@PathVariable Long id) {
        ChatRoom room = chatRoomService.getRoomById(id);
        if (room == null) return ResponseEntity.notFound().build();
        var list = membershipService.listMembers(room).stream()
                .map(m -> m.getUser().getUsername())
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/my")
    public ResponseEntity<List<RoomMembershipDTO>> myRooms(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();
        var list = membershipService.listMembers(user).stream()
                .map(m -> new RoomMembershipDTO(m.getRoom().getId(), m.getRoom().getName(), m.getRole().name()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<MessageDTO>> getRoomMessages(@PathVariable Long id,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "50") int size) {
        ChatRoom room = chatRoomService.getRoomById(id);
        if (room == null) return ResponseEntity.notFound().build();

        var list = messageService.getMessagesByRoom(room, page, size).stream().map(m -> {
            MessageDTO dto = new MessageDTO();
            dto.setId(m.getId());
            dto.setRoomId(id);
            dto.setContent(m.getContent());
            dto.setSender(m.getSender().getUsername());
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{roomId}/messages/{messageId}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long roomId,
                                           @PathVariable Long messageId,
                                           Authentication authentication) {
        ChatRoom room = chatRoomService.getRoomById(roomId);
        if (room == null) return ResponseEntity.notFound().build();
        User requester = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (requester == null) return ResponseEntity.status(401).build();
        if (!membershipService.isModeratorOrOwner(requester, room)) return ResponseEntity.status(403).build();
        var msgOpt = messageService.findById(messageId);
        if (msgOpt.isEmpty() || !msgOpt.get().getRoom().getId().equals(roomId)) return ResponseEntity.notFound().build();
        messageService.delete(messageId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{roomId}/messages/{messageId}")
    public ResponseEntity<?> editMessage(@PathVariable Long roomId,
                                         @PathVariable Long messageId,
                                         @RequestBody MessageDTO body,
                                         Authentication authentication) {
        ChatRoom room = chatRoomService.getRoomById(roomId);
        if (room == null) return ResponseEntity.notFound().build();
        User requester = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (requester == null) return ResponseEntity.status(401).build();
        var msgOpt = messageService.findById(messageId);
        if (msgOpt.isEmpty()) return ResponseEntity.notFound().build();
        var msg = msgOpt.get();
        if (!msg.getRoom().getId().equals(roomId)) return ResponseEntity.notFound().build();
        boolean canEdit = membershipService.isModeratorOrOwner(requester, room) || msg.getSender().getId().equals(requester.getId());
        if (!canEdit) return ResponseEntity.status(403).build();
        messageService.updateContent(messageId, body.getContent());
        return ResponseEntity.ok().build();
    }
}
