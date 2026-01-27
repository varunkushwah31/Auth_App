package com.authApp.AuthApp_Backend.config;

import com.authApp.AuthApp_Backend.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeHttpRequest ->
                        authorizeHttpRequest
                        .requestMatchers("/api/v1/auth/register").permitAll()
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .anyRequest()
                        .authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(((request, response, authException) -> {
                        authException.printStackTrace();
                        response.setStatus(401);
                        response.setContentType("application.json");
                        String message = "unauthorized access" + authException.getMessage();
                        Map<String,String> errorMap = Map.of("message",message,"status", String.valueOf(401),"statusCode", Integer.toString(401));
                        var objectMapper = new ObjectMapper();
                        response.getWriter().write(objectMapper.writeValueAsString(errorMap));

                })))
                .addFilterBefore(jwtAuthenticationFilter,JwtAuthenticationFilter.class);

        return http.build();
    }
}
