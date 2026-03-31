package yazlab.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DispatcherController.class)
class DispatcherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    @DisplayName("TDD RED Phase: Dispatcher '/api/users/**' HTTP GET Route Testi (404 Verecek)")
    void whenRequestUserApi_thenRouteToUserService() throws Exception {
        // Arrange (Test Hazırlığı ve Mocklama)
        String mockInternalResponse = "{\"id\":1, \"name\":\"Yazlab Kullanici\"}";
        
        Mockito.when(restTemplate.exchange(
                eq("http://user-service:8080/api/users/1"),
                eq(HttpMethod.GET),
                any(),
                eq(String.class)
        )).thenReturn(ResponseEntity.ok(mockInternalResponse));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(mockInternalResponse));
    }
}
