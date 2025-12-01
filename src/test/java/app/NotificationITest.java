package app;

import app.model.Notification;
import app.model.NotificationPreference;
import app.model.NotificationStatus;
import app.model.NotificationType;
import app.repository.NotificationRepository;
import app.service.NotificationService;
import app.service.PreferenceService;
import app.web.dto.NotificationRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class NotificationITest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @MockitoBean
    private MailSender mailSender;

    @MockitoBean
    private PreferenceService preferenceService;

    @Test
    void sendEmail_whenEmailEnabled_thenNotificationSavedWithSuccess() {
        UUID userId = UUID.randomUUID();

        NotificationPreference pref = NotificationPreference.builder()
                .userId(userId)
                .email("maxim@gmail.com")
                .emailNotificationEnabled(true)
                .build();

        when(preferenceService.getByUserId(userId))
                .thenReturn(pref);

        doNothing()
                .when(mailSender).send(any(SimpleMailMessage.class));

        NotificationRequest request = NotificationRequest.builder()
                .userId(userId)
                .subject("Subject")
                .body("body")
                .build();

        Notification saved = notificationService.sendEmail(request);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.SUCCEEDED);

        verify(mailSender, Mockito.times(1))
                .send(any(SimpleMailMessage.class));

        Notification db = notificationRepository.findById(saved.getId()).orElseThrow();
        assertThat(db.getBody()).isEqualTo("body");
    }

    @Test
    void sendEmail_whenEmailDisabled_thenThrowsException_andNothingIsSaved() {
        UUID userId = UUID.randomUUID();

        NotificationPreference pref = NotificationPreference.builder()
                .userId(userId)
                .email("x@gmail.com")
                .emailNotificationEnabled(false)
                .build();

        when(preferenceService.getByUserId(userId))
                .thenReturn(pref);

        NotificationRequest request = NotificationRequest.builder()
                .userId(userId)
                .subject("Subject")
                .body("Body")
                .build();

        assertThrows(IllegalStateException.class, () -> {
            notificationService.sendEmail(request);
        });

        assertEquals(0, notificationRepository.findAllByUserId(userId).size());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmail_whenMailSenderFails_thenNotificationSavedWithFailedStatus() {
        UUID userId = UUID.randomUUID();

        NotificationPreference pref = NotificationPreference.builder()
                .userId(userId)
                .email("maxim@gmail.com")
                .emailNotificationEnabled(true)
                .build();

        when(preferenceService.getByUserId(userId))
                .thenReturn(pref);

        Mockito.doThrow(new RuntimeException())
                .when(mailSender).send(any(SimpleMailMessage.class));

        NotificationRequest request = NotificationRequest.builder()
                .userId(userId)
                .subject("Subject")
                .body("Body")
                .build();

        Notification saved = notificationService.sendEmail(request);

        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.FAILED);

        Notification result = notificationRepository.findById(saved.getId()).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(NotificationStatus.FAILED);
    }

}
