package ingprompt.patricia.notification.infrastructure.persistence.mongo;

import ingprompt.patricia.notification.application.port.out.UserNotificationRepositoryOutPort;
import ingprompt.patricia.notification.domain.enums.NotificationState;
import ingprompt.patricia.notification.domain.model.UserNotification;
import ingprompt.patricia.notification.infrastructure.persistence.mongo.mapper.NotificationDocumentMapper;
import ingprompt.patricia.notification.infrastructure.persistence.mongo.document.UserNotificationDocument;
import ingprompt.patricia.notification.infrastructure.persistence.mongo.repository.UserNotificationMongoRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
public class UserNotificationRepositoryAdapter implements UserNotificationRepositoryOutPort {
    private final UserNotificationMongoRepository repository;
    private final MongoTemplate mongoTemplate;

    @Override
    public void save(UserNotification notification) {
        repository.save(NotificationDocumentMapper.toDocument(notification));
    }

    @Override
    public Optional<UserNotification> findByIdAndRecipient(UUID id, UUID recipientId) {
        return repository.findById(id)
                .filter(d -> d.getRecipientId().equals(recipientId))
                .map(NotificationDocumentMapper::toDomain);
    }

    @Override
    public List<UserNotification> findRecentByRecipient(UUID recipientId, int limit) {
        return repository.findByRecipientIdOrderByCreatedAtDesc(recipientId, PageRequest.of(0, limit))
                .stream().map(NotificationDocumentMapper::toDomain).toList();
    }

    @Override
    public long countUnread(UUID recipientId) {
        return repository.countByRecipientIdAndState(recipientId, NotificationState.UNREAD);
    }

    @Override
    public int markAllRead(UUID recipientId) {
        Query query = new Query(Criteria.where("recipientId").is(recipientId).and("state").is(NotificationState.UNREAD));
        Update update = new Update().set("state", NotificationState.READ);
        return (int) mongoTemplate.updateMulti(query, update, UserNotificationDocument.class).getModifiedCount();
    }

    @Override
    public boolean existsBySourceEventAndRecipient(String sourceEventId, UUID recipientId) {
        return repository.existsBySourceEventIdAndRecipientId(sourceEventId, recipientId);
    }
}
