package ingprompt.patricia.notification.infrastructure.push;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ingprompt.patricia.notification.application.port.out.DeviceTokenRepositoryOutPort;
import ingprompt.patricia.notification.application.port.out.MobilePushPort;
import ingprompt.patricia.notification.domain.model.NotificationView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Slf4j
@Component
public class ExpoPushAdapter implements MobilePushPort {
    private static final String APP_TITLE = "PATRICIA";
    private static final String DEVICE_NOT_REGISTERED = "DeviceNotRegistered";

    private final RestClient restClient;
    private final DeviceTokenRepositoryOutPort deviceTokenRepository;
    private final boolean enabled;
    private final String accessToken;

    public ExpoPushAdapter(@Value("${expo.push.url}") String url, @Value("${expo.push.enabled}") boolean enabled, @Value("${expo.push.access-token}") String accessToken, DeviceTokenRepositoryOutPort deviceTokenRepository) {
        this.restClient = RestClient.builder().baseUrl(url).build();
        this.enabled = enabled;
        this.accessToken = accessToken;
        this.deviceTokenRepository = deviceTokenRepository;
    }

    @Override
    public void pushToUser(UUID userId, NotificationView view) {
        if (!enabled) {
            return; // web STOMP push still delivered this notification
        }
        List<String> tokens = deviceTokenRepository.findTokensByUser(userId);
        if (tokens.isEmpty()) {
            return;
        }
        List<Map<String, Object>> messages = tokens.stream().map(token -> message(token, view)).toList();
        try {
            ExpoResponse response = restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(h -> {
                        if (StringUtils.hasText(accessToken)) {
                            h.setBearerAuth(accessToken);
                        }
                    })
                    .body(messages)
                    .retrieve()
                    .body(ExpoResponse.class);
            pruneInvalidTokens(tokens, response);
        } catch (RestClientException ex) {
            log.warn("Expo push failed for user {}: {}", userId, ex.getMessage());
        }
    }

    private Map<String, Object> message(String token, NotificationView view) {
        Map<String, Object> message = new HashMap<>();
        message.put("to", token);
        message.put("title", APP_TITLE);
        message.put("body", view.message());
        message.put("data", data(view));
        return message;
    }

    private Map<String, String> data(NotificationView view) {
        Map<String, String> data = new HashMap<>();
        if (view.payload() != null) {
            data.putAll(view.payload());
        }
        data.put("notificationId", String.valueOf(view.id()));
        data.put("type", view.type().name());
        data.put("scope", view.scope().name());
        return data;
    }

    private void pruneInvalidTokens(List<String> tokens, ExpoResponse response) {
        if (response == null || response.data() == null) {
            return;
        }
        List<ExpoTicket> tickets = response.data();
        for (int i = 0; i < tickets.size() && i < tokens.size(); i++) {
            ExpoTicket ticket = tickets.get(i);
            if (ticket != null && "error".equals(ticket.status())
                    && ticket.details() != null && DEVICE_NOT_REGISTERED.equals(ticket.details().error())) {
                deviceTokenRepository.deleteByToken(tokens.get(i));
                log.info("Pruned Expo token reported DeviceNotRegistered");
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ExpoResponse(List<ExpoTicket> data) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ExpoTicket(String status, ExpoTicketDetails details) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ExpoTicketDetails(String error) {
    }
}
