package app.web.dto;

import app.model.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class NotificationRequest {

    private UUID userId;

    private String subject;

    private String body;

    private NotificationType type;
}
