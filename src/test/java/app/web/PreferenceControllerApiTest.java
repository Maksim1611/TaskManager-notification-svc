package app.web;

import app.model.NotificationPreference;
import app.service.PreferenceService;
import app.web.dto.PreferenceRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PreferenceController.class)
public class PreferenceControllerApiTest {

    @MockitoBean
    private PreferenceService service;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void postUpsertPreference_shouldReturn201CreatedAndUpsertPreferences() throws Exception {
        UUID userId = UUID.randomUUID();

        PreferenceRequest request = PreferenceRequest.builder()
                .userId(userId)
                .emailNotificationEnabled(true)
                .deadLineNotificationEnabled(true)
                .reminderNotificationEnabled(true)
                .summaryNotificationEnabled(true)
                .email("maxim@gmail.com")
                .build();

        NotificationPreference preference = NotificationPreference.builder()
                .id(UUID.randomUUID())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .emailNotificationEnabled(true)
                .deadLineNotificationEnabled(false)
                .reminderNotificationEnabled(true)
                .summaryNotificationEnabled(false)
                .email("maxim@gmail.com")
                .build();

        when(service.upsert(any())).thenReturn(preference);

        MockHttpServletRequestBuilder httpRequest = post("/api/v1/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsBytes(request));

        mockMvc.perform(httpRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("emailNotificationEnabled").isNotEmpty())
                .andExpect(jsonPath("deadLineNotificationEnabled").isNotEmpty())
                .andExpect(jsonPath("summaryNotificationEnabled").isNotEmpty())
                .andExpect(jsonPath("reminderNotificationEnabled").isNotEmpty())
                .andExpect(jsonPath("email").isNotEmpty())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(service).upsert(any());
    }

    @Test
    void getPreferenceForUser_shouldReturnPreferenceAndStatus200Ok() throws Exception {
        UUID userId = UUID.randomUUID();

        NotificationPreference preference = NotificationPreference.builder()
                .id(UUID.randomUUID())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .emailNotificationEnabled(true)
                .deadLineNotificationEnabled(false)
                .reminderNotificationEnabled(true)
                .summaryNotificationEnabled(false)
                .email("maxim@gmail.com")
                .build();

        when(service.getByUserId(any())).thenReturn(preference);

        MockHttpServletRequestBuilder httpRequest = get("/api/v1/preferences")
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("emailNotificationEnabled").isNotEmpty())
                .andExpect(jsonPath("deadLineNotificationEnabled").isNotEmpty())
                .andExpect(jsonPath("summaryNotificationEnabled").isNotEmpty())
                .andExpect(jsonPath("reminderNotificationEnabled").isNotEmpty())
                .andExpect(jsonPath("email").isNotEmpty());

        verify(service).getByUserId(any());
    }


}
