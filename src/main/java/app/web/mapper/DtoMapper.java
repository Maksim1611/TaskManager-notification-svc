package app.web.mapper;

import app.model.Notification;
import app.model.NotificationPreference;
import app.web.dto.NotificationResponse;
import app.web.dto.PreferenceResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public static PreferenceResponse from(NotificationPreference preference) {

        return PreferenceResponse.builder()
                .email(preference.getEmail())
                .emailNotificationEnabled(preference.isEmailNotificationEnabled())
                .summaryNotificationEnabled(preference.isSummaryNotificationEnabled())
                .deadLineNotificationEnabled(preference.isDeadLineNotificationEnabled())
                .reminderNotificationEnabled(preference.isReminderNotificationEnabled())
                .build();
    }

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .subject(notification.getSubject())
                .createdOn(notification.getCreatedOn())
                .type(notification.getType())
                .status(notification.getStatus())
                .build();
    }
}
