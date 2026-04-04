package yazlab.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import yazlab.interceptor.JwtAuthAndLoggingInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthAndLoggingInterceptor jwtAuthAndLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Tüm isteklere (/**) Auth ve Loglama interceptor'ını uygula
        registry.addInterceptor(jwtAuthAndLoggingInterceptor).addPathPatterns("/**");
    }
}
