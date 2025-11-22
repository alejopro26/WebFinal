package chatnexus.controller;

import chatnexus.dto.RoomDTO;
import chatnexus.dto.RoomAccessDTO;
import chatnexus.model.ChatRoom;
import chatnexus.service.ChatRoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final ChatRoomService chatRoomService;

    public RoomController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    // Crear sala
    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody RoomDTO roomDTO) {

        ChatRoom room = chatRoomService.createRoom(
                roomDTO.getName(),
                roomDTO.isPrivate(),
                roomDTO.getPassword()
        );

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
                                        @RequestBody RoomAccessDTO accessDTO) {

        ChatRoom room = chatRoomService.getRoomById(id);
        if (room == null) return ResponseEntity.notFound().build();

        boolean allowed = chatRoomService.canAccess(room, accessDTO.getPassword());
        if (!allowed) return ResponseEntity.status(403).body("Contraseña incorrecta");

        return ResponseEntity.ok("Acceso permitido");
    }
}
