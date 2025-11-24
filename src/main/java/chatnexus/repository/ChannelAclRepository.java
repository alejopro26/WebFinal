package chatnexus.repository;

import chatnexus.model.ChannelAcl;
import chatnexus.model.Channel;
import chatnexus.model.ServerRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChannelAclRepository extends JpaRepository<ChannelAcl, Long> {
    Optional<ChannelAcl> findByChannelAndRole(Channel channel, ServerRole role);
}