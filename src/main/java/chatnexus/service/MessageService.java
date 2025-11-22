package chatnexus.service;

import chatnexus.model.ChatRoom;
import chatnexus.model.Message;
import chatnexus.model.User;
import chatnexus.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
