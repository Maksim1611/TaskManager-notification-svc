package app.preference;

import app.model.NotificationPreference;
import app.repository.NotificationPreferenceRepository;
import app.service.PreferenceService;
import app.web.dto.PreferenceRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.BDDAssertions.within;


@ExtendWith(MockitoExtension.class)
public class PreferenceUTest {

    @Mock
    private NotificationPreferenceRepository preferenceRepository;

    @InjectMocks
    private PreferenceService preferenceService;

    @Test
    void whenUpsertPreferences_andPreferenceIsPresent_thenUpdatePreferencesAndPersistToDatabase() {
        UUID userId = UUID.randomUUID();

        PreferenceRequest request = PreferenceRequest.builder()
                .deadLineNotificationEnabled(true)
                .emailNotificationEnabled(true)
                .summaryNotificationEnabled(true)
                .reminderNotificationEnabled(true)
                .email("ivan@gmail.com")
                .userId(userId)
                .build();

        NotificationPreference notificationPreference = NotificationPreference.builder()
                .deadLineNotificationEnabled(false)
                .emailNotificationEnabled(false)
                .summaryNotificationEnabled(false)
                .reminderNotificationEnabled(false)
                .email("ivan@gmail.com")
                .userId(userId)
                .build();
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(notificationPreference));

        preferenceService.upsert(request);

        assertTrue(notificationPreference.isDeadLineNotificationEnabled());
        assertTrue(notificationPreference.isEmailNotificationEnabled());
        assertTrue(notificationPreference.isSummaryNotificationEnabled());
        assertTrue(notificationPreference.isReminderNotificationEnabled());
        assertEquals(request.getEmail(), notificationPreference.getEmail());
        assertThat(notificationPreference.getUpdatedOn()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));

        verify(preferenceRepository).save(notificationPreference);
    }

    @Test
    void whenUpsertPreferences_andPreferenceIsEmpty_thenPersistPreferencesToDatabase() {
        UUID userId = UUID.randomUUID();

        PreferenceRequest request = PreferenceRequest.builder()
                .deadLineNotificationEnabled(true)
                .emailNotificationEnabled(true)
                .summaryNotificationEnabled(true)
                .reminderNotificationEnabled(true)
                .email("ivan@gmail.com")
                .userId(userId)
                .build();

        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());

        preferenceService.upsert(request);

        ArgumentCaptor<NotificationPreference> captor = ArgumentCaptor.forClass(NotificationPreference.class);

        verify(preferenceRepository).save(captor.capture());
        NotificationPreference saved = captor.getValue();

        assertTrue(saved.isDeadLineNotificationEnabled());
        assertTrue(saved.isEmailNotificationEnabled());
        assertTrue(saved.isSummaryNotificationEnabled());
        assertTrue(saved.isReminderNotificationEnabled());
        assertEquals(request.getEmail(), saved.getEmail());
        assertThat(saved.getUpdatedOn()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
        assertThat(saved.getCreatedOn()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));

    }

    @Test
    void whenGetPreferenceByUserId_andRepositoryReturnsOptionalEmpty_thenThrowException() {
        UUID userId = UUID.randomUUID();

        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> preferenceService.getByUserId(userId));
    }

}
