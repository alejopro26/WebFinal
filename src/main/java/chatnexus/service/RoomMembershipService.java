package chatnexus.service;

import chatnexus.model.ChatRoom;
import chatnexus.model.RoomMember;
import chatnexus.model.RoomRole;
import chatnexus.model.User;
import chatnexus.repository.RoomMemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomMembershipService {

    private final RoomMemberRepository roomMemberRepository;

    public RoomMembershipService(RoomMemberRepository roomMemberRepository) {
        this.roomMemberRepository = roomMemberRepository;
    }

    public boolean isMember(User user, ChatRoom room) {
        return roomMemberRepository.existsByUserAndRoom(user, room);
    }

    public RoomMember join(User user, ChatRoom room) {
        return roomMemberRepository.findByUserAndRoom(user, room)
                .orElseGet(() -> {
                    RoomMember m = new RoomMember();
                    m.setUser(user);
                    m.setRoom(room);
                    m.setRole(RoomRole.MEMBER);
                    return roomMemberRepository.save(m);
                });
    }

    public RoomMember joinAsOwner(User user, ChatRoom room) {
        return roomMemberRepository.findByUserAndRoom(user, room)
                .orElseGet(() -> {
                    RoomMember m = new RoomMember();
                    m.setUser(user);
                    m.setRoom(room);
                    m.setRole(RoomRole.OWNER);
                    return roomMemberRepository.save(m);
                });
    }

    public void promoteToModerator(User user, ChatRoom room) {
        roomMemberRepository.findByUserAndRoom(user, room).ifPresent(m -> {
            m.setRole(RoomRole.MODERATOR);
            roomMemberRepository.save(m);
        });
    }

    public boolean isOwner(User user, ChatRoom room) {
        return roomMemberRepository.findByUserAndRoom(user, room)
                .map(m -> m.getRole() == RoomRole.OWNER)
                .orElse(false);
    }

    public boolean isModeratorOrOwner(User user, ChatRoom room) {
        return roomMemberRepository.findByUserAndRoom(user, room)
                .map(m -> m.getRole() == RoomRole.MODERATOR || m.getRole() == RoomRole.OWNER)
                .orElse(false);
    }

    public void leave(User user, ChatRoom room) {
        roomMemberRepository.findByUserAndRoom(user, room)
                .ifPresent(roomMemberRepository::delete);
    }

    public List<RoomMember> listMembers(ChatRoom room) {
        return roomMemberRepository.findByRoom(room);
    }

    public List<RoomMember> listMembers(User user) {
        return roomMemberRepository.findByUser(user);
    }
}