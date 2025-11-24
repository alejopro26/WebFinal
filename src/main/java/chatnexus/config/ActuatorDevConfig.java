package chatnexus.config;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Exponer endpoints de actuator SOLO en perfil dev y con JWT.
 * Producción debe deshabilitar o proteger con IP interna.
 */
@Configuration
@Profile("dev")
public class ActuatorDevConfig {

    /**
     * Bean opcional: si quieres desactivar completamente actuator en prod,
     * crea un profile=prod que no importe esta clase.
     */

    /**
     * Endpoint custom para demo: devuelve versión y fecha de build.
     */
    @Endpoint(id = "demo")
    public static class DemoEndpoint {
        @ReadOperation
        public DemoInfo info() {
            return new DemoInfo("1.0.0", "2025-11-23", "ChatNexus – Demo para profesor");
        }
        public record DemoInfo(String version, String buildDate, String description) {}
    }

    /**
     * Asegura que /actuator/** requiere autenticación (ya viene por JWT).
     * Si en prod quieres cerrarlo, desactiva exposure o usa IP whitelist.
     */
    @Bean
    @Profile("dev")
    public SecurityFilterChain actuatorFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/actuator/**")
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
}