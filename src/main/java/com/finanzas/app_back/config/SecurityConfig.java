package com.finanzas.app_back.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.and()) // Habilitar CORS
            .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF si no es necesario
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Requerir autenticación para cualquier otra ruta
            );

        return http.build();
    }
}
