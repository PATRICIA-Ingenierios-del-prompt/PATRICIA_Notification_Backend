package ingprompt.patricia.notification.application.port.out;

import ingprompt.patricia.notification.domain.model.DeviceToken;

import java.util.List;
import java.util.UUID;

public interface DeviceTokenRepositoryOutPort {
    void save(DeviceToken deviceToken);
    void deleteByToken(String token);
    List<String> findTokensByUser(UUID userId);
}
