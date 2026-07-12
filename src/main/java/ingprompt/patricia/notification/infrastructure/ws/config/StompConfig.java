package ingprompt.patricia.notification.infrastructure.ws.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Configuration
@EnableWebSocketMessageBroker
public class StompConfig implements WebSocketMessageBrokerConfigurer {

    public static final String USER_ID_ATTR = "userId";

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/notifications")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest req, ServerHttpResponse res,
                                                   WebSocketHandler wsHandler, Map<String, Object> attrs) {
                        if (req instanceof ServletServerHttpRequest servlet) {
                            String header = servlet.getServletRequest().getHeader("X-User-Id");
                            String raw = (header != null && !header.isBlank())
                                    ? header
                                    : servlet.getServletRequest().getParameter("userId"); // dev fallback
                            if (raw == null || raw.isBlank()) {
                                return false; // reject handshake without identity
                            }
                            try {
                                attrs.put(USER_ID_ATTR, UUID.fromString(raw));
                            } catch (IllegalArgumentException ex) {
                                return false;
                            }
                        }
                        return true;
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest req, ServerHttpResponse res,
                                               WebSocketHandler wsHandler, Exception exception) {
                    }
                })
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                                      Map<String, Object> attributes) {
                        UUID userId = (UUID) attributes.get(USER_ID_ATTR);
                        // Principal#getName() must be stable per-user; STOMP uses it for /user/... routing.
                        return () -> userId.toString();
                    }
                });
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");   // server PUBLISH -> subscribers
        registry.setUserDestinationPrefix("/user");        // private per-session ("/user/queue/...")
    }
}
