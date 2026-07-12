package ingprompt.patricia.notification.application.port.in;

import ingprompt.patricia.notification.domain.enums.DevicePlatform;

import java.util.UUID;

public interface ManageDeviceTokenCase {
    void registerDevice(UUID userId, String token, DevicePlatform platform);
    void unregisterDevice(UUID userId, String token);
}
