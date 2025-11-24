package chatnexus.repository;

import chatnexus.model.Message;
import chatnexus.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByRoomOrderBySentAtAsc(ChatRoom room);
    Page<Message> findByRoomOrderBySentAtAsc(ChatRoom room, Pageable pageable);
}
