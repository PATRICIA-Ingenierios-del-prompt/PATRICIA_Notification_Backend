package ingprompt.patricia.notification.infrastructure.persistence.mongo.repository;

import ingprompt.patricia.notification.infrastructure.persistence.mongo.document.DeviceTokenDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface DeviceTokenMongoRepository extends MongoRepository<DeviceTokenDocument, String> {
    List<DeviceTokenDocument> findByUserId(UUID userId);
}
