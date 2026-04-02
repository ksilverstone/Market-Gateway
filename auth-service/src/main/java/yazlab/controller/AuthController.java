package yazlab.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // RMM Seviye 2 Kuralları:
    // İşlem ismi (doLogin, makeLogin vb.) URL'de geçmez. 
    // Kaynak veya otorizasyon/session talebini ifade eden uygun HTTP metodu (POST) kullanılır.
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login() {
        // Şimdilik sadece sabit bir token dönüyoruz
        Map<String, String> response = new HashMap<>();
        response.put("token", "dummy-auth-token-12345");
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
