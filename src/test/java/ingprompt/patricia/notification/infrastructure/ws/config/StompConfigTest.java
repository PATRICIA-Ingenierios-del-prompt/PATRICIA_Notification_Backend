package ingprompt.patricia.notification.infrastructure.ws.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StompConfigTest {

    private final StompConfig config = new StompConfig();

    private static class Captured {
        HandshakeInterceptor interceptor;
        DefaultHandshakeHandler handler;
    }

    private Captured registerAndCapture() {
        StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
        StompWebSocketEndpointRegistration registration = mock(StompWebSocketEndpointRegistration.class);
        when(registry.addEndpoint("/ws/notifications")).thenReturn(registration);
        when(registration.setAllowedOriginPatterns(any())).thenReturn(registration);

        ArgumentCaptor<HandshakeInterceptor[]> interceptorCaptor = ArgumentCaptor.forClass(HandshakeInterceptor[].class);
        when(registration.addInterceptors(interceptorCaptor.capture())).thenReturn(registration);

        ArgumentCaptor<org.springframework.web.socket.server.HandshakeHandler> handlerCaptor =
                ArgumentCaptor.forClass(org.springframework.web.socket.server.HandshakeHandler.class);
        when(registration.setHandshakeHandler(handlerCaptor.capture())).thenReturn(registration);

        config.registerStompEndpoints(registry);

        Captured captured = new Captured();
        captured.interceptor = interceptorCaptor.getValue()[0];
        captured.handler = (DefaultHandshakeHandler) handlerCaptor.getValue();
        return captured;
    }

    private ServletServerHttpRequest requestWithHeader(String headerValue, String paramValue) {
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getHeader("X-User-Id")).thenReturn(headerValue);
        if (headerValue == null || headerValue.isBlank()) {
            when(servletRequest.getParameter("userId")).thenReturn(paramValue);
        }
        return new ServletServerHttpRequest(servletRequest);
    }

    @Test
    void beforeHandshake_headerPresent_storesUserIdAndAllows() throws Exception {
        Captured captured = registerAndCapture();
        UUID userId = UUID.randomUUID();
        ServletServerHttpRequest request = requestWithHeader(userId.toString(), null);
        Map<String, Object> attrs = new HashMap<>();

        boolean allowed = captured.interceptor.beforeHandshake(request, mock(ServerHttpResponse.class),
                mock(WebSocketHandler.class), attrs);

        assertThat(allowed).isTrue();
        assertThat(attrs.get(StompConfig.USER_ID_ATTR)).isEqualTo(userId);
    }

    @Test
    void beforeHandshake_headerMissing_fallsBackToQueryParam() {
        Captured captured = registerAndCapture();
        UUID userId = UUID.randomUUID();
        ServletServerHttpRequest request = requestWithHeader(null, userId.toString());
        Map<String, Object> attrs = new HashMap<>();

        boolean allowed = captured.interceptor.beforeHandshake(request, mock(ServerHttpResponse.class),
                mock(WebSocketHandler.class), attrs);

        assertThat(allowed).isTrue();
        assertThat(attrs.get(StompConfig.USER_ID_ATTR)).isEqualTo(userId);
    }

    @Test
    void beforeHandshake_noIdentityAnywhere_rejectsHandshake() {
        Captured captured = registerAndCapture();
        ServletServerHttpRequest request = requestWithHeader(null, null);
        Map<String, Object> attrs = new HashMap<>();

        boolean allowed = captured.interceptor.beforeHandshake(request, mock(ServerHttpResponse.class),
                mock(WebSocketHandler.class), attrs);

        assertThat(allowed).isFalse();
        assertThat(attrs).doesNotContainKey(StompConfig.USER_ID_ATTR);
    }

    @Test
    void beforeHandshake_invalidUuid_rejectsHandshake() {
        Captured captured = registerAndCapture();
        ServletServerHttpRequest request = requestWithHeader("not-a-uuid", null);
        Map<String, Object> attrs = new HashMap<>();

        boolean allowed = captured.interceptor.beforeHandshake(request, mock(ServerHttpResponse.class),
                mock(WebSocketHandler.class), attrs);

        assertThat(allowed).isFalse();
    }

    @Test
    void beforeHandshake_nonServletRequest_allowsWithoutStoringUser() {
        Captured captured = registerAndCapture();
        ServerHttpRequest request = mock(ServerHttpRequest.class); // not a ServletServerHttpRequest
        Map<String, Object> attrs = new HashMap<>();

        boolean allowed = captured.interceptor.beforeHandshake(request, mock(ServerHttpResponse.class),
                mock(WebSocketHandler.class), attrs);

        assertThat(allowed).isTrue();
        assertThat(attrs).isEmpty();
    }

    @Test
    void afterHandshake_isNoOp() {
        Captured captured = registerAndCapture();
        // Must not throw; covers the empty method body.
        captured.interceptor.afterHandshake(mock(ServerHttpRequest.class), mock(ServerHttpResponse.class),
                mock(WebSocketHandler.class), null);
    }

    @Test
    void determineUser_usesStoredUserIdAsPrincipalName() throws Exception {
        Captured captured = registerAndCapture();
        UUID userId = UUID.randomUUID();
        Map<String, Object> attrs = Map.of(StompConfig.USER_ID_ATTR, userId);

        Method determineUser = captured.handler.getClass().getDeclaredMethod(
                "determineUser", ServerHttpRequest.class, WebSocketHandler.class, Map.class);
        determineUser.setAccessible(true);
        Principal principal = (Principal) determineUser.invoke(captured.handler,
                mock(ServerHttpRequest.class), mock(WebSocketHandler.class), attrs);

        assertThat(principal.getName()).isEqualTo(userId.toString());
    }

    @Test
    void configureMessageBroker_enablesSimpleBrokerAndUserPrefix() {
        MessageBrokerRegistry registry = mock(MessageBrokerRegistry.class);

        config.configureMessageBroker(registry);

        verify(registry).enableSimpleBroker("/topic", "/queue");
        verify(registry).setUserDestinationPrefix("/user");
    }
}
