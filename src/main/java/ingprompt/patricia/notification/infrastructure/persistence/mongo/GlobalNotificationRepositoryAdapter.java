package ingprompt.patricia.notification.infrastructure.persistence.mongo;

import ingprompt.patricia.notification.application.port.out.GlobalNotificationRepositoryOutPort;
import ingprompt.patricia.notification.domain.model.GlobalNotification;
import ingprompt.patricia.notification.infrastructure.persistence.mongo.mapper.NotificationDocumentMapper;
import ingprompt.patricia.notification.infrastructure.persistence.mongo.repository.GlobalNotificationMongoRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@AllArgsConstructor
public class GlobalNotificationRepositoryAdapter implements GlobalNotificationRepositoryOutPort {
    private final GlobalNotificationMongoRepository repository;

    @Override
    public void save(GlobalNotification notification) {
        repository.save(NotificationDocumentMapper.toDocument(notification));
    }

    @Override
    public List<GlobalNotification> findRecent(int limit) {
        return repository.findByOrderByCreatedAtDesc(PageRequest.of(0, limit))
                .stream().map(NotificationDocumentMapper::toDomain).toList();
    }

    @Override
    public long countCreatedAfter(Instant after) {
        return repository.countByCreatedAtAfter(after);
    }

    @Override
    public boolean existsBySourceEvent(String sourceEventId) {
        return repository.existsBySourceEventId(sourceEventId);
    }
}
