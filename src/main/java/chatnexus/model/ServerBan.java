package chatnexus.model;

import jakarta.persistence.*;

@Entity
@Table(name = "server_bans", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "server_id"})
})
public class ServerBan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Server getServer() { return server; }
    public void setServer(Server server) { this.server = server; }
}