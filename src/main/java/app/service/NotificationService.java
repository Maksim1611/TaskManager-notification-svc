package app.service;

import app.model.Notification;
import app.model.NotificationPreference;
import app.model.NotificationStatus;
import app.model.NotificationType;
import app.repository.NotificationRepository;
import app.web.dto.NotificationRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final PreferenceService preferenceService;
    private final MailSender mailSender;

    public NotificationService(NotificationRepository notificationRepository, PreferenceService preferenceService, MailSender mailSender) {
        this.notificationRepository = notificationRepository;
        this.preferenceService = preferenceService;
        this.mailSender = mailSender;
    }

    public Notification sendEmail(NotificationRequest notificationRequest) {

        NotificationPreference preference = preferenceService.getByUserId(notificationRequest.getUserId());

        boolean enabled = preference.isEmailNotificationEnabled();
        if (!enabled) {
            throw new IllegalStateException("User with id=[%s] turned of his notifications".formatted(notificationRequest.getUserId()));
        }

        Notification notification = Notification.builder()
                .subject(notificationRequest.getSubject())
                .body(notificationRequest.getBody())
                .createdOn(LocalDateTime.now())
                .type(NotificationType.EMAIL)
                .userId(notificationRequest.getUserId())
                .deleted(false)
                .build();

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(preference.getEmail());
        mailMessage.setSubject(notificationRequest.getSubject());
        mailMessage.setText(notificationRequest.getBody());

        try {
            mailSender.send(mailMessage);
            notification.setStatus(NotificationStatus.SUCCEEDED);
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            log.error("[S2S Call]: Failed due to %s".formatted(e.getMessage()));
        }

        return notificationRepository.save(notification);
    }

    public Notification sendInAppNotification(NotificationRequest notificationRequest) {
        NotificationPreference preference = preferenceService.getByUserId(notificationRequest.getUserId());

        Notification notification = Notification.builder()
                .subject(notificationRequest.getSubject())
                .body(notificationRequest.getBody())
                .createdOn(LocalDateTime.now())
                .type(notificationRequest.getType())
                .userId(notificationRequest.getUserId())
                .deleted(false)
                .status(checkNotificationStatus(notificationRequest, preference))
                .build();

        return notificationRepository.save(notification);
    }

    public List<Notification> getHistory(UUID userId) {
        return notificationRepository.findAllByUserId(userId).stream().filter(n -> !n.isDeleted()).collect(Collectors.toList());
    }

    public Notification sendNotification(NotificationRequest notificationRequest) {
        if (notificationRequest.getType().equals(NotificationType.EMAIL) ) {
            return sendEmail(notificationRequest);
        }

        return sendInAppNotification(notificationRequest);
    }

    public NotificationStatus checkNotificationStatus(NotificationRequest request, NotificationPreference preference) {
        if (request.getType().equals(NotificationType.DEADLINE) && preference.isDeadLineNotificationEnabled()
         || request.getType().equals(NotificationType.SUMMARY) && preference.isSummaryNotificationEnabled()
         || request.getType().equals(NotificationType.REMINDER) && preference.isReminderNotificationEnabled()
         || request.getType().equals(NotificationType.ALERT)) {
            return NotificationStatus.SUCCEEDED;
        }

        return NotificationStatus.FAILED;
    }

    @Transactional
    public void deleteHistory(UUID userId) {
        notificationRepository.deleteAllByUserId(userId);
    }
}
