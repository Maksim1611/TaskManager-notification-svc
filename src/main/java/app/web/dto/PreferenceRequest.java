package app.web.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PreferenceRequest {

    private UUID userId;

    private boolean emailNotificationEnabled;

    private boolean deadLineNotificationEnabled;

    private boolean summaryNotificationEnabled;

    private boolean reminderNotificationEnabled;

    private String email;

}
