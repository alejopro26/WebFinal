package chatnexus.service;

import chatnexus.model.DirectMessage;
import chatnexus.model.User;
import chatnexus.repository.DirectMessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class DirectMessageService {

    private final DirectMessageRepository repo;

    public DirectMessageService(DirectMessageRepository repo) {
        this.repo = repo;
    }

    public DirectMessage send(String content, User sender, User receiver) {
        DirectMessage dm = new DirectMessage();
        dm.setContent(content);
        dm.setSender(sender);
        dm.setReceiver(receiver);
        return repo.save(dm);
    }

    public Page<DirectMessage> history(User a, User b, int page, int size) {
        return repo.findBySenderAndReceiverOrSenderAndReceiverOrderBySentAtAsc(a, b, b, a, PageRequest.of(page, size));
    }
}