package ingprompt.patricia.notification.infrastructure.persistence.mongo;

import ingprompt.patricia.notification.application.port.out.DeviceTokenRepositoryOutPort;
import ingprompt.patricia.notification.domain.model.DeviceToken;
import ingprompt.patricia.notification.infrastructure.persistence.mongo.document.DeviceTokenDocument;
import ingprompt.patricia.notification.infrastructure.persistence.mongo.repository.DeviceTokenMongoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@AllArgsConstructor
public class DeviceTokenRepositoryAdapter implements DeviceTokenRepositoryOutPort {
    private final DeviceTokenMongoRepository repository;

    @Override
    public void save(DeviceToken deviceToken) {
        repository.save(new DeviceTokenDocument(
                deviceToken.getToken(), deviceToken.getUserId(),
                deviceToken.getPlatform(), deviceToken.getRegisteredAt()));
    }

    @Override
    public void deleteByToken(String token) {
        repository.deleteById(token);
    }

    @Override
    public List<String> findTokensByUser(UUID userId) {
        return repository.findByUserId(userId).stream().map(DeviceTokenDocument::getToken).toList();
    }
}
