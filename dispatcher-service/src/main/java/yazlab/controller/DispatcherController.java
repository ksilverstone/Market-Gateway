package yazlab.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Enumeration;

@RestController
@RequestMapping("/api/users")
public class DispatcherController {

    private final RestTemplate restTemplate;
    
    // Ağ İzolasyonu Kuralı Kapsamında Hedef Servis
    private static final String USER_SERVICE_BASE_URL = "http://user-service:8080/api/users";

    // Bağımlılık Enjeksiyonu
    public DispatcherController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @RequestMapping(value = "/**")
    public ResponseEntity<String> routeUserRequests(HttpServletRequest request, 
                                                    @RequestBody(required = false) String body) {
        try {
            // 1. Dinamik URI inşası
            String targetUri = buildTargetUri(request);
            
            // 2. Orijinal HTTP Metodunu yakalama
            HttpMethod method = HttpMethod.valueOf(request.getMethod());
            
            // 3. Client'tan gelen tüm Header'ları taşıma
            HttpHeaders headers = extractClientHeaders(request);
            
            // 4. İsteği zırhlandırma ve entegre etme
            HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);

            // 5. İstek User Service'e fırlatılır ve döner dönmez client'a paslanır (GREEN PHASE başarı noktası)
            return restTemplate.exchange(targetUri, method, httpEntity, String.class);
            
        } catch (HttpStatusCodeException e) {
            // Gelen orijinal hata durum kodunu, yanıt başlıklarını ve hata gövdesini olduğu gibi dışarı aktarılır.
            return ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsString());
            
        } catch (RestClientException e) {
            // Bağlantı reddedildi => 503 Service Unavailable döner.
            return ResponseEntity.status(503).body("{\"error\": \"User Service is currently unavailable or unreachable on the internal network.\"}");
        }
    }

    // --- OOP HELPER METOTLAR (Spagetti Kod Engelleme) ---

    private String buildTargetUri(HttpServletRequest request) {
        String requestUri = request.getRequestURI(); 
        String queryString = request.getQueryString(); 
        
        // Gelen orijinal URL'den '/api/users' prefix'ini temizleyip kalan suffix yolunu belirliyoruz. (Örn: /1)
        String pathSuffix = requestUri.replaceFirst("^/api/users", "");
        
        StringBuilder targetUrl = new StringBuilder(USER_SERVICE_BASE_URL).append(pathSuffix);
        
        if (queryString != null) {
            targetUrl.append("?").append(queryString);
        }
        return targetUrl.toString();
    }

    private HttpHeaders extractClientHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        
        while (headerNames != null && headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            // Host bilgisi Dispatcher üzerinden değiştiği ve hedef servise kaydığı için eski client hostunu temizliyoruz.
            if (!headerName.equalsIgnoreCase("host")) { 
                headers.add(headerName, request.getHeader(headerName));
            }
        }
        return headers;
    }
}
