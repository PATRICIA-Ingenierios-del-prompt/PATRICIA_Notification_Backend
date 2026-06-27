package ingprompt.patricia.notification.application.service;

import ingprompt.patricia.notification.application.port.out.DeviceTokenRepositoryOutPort;
import ingprompt.patricia.notification.domain.enums.DevicePlatform;
import ingprompt.patricia.notification.domain.model.DeviceToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeviceTokenServiceTest {

    @Mock
    private DeviceTokenRepositoryOutPort deviceTokenRepository;
    @InjectMocks
    private DeviceTokenService service;

    private final UUID userId = UUID.randomUUID();

    @Test
    void registerDevice_savesToken() {
        service.registerDevice(userId, "ExponentPushToken[abc]", DevicePlatform.ANDROID);

        ArgumentCaptor<DeviceToken> captor = ArgumentCaptor.forClass(DeviceToken.class);
        verify(deviceTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getToken()).isEqualTo("ExponentPushToken[abc]");
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
        assertThat(captor.getValue().getPlatform()).isEqualTo(DevicePlatform.ANDROID);
    }

    @Test
    void unregisterDevice_deletesByToken() {
        service.unregisterDevice(userId, "ExponentPushToken[abc]");
        verify(deviceTokenRepository).deleteByToken("ExponentPushToken[abc]");
    }
}
