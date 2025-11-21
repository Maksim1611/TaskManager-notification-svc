package app.service;

import app.model.NotificationPreference;
import app.repository.NotificationPreferenceRepository;
import app.web.dto.PreferenceRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;

    public PreferenceService(NotificationPreferenceRepository preferenceRepository) {
        this.preferenceRepository = preferenceRepository;
    }

    public NotificationPreference upsert(PreferenceRequest preferenceRequest) {

        Optional<NotificationPreference> preferenceOptional = preferenceRepository.findByUserId(preferenceRequest.getUserId());

        if (preferenceOptional.isPresent()) {
            NotificationPreference preference = preferenceOptional.get();
            preference.setDeadLineNotificationEnabled(preferenceRequest.isDeadLineNotificationEnabled());
            preference.setEmailNotificationEnabled(preferenceRequest.isEmailNotificationEnabled());
            preference.setSummaryNotificationEnabled(preferenceRequest.isSummaryNotificationEnabled());
            preference.setReminderNotificationEnabled(preferenceRequest.isReminderNotificationEnabled());
            preference.setEmail(preferenceRequest.getEmail());
            preference.setUpdatedOn(LocalDateTime.now());

            return preferenceRepository.save(preference);
        }

        NotificationPreference preference = NotificationPreference.builder()
                .userId(preferenceRequest.getUserId())
                .deadLineNotificationEnabled(preferenceRequest.isDeadLineNotificationEnabled())
                .emailNotificationEnabled(preferenceRequest.isEmailNotificationEnabled())
                .summaryNotificationEnabled(preferenceRequest.isSummaryNotificationEnabled())
                .reminderNotificationEnabled(preferenceRequest.isReminderNotificationEnabled())
                .email(preferenceRequest.getEmail())
                .updatedOn(LocalDateTime.now())
                .createdOn(LocalDateTime.now())
                .build();

        return preferenceRepository.save(preference);
    }

    public NotificationPreference getByUserId(UUID userId) {
        return preferenceRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Preference for this user does not exist!"));
    }
}
