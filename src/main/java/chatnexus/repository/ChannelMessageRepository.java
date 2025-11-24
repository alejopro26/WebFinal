package chatnexus.repository;

import chatnexus.model.ChannelMessage;
import chatnexus.model.Channel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelMessageRepository extends JpaRepository<ChannelMessage, Long> {
    Page<ChannelMessage> findByChannelOrderBySentAtAsc(Channel channel, Pageable pageable);
}