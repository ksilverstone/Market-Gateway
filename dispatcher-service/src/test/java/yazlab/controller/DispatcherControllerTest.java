package yazlab.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

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

import java.security.Key;
import java.util.Date;

import yazlab.interceptor.JwtAuthAndLoggingInterceptor;
import yazlab.config.WebConfig;
import org.springframework.context.annotation.Import;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DispatcherController.class)
@Import({WebConfig.class, JwtAuthAndLoggingInterceptor.class})
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
                eq("http://user-service:8082/api/users/1"),
                eq(HttpMethod.GET),
                any(),
                eq(String.class)
        )).thenReturn(ResponseEntity.ok(mockInternalResponse));

        // JWT Token Üretimi (Geçerli)
        Key key = Keys.hmacShaKeyFor(JwtAuthAndLoggingInterceptor.SECRET_KEY.getBytes());
        String validToken = Jwts.builder()
                .setSubject("testuser")
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        mockMvc.perform(get("/api/users/1").header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(content().json(mockInternalResponse));
    }
}
