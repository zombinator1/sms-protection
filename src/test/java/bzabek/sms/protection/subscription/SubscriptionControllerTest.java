package bzabek.sms.protection.subscription;

import bzabek.sms.protection.exceptions.SubscriptionsDatabaseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static bzabek.sms.protection.exceptions.RestControllerExceptionHandler.DB_FAILURE_MSG;
import static bzabek.sms.protection.exceptions.RestControllerExceptionHandler.UNEXPECTED_ERROR_MSG;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubscriptionController.class)
class SubscriptionControllerTest {

    SubscribeRequest SUBSCRIBE_REQ = new SubscribeRequest("123456789");
    UnsubscribeRequest UNSUBSCRIBE_REQ = new UnsubscribeRequest("123456789");
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private SubscriptionService subscriptionService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSubscribeSuccessfully() throws Exception {
        // when + then
        mockMvc.perform(post("/api/sms/subscribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(SUBSCRIBE_REQ)))
                .andExpect(status().isOk());

        verify(subscriptionService).subscribe(SUBSCRIBE_REQ.phoneNumber());
    }

    @Test
    void shouldUnsubscribeSuccessfully() throws Exception {
        // when + then
        mockMvc.perform(post("/api/sms/unsubscribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UNSUBSCRIBE_REQ)))
                .andExpect(status().isOk());

        verify(subscriptionService).unsubscribe(UNSUBSCRIBE_REQ.phoneNumber());
    }

    @Nested
    class ErrorResponse {

        @Test
        void databaseFailure() throws Exception {
            // Given
            Mockito.doThrow(new SubscriptionsDatabaseException("DB failure", null))
                    .when(subscriptionService).subscribe(SUBSCRIBE_REQ.phoneNumber());

            // When & Then
            mockMvc.perform(post("/api/sms/subscribe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(SUBSCRIBE_REQ)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value(
                            DB_FAILURE_MSG));
        }

        @Test
        void unexpectedFailure() throws Exception {
            // Given
            Mockito.doThrow(new RuntimeException("Unknown reason", null))
                    .when(subscriptionService).subscribe(SUBSCRIBE_REQ.phoneNumber());

            // When & Then
            mockMvc.perform(post("/api/sms/subscribe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(SUBSCRIBE_REQ)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value(
                            UNEXPECTED_ERROR_MSG));
        }
    }
}
