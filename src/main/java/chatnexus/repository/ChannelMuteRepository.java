package chatnexus.repository;

import chatnexus.model.ChannelMute;
import chatnexus.model.Channel;
import chatnexus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelMuteRepository extends JpaRepository<ChannelMute, Long> {
    boolean existsByUserAndChannel(User user, Channel channel);
    void deleteByUserAndChannel(User user, Channel channel);
}