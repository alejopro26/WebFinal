package chatnexus.service;

import chatnexus.model.Channel;
import chatnexus.model.User;
import chatnexus.model.Server;
import chatnexus.repository.ChannelMuteRepository;
import chatnexus.repository.ServerBanRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

public class ModerationServiceTest {
    @Test
    void banAndUnbanFlow() {
        ServerBanRepository banRepo = Mockito.mock(ServerBanRepository.class);
        ChannelMuteRepository muteRepo = Mockito.mock(ChannelMuteRepository.class);
        ModerationService svc = new ModerationService(banRepo, muteRepo);
        User u = new User(); u.setId(1L);
        Server s = new Server(); s.setId(10L);

        Mockito.when(banRepo.existsByUserAndServer(u, s)).thenReturn(false, true);
        svc.ban(u, s);
        assertTrue(svc.isBanned(u, s));
        svc.unban(u, s);
        Mockito.verify(banRepo).deleteByUserAndServer(u, s);
    }

    @Test
    void muteAndUnmuteFlow() {
        ServerBanRepository banRepo = Mockito.mock(ServerBanRepository.class);
        ChannelMuteRepository muteRepo = Mockito.mock(ChannelMuteRepository.class);
        ModerationService svc = new ModerationService(banRepo, muteRepo);
        User u = new User(); u.setId(1L);
        Channel c = new Channel(); c.setId(100L);

        Mockito.when(muteRepo.existsByUserAndChannel(u, c)).thenReturn(false, true);
        svc.mute(u, c);
        assertTrue(svc.isMuted(u, c));
        svc.unmute(u, c);
        Mockito.verify(muteRepo).deleteByUserAndChannel(u, c);
    }
}