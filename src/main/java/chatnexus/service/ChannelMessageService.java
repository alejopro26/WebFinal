package chatnexus.service;

import chatnexus.model.ChannelMessage;
import chatnexus.model.Channel;
import chatnexus.model.User;
import chatnexus.repository.ChannelMessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ChannelMessageService {
    private final ChannelMessageRepository repo;
    public ChannelMessageService(ChannelMessageRepository repo) { this.repo = repo; }

    public ChannelMessage save(String content, User sender, Channel channel) {
        ChannelMessage m = new ChannelMessage();
        m.setContent(content);
        m.setSender(sender);
        m.setChannel(channel);
        return repo.save(m);
    }

    public Page<ChannelMessage> history(Channel channel, int page, int size) {
        return repo.findByChannelOrderBySentAtAsc(channel, PageRequest.of(page, size));
    }
}