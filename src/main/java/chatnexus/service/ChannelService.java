package chatnexus.service;

import chatnexus.model.Channel;
import chatnexus.model.Server;
import chatnexus.repository.ChannelRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChannelService {
    private final ChannelRepository channelRepository;

    public ChannelService(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    public Channel create(Server server, String name) {
        Channel c = new Channel();
        c.setServer(server);
        c.setName(name);
        return channelRepository.save(c);
    }

    public List<Channel> list(Server server) { return channelRepository.findByServer(server); }
    public Channel get(Long id) { return channelRepository.findById(id).orElse(null); }
}