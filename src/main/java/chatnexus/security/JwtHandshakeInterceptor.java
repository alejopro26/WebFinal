package chatnexus.security;

import chatnexus.jwt.JwtService;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.Map;
import java.util.Objects;

/**
 * Extrae el token JWT del handshake HTTP (header "Authorization" o query param "token"),
 * valida el token usando JwtService y coloca un Principal en los atributos de la sesión
 * con clave "user". El DefaultHandshakeHandler configurado en WebSocketConfig tomará
 * este principal y lo usará como el Principal de la sesión WebSocket.
 *
 * Requisitos: tu JwtService debe exponer al menos:
 *   - String extractUsername(String token)   (o método equivalente)
 *   - boolean validateToken(String token)    (o método equivalente)
 *
 * Si los nombres son distintos, adapta las llamadas abajo a tus métodos reales.
 */
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;

    public JwtHandshakeInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   org.springframework.web.socket.WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        String token = null;

        // 1) Intentar leer header Authorization
        if (request.getHeaders() != null && request.getHeaders().containsKey("Authorization")) {
            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        // 2) Si no hay header, intentar leer query param "token"
        if (token == null && request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletReq = (ServletServerHttpRequest) request;
            String q = servletReq.getServletRequest().getParameter("token");
            if (q != null && !q.isEmpty()) token = q;
        }

        // 3) Si no hay token, denegar
        if (token == null || token.isBlank()) {
            return true; // permitir handshake pero sin principal -> bloqueos posteriores en endpoints si requiere auth
            // Si prefieres rechazar el handshake directamente, devuelve false.
        }

        // 4) Validar token y extraer username
        try {
            boolean valid = jwtService.validateToken(token);
            if (!valid) {
                return false; // token inválido -> rechazar handshake
            }

            String username = jwtService.extractUsername(token);
            if (username == null || username.isBlank()) {
                return false;
            }

            // Crear Principal mínimo (solo con nombre). Esto bastará para authentication.getName() en @MessageMapping
            Principal principal = new Principal() {
                @Override
                public String getName() {
                    return username;
                }
            };

            // Guardar el principal en los atributos para que el DefaultHandshakeHandler lo lea
            attributes.put("user", principal);
            // También puedes guardar el token si lo necesitas luego
            attributes.put("jwt", token);

            return true;
        } catch (Exception ex) {
            // token inválido o error -> rechazar handshake
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               org.springframework.web.socket.WebSocketHandler wsHandler,
                               @Nullable Exception exception) {
        // no-op
    }
}
