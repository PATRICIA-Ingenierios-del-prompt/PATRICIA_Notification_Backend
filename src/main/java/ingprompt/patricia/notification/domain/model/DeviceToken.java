package ingprompt.patricia.notification.domain.model;

import ingprompt.patricia.notification.domain.enums.DevicePlatform;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;


@Getter
public class DeviceToken {
    private final UUID userId;
    private final String token;
    private final DevicePlatform platform;
    private final Instant registeredAt;

    private DeviceToken(UUID userId, String token, DevicePlatform platform, Instant registeredAt) {
        this.userId = userId;
        this.token = token;
        this.platform = platform;
        this.registeredAt = registeredAt;
    }

    public static DeviceToken register(UUID userId, String token, DevicePlatform platform) {
        return new DeviceToken(userId, token, platform, Instant.now());
    }

    public static DeviceToken rehydrate(UUID userId, String token, DevicePlatform platform, Instant registeredAt) {
        return new DeviceToken(userId, token, platform, registeredAt);
    }
}
