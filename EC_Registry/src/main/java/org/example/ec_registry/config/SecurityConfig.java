package org.example.ec_registry.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .requiresChannel() // Forzar el uso de HTTPS
                .anyRequest()
                .requiresSecure() // Todas las solicitudes deben ser HTTPS
                .and()
                .authorizeHttpRequests()
                .anyRequest().permitAll() // Permitir acceso a todos los endpoints
                .and()
                .csrf().disable(); // Opcional: deshabilitar CSRF si no es necesario

        return http.build();
    }
}
