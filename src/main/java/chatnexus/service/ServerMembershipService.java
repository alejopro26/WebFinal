package chatnexus.service;

import chatnexus.model.Server;
import chatnexus.model.ServerMember;
import chatnexus.model.ServerRole;
import chatnexus.model.User;
import chatnexus.repository.ServerMemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServerMembershipService {
    private final ServerMemberRepository repo;

    public ServerMembershipService(ServerMemberRepository repo) { this.repo = repo; }

    public boolean isMember(User user, Server server) { return repo.existsByUserAndServer(user, server); }
    public boolean isOwner(User user, Server server) {
        return repo.findByUserAndServer(user, server).map(m -> m.getRole() == ServerRole.OWNER).orElse(false);
    }
    public ServerMember join(User user, Server server) {
        return repo.findByUserAndServer(user, server).orElseGet(() -> {
            ServerMember m = new ServerMember();
            m.setUser(user);
            m.setServer(server);
            m.setRole(ServerRole.MEMBER);
            return repo.save(m);
        });
    }
    public ServerMember joinAsOwner(User user, Server server) {
        return repo.findByUserAndServer(user, server).orElseGet(() -> {
            ServerMember m = new ServerMember();
            m.setUser(user);
            m.setServer(server);
            m.setRole(ServerRole.OWNER);
            return repo.save(m);
        });
    }
    public void leave(User user, Server server) { repo.findByUserAndServer(user, server).ifPresent(repo::delete); }
    public List<ServerMember> listMembers(Server server) { return repo.findByServer(server); }
}