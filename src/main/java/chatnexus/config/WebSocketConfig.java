package chatnexus.config;

import chatnexus.security.AuthChannelInterceptor;
import chatnexus.security.SubscribeAuthChannelInterceptor;
import chatnexus.security.PresenceChannelInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final AuthChannelInterceptor authChannelInterceptor;
    private final SubscribeAuthChannelInterceptor subscribeAuthChannelInterceptor;
    private final PresenceChannelInterceptor presenceChannelInterceptor;

    public WebSocketConfig(AuthChannelInterceptor authChannelInterceptor,
                           SubscribeAuthChannelInterceptor subscribeAuthChannelInterceptor,
                           PresenceChannelInterceptor presenceChannelInterceptor) {
        this.authChannelInterceptor = authChannelInterceptor;
        this.subscribeAuthChannelInterceptor = subscribeAuthChannelInterceptor;
        this.presenceChannelInterceptor = presenceChannelInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    // REGISTRAMOS EL INTERCEPTOR DE AUTENTICACIÓN
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authChannelInterceptor, subscribeAuthChannelInterceptor, presenceChannelInterceptor);
    }
}
