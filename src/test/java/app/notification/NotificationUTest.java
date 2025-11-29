package app.notification;

import app.model.Notification;
import app.model.NotificationPreference;
import app.model.NotificationStatus;
import app.model.NotificationType;
import app.repository.NotificationRepository;
import app.service.NotificationService;
import app.service.PreferenceService;
import app.web.dto.NotificationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.lang.invoke.ConstantBootstraps.invoke;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class NotificationUTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private PreferenceService preferenceService;
    @Mock
    private MailSender mailSender;

    @Spy
    @InjectMocks
    private NotificationService notificationService;

    @Test
    void whenSendEmail_andEmailNotificationIsNotEnabled_thenThrowException() {
        UUID userId = UUID.randomUUID();

        NotificationRequest request = NotificationRequest.builder()
                .userId(userId)
                .type(NotificationType.EMAIL)
                .body("")
                .subject("")
                .build();

        NotificationPreference preference = NotificationPreference.builder()
                .userId(UUID.randomUUID())
                .emailNotificationEnabled(false)
                .build();

        when(preferenceService.getByUserId(userId)).thenReturn(preference);

        assertThrows(IllegalStateException.class, () -> notificationService.sendEmail(request));
    }

    @Test
    void whenSendEmail_andNotificationStatusIsSucceeded_thenPersistToDatabase() {
        UUID userId = UUID.randomUUID();

        NotificationRequest request = NotificationRequest.builder()
                .userId(userId)
                .type(NotificationType.EMAIL)
                .body("test")
                .subject("test")
                .build();

        NotificationPreference preference = NotificationPreference.builder()
                .userId(UUID.randomUUID())
                .email("ivan@gmail.com")
                .emailNotificationEnabled(true)
                .build();

        when(preferenceService.getByUserId(userId)).thenReturn(preference);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

        notificationService.sendEmail(request);
        verify(notificationRepository).save(captor.capture());

        Notification notification = captor.getValue();

        assertEquals(NotificationStatus.SUCCEEDED, notification.getStatus());
    }

    @Test
    void whenSendEmail_andNotificationStatusIsFailed_thenPersistToDatabase() {
        UUID userId = UUID.randomUUID();

        NotificationRequest request = NotificationRequest.builder()
                .userId(userId)
                .type(NotificationType.EMAIL)
                .body("test")
                .subject("test")
                .build();

        NotificationPreference preference = NotificationPreference.builder()
                .userId(UUID.randomUUID())
                .email("ivan@test.bg")
                .emailNotificationEnabled(true)
                .build();

        when(preferenceService.getByUserId(userId)).thenReturn(preference);

        doThrow(new RuntimeException("fail test")).when(mailSender)
                .send(any(SimpleMailMessage.class));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

        notificationService.sendEmail(request);
        verify(notificationRepository).save(captor.capture());

        Notification notification = captor.getValue();

        assertEquals(NotificationStatus.FAILED, notification.getStatus());
    }

    @Test
    void whenSendInAppNotification_andParticularNotificationIsNotEnabled_thenSetStatusToFailedAndPersistInTheDatabase() {
        UUID userId = UUID.randomUUID();

        NotificationRequest request = NotificationRequest.builder()
                .userId(userId)
                .type(NotificationType.REMINDER)
                .body("test")
                .subject("test")
                .build();

        NotificationPreference preference = NotificationPreference.builder()
                .userId(UUID.randomUUID())
                .email("ivan@test.bg")
                .reminderNotificationEnabled(false)
                .build();

        when(preferenceService.getByUserId(userId)).thenReturn(preference);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        notificationService.sendInAppNotification(request);

        verify(notificationRepository).save(captor.capture());
        Notification notification = captor.getValue();

        assertEquals(NotificationStatus.FAILED, notification.getStatus());
    }

    @Test
    void whenSendInAppNotification_andParticularNotificationIsEnabled_thenSetStatusToFailedAndPersistInTheDatabase() {
        UUID userId = UUID.randomUUID();

        NotificationRequest request = NotificationRequest.builder()
                .userId(userId)
                .type(NotificationType.REMINDER)
                .body("test")
                .subject("test")
                .build();

        NotificationPreference preference = NotificationPreference.builder()
                .userId(UUID.randomUUID())
                .email("ivan@test.bg")
                .reminderNotificationEnabled(true)
                .build();

        when(preferenceService.getByUserId(userId)).thenReturn(preference);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        notificationService.sendInAppNotification(request);

        verify(notificationRepository).save(captor.capture());
        Notification notification = captor.getValue();

        assertEquals(NotificationStatus.SUCCEEDED, notification.getStatus());
    }

    @Test
    void whenSendNotification_andNotificationTypeIsEmail_thenInvokeSendEmailMethod() {
        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.EMAIL)
                .build();

        Notification expected = new Notification();

        doReturn(expected).when(notificationService).sendEmail(request);

        Notification result = notificationService.sendNotification(request);

        assertSame(expected, result);
        verify(notificationService).sendEmail(request);
        verify(notificationService, never()).sendInAppNotification(any());
    }

    @Test
    void whenSendNotification_andNotificationTypeIsEmail_thenInvokeSendInAppNotificationMethod() {
        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.REMINDER)
                .build();

        Notification expected = new Notification();

        doReturn(expected).when(notificationService).sendInAppNotification(request);

        Notification result = notificationService.sendNotification(request);

        assertSame(expected, result);
        verify(notificationService).sendInAppNotification(request);
        verify(notificationService, never()).sendEmail(any());
    }

    @Test
    void whenDeleteHistory_shouldCallRepositoryWithCorrectUserId() {
        UUID userId = UUID.randomUUID();

        notificationService.deleteHistory(userId);

        verify(notificationRepository).deleteAllByUserId(userId);
    }

    @Test
    void whenGetHistory_andRepositoryReturnsEmptyList_thenReturnEmptyList() {
        List<Notification> emptyList = Collections.emptyList();

        when(notificationService.getHistory(any())).thenReturn(emptyList);

        List<Notification> result = notificationService.getHistory(UUID.randomUUID());
        assertSame(emptyList, result);
    }

    @Test
    void whenGetHistory_andRepositoryReturnsCorrectHistory() {
        UUID userId = UUID.randomUUID();

        Notification n1 = Notification.builder().userId(userId).build();
        Notification n2 = Notification.builder().userId(userId).build();

        List<Notification> list = List.of(n1, n2);

        when(notificationService.getHistory(any())).thenReturn(list);

        List<Notification> result = notificationService.getHistory(userId);

        assertSame(list, result);
    }
}
