package ingprompt.patricia.notification.infrastructure.persistence.mongo.repository;

import ingprompt.patricia.notification.infrastructure.persistence.mongo.document.GlobalNotificationDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface GlobalNotificationMongoRepository extends MongoRepository<GlobalNotificationDocument, UUID> {
    List<GlobalNotificationDocument> findByOrderByCreatedAtDesc(Pageable pageable);

    long countByCreatedAtAfter(Instant after);

    boolean existsBySourceEventId(String sourceEventId);
}
