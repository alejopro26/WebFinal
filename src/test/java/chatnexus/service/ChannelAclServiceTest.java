package chatnexus.service;

import chatnexus.model.*;
import chatnexus.repository.ChannelAclRepository;
import chatnexus.repository.ServerMemberRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ChannelAclServiceTest {
    @Test
    void ownerCanManageAlways() {
        ChannelAclRepository aclRepo = Mockito.mock(ChannelAclRepository.class);
        ServerMemberRepository memberRepo = Mockito.mock(ServerMemberRepository.class);
        ChannelAclService svc = new ChannelAclService(aclRepo, memberRepo);

        User u = new User(); u.setId(1L);
        Server s = new Server(); s.setId(10L);
        Channel c = new Channel(); c.setId(100L); c.setServer(s);
        ServerMember m = new ServerMember(); m.setUser(u); m.setServer(s); m.setRole(ServerRole.OWNER);
        Mockito.when(memberRepo.findByUserAndServer(u, s)).thenReturn(Optional.of(m));

        assertTrue(svc.canManage(u, c));
    }

    @Test
    void moderatorCanManageIfAclAllows() {
        ChannelAclRepository aclRepo = Mockito.mock(ChannelAclRepository.class);
        ServerMemberRepository memberRepo = Mockito.mock(ServerMemberRepository.class);
        ChannelAclService svc = new ChannelAclService(aclRepo, memberRepo);

        User u = new User(); u.setId(1L);
        Server s = new Server(); s.setId(10L);
        Channel c = new Channel(); c.setId(100L); c.setServer(s);
        ServerMember m = new ServerMember(); m.setUser(u); m.setServer(s); m.setRole(ServerRole.MODERATOR);
        Mockito.when(memberRepo.findByUserAndServer(u, s)).thenReturn(Optional.of(m));
        ChannelAcl acl = new ChannelAcl(); acl.setChannel(c); acl.setRole(ServerRole.MODERATOR); acl.setCanManage(true);
        Mockito.when(aclRepo.findByChannelAndRole(c, ServerRole.MODERATOR)).thenReturn(Optional.of(acl));

        assertTrue(svc.canManage(u, c));
    }

    @Test
    void memberCannotManageByDefault() {
        ChannelAclRepository aclRepo = Mockito.mock(ChannelAclRepository.class);
        ServerMemberRepository memberRepo = Mockito.mock(ServerMemberRepository.class);
        ChannelAclService svc = new ChannelAclService(aclRepo, memberRepo);

        User u = new User(); u.setId(1L);
        Server s = new Server(); s.setId(10L);
        Channel c = new Channel(); c.setId(100L); c.setServer(s);
        Mockito.when(memberRepo.findByUserAndServer(u, s)).thenReturn(Optional.empty());

        assertFalse(svc.canManage(u, c));
    }
}