package chatnexus.model;

import jakarta.persistence.*;

@Entity
@Table(name = "server_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "server_id"})
})
public class ServerMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServerRole role = ServerRole.MEMBER;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Server getServer() { return server; }
    public void setServer(Server server) { this.server = server; }
    public ServerRole getRole() { return role; }
    public void setRole(ServerRole role) { this.role = role; }
}