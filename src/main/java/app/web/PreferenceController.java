package app.web;

import app.model.NotificationPreference;
import app.service.PreferenceService;
import app.web.dto.PreferenceRequest;
import app.web.dto.PreferenceResponse;
import app.web.mapper.DtoMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/preferences")
public class PreferenceController {

    private final PreferenceService preferenceService;

    public PreferenceController(PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @PostMapping
    public ResponseEntity<PreferenceResponse> upsertPreference(@RequestBody PreferenceRequest preferenceRequest) {

        NotificationPreference notificationPreference = preferenceService.upsert(preferenceRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.from(notificationPreference));
    }

    @GetMapping
    public ResponseEntity<PreferenceResponse> getPreferenceForUser(@RequestParam("userId") UUID userId) {
        NotificationPreference preference = preferenceService.getByUserId(userId);

        return ResponseEntity.ok(DtoMapper.from(preference));
    }
}
