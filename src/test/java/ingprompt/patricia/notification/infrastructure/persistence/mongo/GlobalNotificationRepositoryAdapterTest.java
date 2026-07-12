package ingprompt.patricia.notification.infrastructure.persistence.mongo;

import ingprompt.patricia.notification.domain.enums.NotificationType;
import ingprompt.patricia.notification.domain.model.GlobalNotification;
import ingprompt.patricia.notification.infrastructure.persistence.mongo.document.GlobalNotificationDocument;
import ingprompt.patricia.notification.infrastructure.persistence.mongo.repository.GlobalNotificationMongoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalNotificationRepositoryAdapterTest {

    @Mock
    private GlobalNotificationMongoRepository repository;

    private GlobalNotificationRepositoryAdapter adapter;

    @Test
    void save_persistsMappedDocument() {
        adapter = new GlobalNotificationRepositoryAdapter(repository);
        GlobalNotification notification = GlobalNotification.create(NotificationType.NEW_PUBLIC_PARCHE,
                "g", Map.of("parcheId", "p"), "evt", Duration.ofDays(90));

        adapter.save(notification);

        ArgumentCaptor<GlobalNotificationDocument> captor = ArgumentCaptor.forClass(GlobalNotificationDocument.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getMessage()).isEqualTo("g");
        assertThat(captor.getValue().getSourceEventId()).isEqualTo("evt");
    }

    @Test
    void findRecent_mapsDocumentsToDomain() {
        adapter = new GlobalNotificationRepositoryAdapter(repository);
        GlobalNotificationDocument doc = new GlobalNotificationDocument(UUID.randomUUID(), NotificationType.NEW_PUBLIC_PARCHE,
                "g", Map.of(), Instant.now(), "evt", Instant.now().plusSeconds(3600));
        when(repository.findByOrderByCreatedAtDesc(any())).thenReturn(List.of(doc));

        List<GlobalNotification> result = adapter.findRecent(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMessage()).isEqualTo("g");
    }

    @Test
    void countCreatedAfter_delegatesToRepository() {
        adapter = new GlobalNotificationRepositoryAdapter(repository);
        Instant after = Instant.now();
        when(repository.countByCreatedAtAfter(after)).thenReturn(5L);

        assertThat(adapter.countCreatedAfter(after)).isEqualTo(5L);
    }

    @Test
    void existsBySourceEvent_delegatesToRepository() {
        adapter = new GlobalNotificationRepositoryAdapter(repository);
        when(repository.existsBySourceEventId("evt")).thenReturn(true);

        assertThat(adapter.existsBySourceEvent("evt")).isTrue();
    }
}
