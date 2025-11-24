package chatnexus.service;

import chatnexus.model.Server;
import chatnexus.repository.ServerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServerService {
    private final ServerRepository serverRepository;

    public ServerService(ServerRepository serverRepository) {
        this.serverRepository = serverRepository;
    }

    public Server create(String name) {
        if (serverRepository.findByName(name).isPresent()) throw new RuntimeException("Nombre ya existe");
        Server s = new Server();
        s.setName(name);
        return serverRepository.save(s);
    }

    public List<Server> list() { return serverRepository.findAll(); }
    public Server get(Long id) { return serverRepository.findById(id).orElse(null); }
}