package ingprompt.patricia.notification.infrastructure.persistence.mongo;

import com.mongodb.client.result.UpdateResult;
import ingprompt.patricia.notification.domain.enums.NotificationState;
import ingprompt.patricia.notification.domain.enums.NotificationType;
import ingprompt.patricia.notification.domain.model.UserNotification;
import ingprompt.patricia.notification.infrastructure.persistence.mongo.document.UserNotificationDocument;
import ingprompt.patricia.notification.infrastructure.persistence.mongo.repository.UserNotificationMongoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserNotificationRepositoryAdapterTest {

    @Mock
    private UserNotificationMongoRepository repository;
    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private UpdateResult updateResult;

    private UserNotificationRepositoryAdapter adapter;

    private UserNotificationDocument document(UUID id, UUID recipientId) {
        return new UserNotificationDocument(id, recipientId, NotificationType.NEW_MATCH_CONFIRMED, "m",
                Map.of(), NotificationState.UNREAD, Instant.now(), "evt", Instant.now().plusSeconds(3600));
    }

    @Test
    void save_persistsMappedDocument() {
        adapter = new UserNotificationRepositoryAdapter(repository, mongoTemplate);
        UserNotification notification = UserNotification.create(UUID.randomUUID(), NotificationType.NEW_MATCH_CONFIRMED,
                "hi", Map.of(), "evt", java.time.Duration.ofDays(90));

        adapter.save(notification);

        ArgumentCaptor<UserNotificationDocument> captor = ArgumentCaptor.forClass(UserNotificationDocument.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getMessage()).isEqualTo("hi");
    }

    @Test
    void findByIdAndRecipient_matchingRecipient_returnsMapped() {
        adapter = new UserNotificationRepositoryAdapter(repository, mongoTemplate);
        UUID id = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(document(id, recipientId)));

        Optional<UserNotification> result = adapter.findByIdAndRecipient(id, recipientId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
    }

    @Test
    void findByIdAndRecipient_wrongRecipient_returnsEmpty() {
        adapter = new UserNotificationRepositoryAdapter(repository, mongoTemplate);
        UUID id = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        UUID otherRecipient = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(document(id, otherRecipient)));

        assertThat(adapter.findByIdAndRecipient(id, recipientId)).isEmpty();
    }

    @Test
    void findByIdAndRecipient_notFound_returnsEmpty() {
        adapter = new UserNotificationRepositoryAdapter(repository, mongoTemplate);
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThat(adapter.findByIdAndRecipient(id, UUID.randomUUID())).isEmpty();
    }

    @Test
    void findRecentByRecipient_mapsDocumentsToDomain() {
        adapter = new UserNotificationRepositoryAdapter(repository, mongoTemplate);
        UUID recipientId = UUID.randomUUID();
        when(repository.findByRecipientIdOrderByCreatedAtDesc(eq(recipientId), any(Pageable.class)))
                .thenReturn(List.of(document(UUID.randomUUID(), recipientId)));

        List<UserNotification> result = adapter.findRecentByRecipient(recipientId, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRecipientId()).isEqualTo(recipientId);
    }

    @Test
    void countUnread_delegatesToRepository() {
        adapter = new UserNotificationRepositoryAdapter(repository, mongoTemplate);
        UUID recipientId = UUID.randomUUID();
        when(repository.countByRecipientIdAndState(recipientId, NotificationState.UNREAD)).thenReturn(4L);

        assertThat(adapter.countUnread(recipientId)).isEqualTo(4L);
    }

    @Test
    void markAllRead_updatesViaMongoTemplate_returnsModifiedCount() {
        adapter = new UserNotificationRepositoryAdapter(repository, mongoTemplate);
        UUID recipientId = UUID.randomUUID();
        when(mongoTemplate.updateMulti(any(Query.class), any(Update.class), eq(UserNotificationDocument.class)))
                .thenReturn(updateResult);
        when(updateResult.getModifiedCount()).thenReturn(3L);

        int updated = adapter.markAllRead(recipientId);

        assertThat(updated).isEqualTo(3);
    }

    @Test
    void existsBySourceEventAndRecipient_delegatesToRepository() {
        adapter = new UserNotificationRepositoryAdapter(repository, mongoTemplate);
        UUID recipientId = UUID.randomUUID();
        when(repository.existsBySourceEventIdAndRecipientId("evt", recipientId)).thenReturn(true);

        assertThat(adapter.existsBySourceEventAndRecipient("evt", recipientId)).isTrue();
    }
}
