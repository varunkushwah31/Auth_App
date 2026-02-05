package com.authApp.AuthApp_Backend.security;

import com.authApp.AuthApp_Backend.entities.Provider;
import com.authApp.AuthApp_Backend.entities.RefreshToken;
import com.authApp.AuthApp_Backend.entities.User;
import com.authApp.AuthApp_Backend.repository.RefreshTokenRepository;
import com.authApp.AuthApp_Backend.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.rmi.RemoteException;
import java.time.Instant;
import java.util.UUID;

@Component
@AllArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Authentication authentication) throws IOException, ServletException {
        logger.info("Successful authentication");
        logger.info(authentication.toString());

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String registrationId = "unknown";
        if (authentication instanceof OAuth2AuthenticationToken token){
            registrationId = token.getAuthorizedClientRegistrationId();
        }
        logger.info("registrationId: {}", registrationId);
        logger.info("user: %s".formatted(oAuth2User.getAttributes().toString())) ;

        User user;
        switch (registrationId){
            case "google" -> {
                String googleId = oAuth2User.getAttributes().getOrDefault("sub","").toString();
                String email = oAuth2User.getAttributes().getOrDefault("email","").toString();
                String name = oAuth2User.getAttributes().getOrDefault("name","").toString();
                String image = oAuth2User.getAttributes().getOrDefault("picture","").toString();
                user = User.builder()
                        .email(email)
                        .name(name)
                        .image(image)
                        .provider(Provider.GOOGLE)
                        .enable(true)
                        .build();
                userRepository.findByEmail(email).ifPresentOrElse(user1 -> {
                    logger.info("user is there in database");
                    logger.info(user.toString());
                    }, () -> userRepository.save(user));
            }
            default -> throw new RemoteException("Invalid registration id");
        }

        String jti = UUID.randomUUID().toString();
        RefreshToken refreshTokenOb = RefreshToken
                .builder()
                .jti(jti)
                .revoked(false)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .build();

        refreshTokenRepository.save(refreshTokenOb);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user,refreshTokenOb.getJti());
        cookieService.attachRefreshCookie(response,refreshToken,(int) jwtService.getRefreshTtlSeconds());

        response.getWriter().write("Login successful");
    }
}
