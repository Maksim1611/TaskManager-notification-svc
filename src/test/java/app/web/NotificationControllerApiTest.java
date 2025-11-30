package app.web;

import app.model.Notification;
import app.model.NotificationType;
import app.service.NotificationService;
import app.web.dto.NotificationRequest;
import app.web.dto.NotificationResponse;
import app.web.mapper.DtoMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
public class NotificationControllerApiTest {

    @MockitoBean
    private NotificationService service;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sendNotification_shouldReturnCreatedResponse() throws Exception {
        NotificationRequest request = NotificationRequest.builder()
                .subject("Hello")
                .userId(UUID.randomUUID())
                .build();

        Notification savedNotification = Notification.builder()
                .id(UUID.randomUUID())
                .subject("Hello")
                .type(NotificationType.REMINDER)
                .createdOn(LocalDateTime.now())
                .build();

        when(service.sendNotification(any()))
                .thenReturn(savedNotification);

        MockHttpServletRequestBuilder httpRequest = post("/api/v1/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsBytes(request));

        mockMvc.perform(httpRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("subject").isNotEmpty())
                .andExpect(jsonPath("type").isNotEmpty())
                .andExpect(jsonPath("createdOn").isNotEmpty());
        verify(service).sendNotification(any());
    }

    @Test
    void getHistory_shouldReturnListOfNotificationResponsesAndStatus200() throws Exception {
        UUID userId = UUID.randomUUID();
        Notification n1 = randomNotification();
        Notification n2 = randomNotification();

        n1.setUserId(userId);
        n2.setUserId(userId);

        List<Notification> list = List.of(n1, n2);

        when(service.getHistory(any())).thenReturn(list);

        List<NotificationResponse> result = list.stream().map(DtoMapper::from)
                .sorted(Comparator.comparing(NotificationResponse::getCreatedOn).reversed()).toList();

        MockHttpServletRequestBuilder httpRequest = get("/api/v1/notifications")
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].subject").value("Hello"))
                .andExpect(jsonPath("$[1].subject").value("Hello"));
    }

    public Notification randomNotification() {
        return Notification.builder()
                .id(UUID.randomUUID())
                .subject("Hello")
                .type(NotificationType.REMINDER)
                .createdOn(LocalDateTime.now())
                .body("Test")
                .build();
    }

    @Test
    void deleteHistory_shouldReturnStatus200AndDelete() throws Exception {
        UUID userId = UUID.randomUUID();

        MockHttpServletRequestBuilder httpRequest = delete("/api/v1/notifications/history")
                .param("userId", userId.toString());

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(service).deleteHistory(any());
    }

}
