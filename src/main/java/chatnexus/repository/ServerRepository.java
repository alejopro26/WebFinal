package chatnexus.repository;

import chatnexus.model.Server;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ServerRepository extends JpaRepository<Server, Long> {
    Optional<Server> findByName(String name);
}