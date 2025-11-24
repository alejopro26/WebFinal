package chatnexus.security;

import chatnexus.jwt.JwtService;
import chatnexus.model.User;
import chatnexus.repository.UserRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthChannelInterceptor(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // Solo validar cuando el cliente envía el frame CONNECT
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("No se envió token JWT en el CONNECT");
            }

            String token = authHeader.substring(7);

            if (!jwtService.validateToken(token)) {
                throw new RuntimeException("Token inválido");
            }

            String subject = jwtService.extractUsername(token); // el subject es el email

            User user = userRepository.findByEmail(subject)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(subject, null, null);

            accessor.setUser(authentication);
        }

        return message;
    }
}
