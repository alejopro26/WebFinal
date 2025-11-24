package chatnexus.service;

import chatnexus.model.*;
import chatnexus.repository.ChannelMuteRepository;
import chatnexus.repository.ServerBanRepository;
import org.springframework.stereotype.Service;

@Service
public class ModerationService {
    private final ServerBanRepository banRepo;
    private final ChannelMuteRepository muteRepo;

    public ModerationService(ServerBanRepository banRepo, ChannelMuteRepository muteRepo) {
        this.banRepo = banRepo;
        this.muteRepo = muteRepo;
    }

    public boolean isBanned(User user, Server server) { return banRepo.existsByUserAndServer(user, server); }
    public boolean isMuted(User user, Channel channel) { return muteRepo.existsByUserAndChannel(user, channel); }

    public void ban(User user, Server server) {
        if (!banRepo.existsByUserAndServer(user, server)) {
            ServerBan b = new ServerBan();
            b.setUser(user);
            b.setServer(server);
            banRepo.save(b);
        }
    }
    public void unban(User user, Server server) { banRepo.deleteByUserAndServer(user, server); }

    public void mute(User user, Channel channel) {
        if (!muteRepo.existsByUserAndChannel(user, channel)) {
            ChannelMute m = new ChannelMute();
            m.setUser(user);
            m.setChannel(channel);
            muteRepo.save(m);
        }
    }
    public void unmute(User user, Channel channel) { muteRepo.deleteByUserAndChannel(user, channel); }
}