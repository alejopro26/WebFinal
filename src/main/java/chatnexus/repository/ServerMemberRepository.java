package chatnexus.repository;

import chatnexus.model.Server;
import chatnexus.model.ServerMember;
import chatnexus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServerMemberRepository extends JpaRepository<ServerMember, Long> {
    boolean existsByUserAndServer(User user, Server server);
    Optional<ServerMember> findByUserAndServer(User user, Server server);
    List<ServerMember> findByServer(Server server);
    List<ServerMember> findByUser(User user);
}