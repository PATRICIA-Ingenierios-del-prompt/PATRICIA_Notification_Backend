package ingprompt.patricia.notification.infrastructure.push;

import com.sun.net.httpserver.HttpServer;
import ingprompt.patricia.notification.application.port.out.DeviceTokenRepositoryOutPort;
import ingprompt.patricia.notification.domain.enums.NotificationScope;
import ingprompt.patricia.notification.domain.enums.NotificationType;
import ingprompt.patricia.notification.domain.model.NotificationView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ExpoPushAdapter builds its own RestClient internally from a base URL, so instead of mocking
 * the HTTP client we stand up a real, local, JDK-provided HTTP server and point the adapter at it.
 * No external test dependencies (like WireMock) are required.
 */
@ExtendWith(MockitoExtension.class)
class ExpoPushAdapterTest {

    @Mock
    private DeviceTokenRepositoryOutPort deviceTokenRepository;

    private HttpServer server;
    private String baseUrl;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        baseUrl = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    private NotificationView view() {
        return new NotificationView(UUID.randomUUID(), NotificationScope.TARGETED, NotificationType.NEW_MATCH_CONFIRMED,
                "hi", Map.of("k", "v"), null, Instant.now());
    }

    private void respondWith(int status, String body) {
        server.createContext("/", exchange -> {
            exchange.getRequestBody().readAllBytes(); // drain request
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();
    }

    @Test
    void pushToUser_disabled_neverQueriesTokensOrCallsServer() {
        ExpoPushAdapter adapter = new ExpoPushAdapter(baseUrl, false, "", deviceTokenRepository);

        adapter.pushToUser(UUID.randomUUID(), view());

        verify(deviceTokenRepository, never()).findTokensByUser(any());
    }

    @Test
    void pushToUser_enabled_noTokens_doesNothing() {
        UUID userId = UUID.randomUUID();
        when(deviceTokenRepository.findTokensByUser(userId)).thenReturn(List.of());
        ExpoPushAdapter adapter = new ExpoPushAdapter(baseUrl, true, "", deviceTokenRepository);

        adapter.pushToUser(userId, view());

        verify(deviceTokenRepository, never()).deleteByToken(any());
    }

    @Test
    void pushToUser_enabled_successfulResponse_prunesDeviceNotRegisteredTokens() {
        UUID userId = UUID.randomUUID();
        when(deviceTokenRepository.findTokensByUser(userId)).thenReturn(List.of("tok-ok", "tok-dead"));
        String responseJson = """
                {"data":[
                    {"status":"ok"},
                    {"status":"error","details":{"error":"DeviceNotRegistered"}}
                ]}
                """;
        respondWith(200, responseJson);
        ExpoPushAdapter adapter = new ExpoPushAdapter(baseUrl, true, "secret-token", deviceTokenRepository);

        adapter.pushToUser(userId, view());

        verify(deviceTokenRepository).deleteByToken("tok-dead");
        verify(deviceTokenRepository, never()).deleteByToken("tok-ok");
    }

    @Test
    void pushToUser_serverError_isSwallowed_doesNotThrow() {
        UUID userId = UUID.randomUUID();
        when(deviceTokenRepository.findTokensByUser(userId)).thenReturn(List.of("tok-1"));
        respondWith(500, "{}");
        ExpoPushAdapter adapter = new ExpoPushAdapter(baseUrl, true, "", deviceTokenRepository);

        adapter.pushToUser(userId, view());

        verify(deviceTokenRepository, never()).deleteByToken(any());
    }
}
