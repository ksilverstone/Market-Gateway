package yazlab.interceptor;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.security.Key;

@Component
public class JwtAuthAndLoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthAndLoggingInterceptor.class);
    
    // Auth Service'teki aynı anahtar kullanılmak zorundadır
    public static final String SECRET_KEY = "YazlabMicroservicesProjesiIcinCokGizliAnahtar!"; 

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Loglama için performans ölçümü başlangıcı
        request.setAttribute("startTime", System.currentTimeMillis());

        String uri = request.getRequestURI();

        // 1. İstisnalar (Oturum açma ve Sistem metrikleri yetki gerektirmez)
        if (uri.startsWith("/api/auth") || uri.startsWith("/actuator")) {
            return true;
        }

        // 2. JWT Yetki Kontrolü
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Missing or Invalid Authorization Header\"}");
            return false;
        }

        String token = authHeader.substring(7);
        try {
            Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
            // Token parse edilebiliyorsa ve expire olmamışsa imza geçerlidir
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid or Expired JWT Token\"}");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 3. Trafik Loglaması (Doküman İsteri)
        if (request.getAttribute("startTime") != null) {
            long startTime = (Long) request.getAttribute("startTime");
            long duration = System.currentTimeMillis() - startTime;
            
            // SLF4J kullanılarak konsola standart log çıkışı
            logger.info("TRAFFIC LOG -> Method: {} | URL: {} | Status: {} | Duration: {}ms", 
                    request.getMethod(), 
                    request.getRequestURI(), 
                    response.getStatus(), 
                    duration);
        }
    }
}
