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
            .csrf(csrf -> csrf.disable()) // Desactiva CSRF (solo para pruebas)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/users/**").permitAll() // Permitir todas las rutas bajo /api/users
                .anyRequest().permitAll() // Permitir cualquier otra solicitud
            );
        return http.build();
    }
}
