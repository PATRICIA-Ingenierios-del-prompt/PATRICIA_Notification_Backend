package ingprompt.patricia.notification.infrastructure.web;

import ingprompt.patricia.notification.application.port.in.ManageDeviceTokenCase;
import ingprompt.patricia.notification.infrastructure.web.dto.request.RegisterDeviceRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/notifications/devices")
@AllArgsConstructor
public class DeviceController {
    private final ManageDeviceTokenCase manageDeviceTokenCase;

    @PostMapping
    public ResponseEntity<Void> register(@RequestHeader("X-User-Id") UUID userId, @Valid @RequestBody RegisterDeviceRequest request) {
        manageDeviceTokenCase.registerDevice(userId, request.getToken(), request.getPlatform());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{token}")
    public ResponseEntity<Void> unregister(@RequestHeader("X-User-Id") UUID userId, @PathVariable String token) {
        manageDeviceTokenCase.unregisterDevice(userId, token);
        return ResponseEntity.noContent().build();
    }
}
