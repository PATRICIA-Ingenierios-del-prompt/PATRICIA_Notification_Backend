package ingprompt.patricia.notification.application.service;

import ingprompt.patricia.notification.application.port.in.ManageDeviceTokenCase;
import ingprompt.patricia.notification.application.port.out.DeviceTokenRepositoryOutPort;
import ingprompt.patricia.notification.domain.enums.DevicePlatform;
import ingprompt.patricia.notification.domain.model.DeviceToken;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class DeviceTokenService implements ManageDeviceTokenCase {
    private final DeviceTokenRepositoryOutPort deviceTokenRepository;

    @Override
    public void registerDevice(UUID userId, String token, DevicePlatform platform) {
        deviceTokenRepository.save(DeviceToken.register(userId, token, platform));
    }

    @Override
    public void unregisterDevice(UUID userId, String token) {
        deviceTokenRepository.deleteByToken(token);
    }
}
