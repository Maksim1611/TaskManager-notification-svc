package app.web;

import app.model.Notification;
import app.service.NotificationService;
import app.web.dto.NotificationRequest;
import app.web.dto.NotificationResponse;
import app.web.mapper.DtoMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> sendNotification(@RequestBody NotificationRequest notificationRequest) {

        Notification notification = notificationService.sendNotification(notificationRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.from(notification));
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getHistory(@RequestParam("userId") UUID userId) {
        List<Notification> notifications = notificationService.getHistory(userId);
        List<NotificationResponse> responses = notifications.stream().map(DtoMapper::from)
                .sorted(Comparator.comparing(NotificationResponse::getCreatedOn).reversed()).collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/history")
    public ResponseEntity<Void> deleteHistory(@RequestParam("userId") UUID userId) {
        notificationService.deleteHistory(userId);

        return ResponseEntity.ok().build();
    }
}
