package ingprompt.patricia.notification.infrastructure.persistence.mongo.repository;

import ingprompt.patricia.notification.domain.enums.NotificationState;
import ingprompt.patricia.notification.infrastructure.persistence.mongo.document.UserNotificationDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface UserNotificationMongoRepository extends MongoRepository<UserNotificationDocument, UUID> {
    List<UserNotificationDocument> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId, Pageable pageable);

    long countByRecipientIdAndState(UUID recipientId, NotificationState state);

    boolean existsBySourceEventIdAndRecipientId(String sourceEventId, UUID recipientId);
}
