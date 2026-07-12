package ingprompt.patricia.notification.infrastructure.persistence.mongo;

import ingprompt.patricia.notification.domain.enums.DevicePlatform;
import ingprompt.patricia.notification.domain.model.DeviceToken;
import ingprompt.patricia.notification.infrastructure.persistence.mongo.document.DeviceTokenDocument;
import ingprompt.patricia.notification.infrastructure.persistence.mongo.repository.DeviceTokenMongoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceTokenRepositoryAdapterTest {

    @Mock
    private DeviceTokenMongoRepository repository;

    private DeviceTokenRepositoryAdapter adapter;

    @Test
    void save_persistsDocumentWithMappedFields() {
        adapter = new DeviceTokenRepositoryAdapter(repository);
        UUID userId = UUID.randomUUID();
        DeviceToken token = DeviceToken.register(userId, "ExponentPushToken[a]", DevicePlatform.ANDROID);

        adapter.save(token);

        ArgumentCaptor<DeviceTokenDocument> captor = ArgumentCaptor.forClass(DeviceTokenDocument.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getToken()).isEqualTo("ExponentPushToken[a]");
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
        assertThat(captor.getValue().getPlatform()).isEqualTo(DevicePlatform.ANDROID);
    }

    @Test
    void deleteByToken_delegatesToRepositoryDeleteById() {
        adapter = new DeviceTokenRepositoryAdapter(repository);
        adapter.deleteByToken("tok-123");
        verify(repository).deleteById("tok-123");
    }

    @Test
    void findTokensByUser_mapsDocumentsToTokenStrings() {
        adapter = new DeviceTokenRepositoryAdapter(repository);
        UUID userId = UUID.randomUUID();
        DeviceTokenDocument d1 = new DeviceTokenDocument("tok-1", userId, DevicePlatform.IOS, Instant.now());
        DeviceTokenDocument d2 = new DeviceTokenDocument("tok-2", userId, DevicePlatform.ANDROID, Instant.now());
        when(repository.findByUserId(userId)).thenReturn(List.of(d1, d2));

        List<String> tokens = adapter.findTokensByUser(userId);

        assertThat(tokens).containsExactly("tok-1", "tok-2");
    }

    @Test
    void findTokensByUser_noDevices_returnsEmptyList() {
        adapter = new DeviceTokenRepositoryAdapter(repository);
        UUID userId = UUID.randomUUID();
        when(repository.findByUserId(userId)).thenReturn(List.of());

        assertThat(adapter.findTokensByUser(userId)).isEmpty();
    }
}
