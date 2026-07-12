package ingprompt.patricia.notification.infrastructure.web.dto.request;

import ingprompt.patricia.notification.domain.enums.DevicePlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterDeviceRequest {
    @NotBlank
    private String token;

    @NotNull
    private DevicePlatform platform;
}
