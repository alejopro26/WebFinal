package chatnexus.repository;

import chatnexus.model.DirectMessage;
import chatnexus.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {
    Page<DirectMessage> findBySenderAndReceiverOrSenderAndReceiverOrderBySentAtAsc(
            User sender1, User receiver1, User sender2, User receiver2, Pageable pageable);
}