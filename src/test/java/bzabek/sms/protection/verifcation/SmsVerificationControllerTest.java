package bzabek.sms.protection.verifcation;

import bzabek.sms.protection.exceptions.SubscriptionsDatabaseException;
import bzabek.sms.protection.exceptions.WebRiskClientException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static bzabek.sms.protection.exceptions.RestControllerExceptionHandler.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SmsVerificationController.class)
class SmsVerificationControllerTest {

    private final SmsRequest TEST_SMS_REQ = new SmsRequest("123456789", "987654321", "Hello, visit: http://test.com");
    @MockitoBean
    public SmsSecurityService smsSecurityService;

    @Autowired
    public ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @Nested
    class OkResponse {
        @Test
        void safe() throws Exception {
            // Given
            Mockito.when(smsSecurityService.isPhishingSafe(any(SMS.class)))
                    .thenReturn(true); // not malicious

            // Then
            mockMvc.perform(post("/api/sms/check-phishing")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(TEST_SMS_REQ)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSafe").value(true))
                    .andExpect(jsonPath("$.reason").value("SAFE"));
        }

        @Test
        void malicious() throws Exception {
            // Given
            Mockito.when(smsSecurityService.isPhishingSafe(any(SMS.class)))
                    .thenReturn(false);

            // Then
            mockMvc.perform(post("/api/sms/check-phishing")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(TEST_SMS_REQ)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSafe").value(false))
                    .andExpect(jsonPath("$.reason").value("SOCIAL_ENGINEERING"));
        }
    }

    @Nested
    class ErrorResponse {

        @Test
        void webRiskClientFailure() throws Exception {
            // Given
            Mockito.when(smsSecurityService.isPhishingSafe(any(SMS.class)))
                    .thenThrow(new WebRiskClientException("WebRisk failed", null));

            // Then
            mockMvc.perform(post("/api/sms/check-phishing")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(TEST_SMS_REQ)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value(
                            CLIENT_FAILURE_MSG));
        }

        @Test
        void databaseFailure() throws Exception {
            // Given
            Mockito.when(smsSecurityService.isPhishingSafe(any(SMS.class)))
                    .thenThrow(new SubscriptionsDatabaseException("DB failure", null));

            // Then
            mockMvc.perform(post("/api/sms/check-phishing")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(TEST_SMS_REQ)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value(
                            DB_FAILURE_MSG));
        }

        @Test
        void unexpectedFailure() throws Exception {
            // Given
            Mockito.when(smsSecurityService.isPhishingSafe(any(SMS.class)))
                    .thenThrow(new RuntimeException("Error"));

            // Then
            mockMvc.perform(post("/api/sms/check-phishing")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(TEST_SMS_REQ)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value(
                            UNEXPECTED_ERROR_MSG));
        }
    }
}
