package chatnexus.service;

import chatnexus.model.*;
import chatnexus.repository.ChannelAclRepository;
import chatnexus.repository.ServerMemberRepository;
import org.springframework.stereotype.Service;

@Service
public class ChannelAclService {
    private final ChannelAclRepository aclRepo;
    private final ServerMemberRepository memberRepo;

    public ChannelAclService(ChannelAclRepository aclRepo, ServerMemberRepository memberRepo) {
        this.aclRepo = aclRepo;
        this.memberRepo = memberRepo;
    }

    private ServerRole roleOf(User user, Server server) {
        return memberRepo.findByUserAndServer(user, server)
                .map(ServerMember::getRole)
                .orElse(ServerRole.MEMBER);
    }

    public boolean canSubscribe(User user, Channel channel) {
        ServerRole role = roleOf(user, channel.getServer());
        return aclRepo.findByChannelAndRole(channel, role)
                .map(ChannelAcl::isCanSubscribe)
                .orElse(true);
    }

    public boolean canSend(User user, Channel channel) {
        ServerRole role = roleOf(user, channel.getServer());
        return aclRepo.findByChannelAndRole(channel, role)
                .map(ChannelAcl::isCanSend)
                .orElse(true);
    }

    public boolean canManage(User user, Channel channel) {
        Server server = channel.getServer();
        ServerRole role = roleOf(user, server);
        if (role == ServerRole.OWNER) return true;
        return aclRepo.findByChannelAndRole(channel, role)
                .map(ChannelAcl::isCanManage)
                .orElse(false);
    }
}