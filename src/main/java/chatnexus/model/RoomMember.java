package chatnexus.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "room_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "room_id"})
})
public class RoomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private RoomRole role = RoomRole.MEMBER;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public ChatRoom getRoom() { return room; }
    public void setRoom(ChatRoom room) { this.room = room; }

    public RoomRole getRole() { return role; }
    public void setRole(RoomRole role) { this.role = role; }
}