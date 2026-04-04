package yazlab.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Gerçek bir senaryoda bu gizli anahtar application.yml gibi güvenli bir ortamdan okunur
    // Secret en az 256 bit uzunluğunda olmalıdır (HS256 algoritması için)
    public static final String SECRET_KEY = "YazlabMicroservicesProjesiIcinCokGizliAnahtar!"; 
    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody(required = false) Map<String, String> credentials) {
        
        // Örnek doğrulama (Aslında burada DB'den kullanıcı kontrol edilir)
        String username = credentials != null && credentials.containsKey("username") 
                ? credentials.get("username") 
                : "guest_user";
        
        // Gerçek JWT (JSON Web Token) üretimi
        String jwtToken = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 saat geçerlilik
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // RMM Seviye 2 kuralına binaen response payload
        Map<String, String> response = new HashMap<>();
        response.put("token", jwtToken);
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
