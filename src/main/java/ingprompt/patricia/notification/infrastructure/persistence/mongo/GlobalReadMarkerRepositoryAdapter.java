package ingprompt.patricia.notification.infrastructure.persistence.mongo;

import ingprompt.patricia.notification.application.port.out.GlobalReadMarkerRepositoryOutPort;
import ingprompt.patricia.notification.infrastructure.persistence.mongo.document.GlobalReadMarkerDocument;
import ingprompt.patricia.notification.infrastructure.persistence.mongo.repository.GlobalReadMarkerMongoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
public class GlobalReadMarkerRepositoryAdapter implements GlobalReadMarkerRepositoryOutPort {
    private final GlobalReadMarkerMongoRepository repository;

    @Override
    public Optional<Instant> findLastReadAt(UUID userId) {
        return repository.findById(userId).map(GlobalReadMarkerDocument::getLastReadAt);
    }

    @Override
    public void setLastReadAt(UUID userId, Instant lastReadAt) {
        repository.save(new GlobalReadMarkerDocument(userId, lastReadAt));
    }
}
