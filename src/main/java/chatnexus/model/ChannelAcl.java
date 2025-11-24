package chatnexus.model;

import jakarta.persistence.*;

@Entity
@Table(name = "channel_acl", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"channel_id", "role"})
})
public class ChannelAcl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServerRole role;

    @Column(name = "can_send", nullable = false)
    private boolean canSend = true;

    @Column(name = "can_subscribe", nullable = false)
    private boolean canSubscribe = true;

    @Column(name = "can_manage", nullable = false)
    private boolean canManage = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Channel getChannel() { return channel; }
    public void setChannel(Channel channel) { this.channel = channel; }
    public ServerRole getRole() { return role; }
    public void setRole(ServerRole role) { this.role = role; }
    public boolean isCanSend() { return canSend; }
    public void setCanSend(boolean canSend) { this.canSend = canSend; }
    public boolean isCanSubscribe() { return canSubscribe; }
    public void setCanSubscribe(boolean canSubscribe) { this.canSubscribe = canSubscribe; }
    public boolean isCanManage() { return canManage; }
    public void setCanManage(boolean canManage) { this.canManage = canManage; }
}