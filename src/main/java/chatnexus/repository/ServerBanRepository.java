package chatnexus.repository;

import chatnexus.model.ServerBan;
import chatnexus.model.Server;
import chatnexus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerBanRepository extends JpaRepository<ServerBan, Long> {
    boolean existsByUserAndServer(User user, Server server);
    void deleteByUserAndServer(User user, Server server);
}