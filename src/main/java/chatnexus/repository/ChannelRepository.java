package chatnexus.repository;

import chatnexus.model.Channel;
import chatnexus.model.Server;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChannelRepository extends JpaRepository<Channel, Long> {
    List<Channel> findByServer(Server server);
}