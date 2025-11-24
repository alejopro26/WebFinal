package chatnexus.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class PresenceService {

    private final ConcurrentHashMap<String, Boolean> online = new ConcurrentHashMap<>();

    public void setOnline(String userId) { online.put(userId, true); }
    public void setOffline(String userId) { online.remove(userId); }
    public List<String> onlineUsers() {
        return online.keySet().stream().sorted().collect(Collectors.toList());
    }
}