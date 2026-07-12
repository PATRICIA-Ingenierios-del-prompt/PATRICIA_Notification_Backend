package ingprompt.patricia.notification.infrastructure.web;

import ingprompt.patricia.notification.application.port.in.ManageDeviceTokenCase;
import ingprompt.patricia.notification.domain.enums.DevicePlatform;
import ingprompt.patricia.notification.infrastructure.web.dto.request.RegisterDeviceRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeviceControllerTest {

    @Mock
    private ManageDeviceTokenCase manageDeviceTokenCase;

    private DeviceController controller;

    @Test
    void register_delegatesWithTokenAndPlatform() {
        controller = new DeviceController(manageDeviceTokenCase);
        UUID userId = UUID.randomUUID();
        RegisterDeviceRequest request = new RegisterDeviceRequest();
        request.setToken("ExponentPushToken[x]");
        request.setPlatform(DevicePlatform.ANDROID);

        ResponseEntity<Void> response = controller.register(userId, request);

        verify(manageDeviceTokenCase).registerDevice(userId, "ExponentPushToken[x]", DevicePlatform.ANDROID);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void unregister_delegatesWithToken() {
        controller = new DeviceController(manageDeviceTokenCase);
        UUID userId = UUID.randomUUID();

        ResponseEntity<Void> response = controller.unregister(userId, "tok-123");

        verify(manageDeviceTokenCase).unregisterDevice(userId, "tok-123");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
