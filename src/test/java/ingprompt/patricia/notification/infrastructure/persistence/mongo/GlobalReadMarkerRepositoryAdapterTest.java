package ingprompt.patricia.notification.infrastructure.persistence.mongo;

import ingprompt.patricia.notification.infrastructure.persistence.mongo.document.GlobalReadMarkerDocument;
import ingprompt.patricia.notification.infrastructure.persistence.mongo.repository.GlobalReadMarkerMongoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalReadMarkerRepositoryAdapterTest {

    @Mock
    private GlobalReadMarkerMongoRepository repository;

    private GlobalReadMarkerRepositoryAdapter adapter;

    @Test
    void findLastReadAt_present_mapsToInstant() {
        adapter = new GlobalReadMarkerRepositoryAdapter(repository);
        UUID userId = UUID.randomUUID();
        Instant lastReadAt = Instant.now();
        when(repository.findById(userId)).thenReturn(Optional.of(new GlobalReadMarkerDocument(userId, lastReadAt)));

        assertThat(adapter.findLastReadAt(userId)).contains(lastReadAt);
    }

    @Test
    void findLastReadAt_absent_returnsEmpty() {
        adapter = new GlobalReadMarkerRepositoryAdapter(repository);
        UUID userId = UUID.randomUUID();
        when(repository.findById(userId)).thenReturn(Optional.empty());

        assertThat(adapter.findLastReadAt(userId)).isEmpty();
    }

    @Test
    void setLastReadAt_savesDocumentWithUserAndTimestamp() {
        adapter = new GlobalReadMarkerRepositoryAdapter(repository);
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        adapter.setLastReadAt(userId, now);

        ArgumentCaptor<GlobalReadMarkerDocument> captor = ArgumentCaptor.forClass(GlobalReadMarkerDocument.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
        assertThat(captor.getValue().getLastReadAt()).isEqualTo(now);
    }
}
