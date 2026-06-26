package ingprompt.patricia.notification.infrastructure.persistence.mongo.repository;

import ingprompt.patricia.notification.infrastructure.persistence.mongo.document.GlobalReadMarkerDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface GlobalReadMarkerMongoRepository extends MongoRepository<GlobalReadMarkerDocument, UUID> {
}
