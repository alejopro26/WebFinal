package chatnexus.service;

import chatnexus.model.ChatRoom;
import chatnexus.model.Message;
import chatnexus.model.User;
import chatnexus.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.Optional;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    // Guardar un mensaje
    public Message saveMessage(String content, User sender, ChatRoom room) {
        Message message = new Message();
        message.setContent(content);
        message.setSender(sender);
        message.setRoom(room);

        return messageRepository.save(message);
    }

    // Obtener historial de una sala
    public List<Message> getMessagesByRoom(ChatRoom room) {
        return messageRepository.findByRoomOrderBySentAtAsc(room);
    }

    public Page<Message> getMessagesByRoom(ChatRoom room, int page, int size) {
        return messageRepository.findByRoomOrderBySentAtAsc(room, PageRequest.of(page, size));
    }

    public Optional<Message> findById(Long id) {
        return messageRepository.findById(id);
    }

    public void delete(Long id) {
        messageRepository.deleteById(id);
    }

    public void updateContent(Long id, String content) {
        messageRepository.findById(id).ifPresent(m -> {
            m.setContent(content);
            messageRepository.save(m);
        });
    }
}
