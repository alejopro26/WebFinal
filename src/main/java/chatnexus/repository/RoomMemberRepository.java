package chatnexus.repository;

import chatnexus.model.ChatRoom;
import chatnexus.model.RoomMember;
import chatnexus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {
    boolean existsByUserAndRoom(User user, ChatRoom room);
    Optional<RoomMember> findByUserAndRoom(User user, ChatRoom room);
    List<RoomMember> findByRoom(ChatRoom room);
    List<RoomMember> findByUser(User user);
}