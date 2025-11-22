package chatnexus.service;

import chatnexus.model.ChatRoom;
import chatnexus.repository.ChatRoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    public ChatRoomService(ChatRoomRepository chatRoomRepository) {
        this.chatRoomRepository = chatRoomRepository;
    }

    // Crear una sala
    public ChatRoom createRoom(String name, boolean isPrivate, String password) {
        if (chatRoomRepository.findByName(name).isPresent()) {
            throw new RuntimeException("El nombre de la sala ya existe.");
        }

        ChatRoom room = new ChatRoom();
        room.setName(name);
        room.setPrivate(isPrivate);

        if (isPrivate) {
            room.setRoomPassword(password);
        }

        return chatRoomRepository.save(room);
    }

    // Obtener todas las salas
    public List<ChatRoom> getAllRooms() {
        return chatRoomRepository.findAll();
    }

    // Buscar sala por ID
    public ChatRoom getRoomById(Long id) {
        Optional<ChatRoom> room = chatRoomRepository.findById(id);
        return room.orElse(null);
    }

    // Validar acceso a una sala privada
    public boolean canAccess(ChatRoom room, String password) {
        if (!room.isPrivate()) return true; // si no es privada, acceso libre
        if (password == null) return false;
        return room.getRoomPassword() != null && room.getRoomPassword().equals(password);
    }
}
