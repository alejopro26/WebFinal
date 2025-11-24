package chatnexus.controller;

import chatnexus.dto.UserResponse;
import chatnexus.model.User;
import chatnexus.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> list() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(u -> new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getRole().name()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        User u = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (u == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getRole().name()));
    }
}