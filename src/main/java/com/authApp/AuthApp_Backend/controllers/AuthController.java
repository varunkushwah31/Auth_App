package com.authApp.AuthApp_Backend.controllers;

import com.authApp.AuthApp_Backend.dtos.LoginRequest;
import com.authApp.AuthApp_Backend.dtos.RefreshTokenRequest;
import com.authApp.AuthApp_Backend.dtos.TokenResponse;
import com.authApp.AuthApp_Backend.dtos.UserDto;
import com.authApp.AuthApp_Backend.entities.RefreshToken;
import com.authApp.AuthApp_Backend.entities.User;
import com.authApp.AuthApp_Backend.repository.RefreshTokenRepository;
import com.authApp.AuthApp_Backend.repository.UserRepository;
import com.authApp.AuthApp_Backend.security.CookieService;
import com.authApp.AuthApp_Backend.security.JwtService;
import com.authApp.AuthApp_Backend.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieService cookieService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse response){

        //authenticate
        Authentication authentication = authenticate(loginRequest);
        User user = userRepository.findByEmail(loginRequest.email()).orElseThrow(() -> new BadCredentialsException("User does not Exist!!!"));

        if(!user.isEnable()){
            throw new DisabledException("User is disabled");
        }

        String jti = UUID.randomUUID().toString();
        RefreshToken refreshTokenObject = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .revoked(false)
                .build();


        //Refresh Token Save --Information
        refreshTokenRepository.save(refreshTokenObject);


        //access token generated

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user,refreshTokenObject.getJti());


        //use cookie service to attach refresh token in cookie

        cookieService.attachRefreshCookie(response,refreshToken,(int) jwtService.getRefreshTtlSeconds());
        cookieService.addNoStoreHeaders(response);



        TokenResponse tokenResponse = TokenResponse.of(accessToken,refreshToken,jwtService.getAccessTtlSeconds(),modelMapper.map(user, UserDto.class));

        return ResponseEntity.ok(tokenResponse);

    }

    private @NonNull Authentication authenticate(LoginRequest loginRequest) {
        try {
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(),loginRequest.password()));
        } catch (Exception e) {
            throw new BadCredentialsException("Username or Password Invalid");
        }
    }


    // for access and refresh token review

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            @RequestBody(required = false)RefreshTokenRequest body,
            HttpServletResponse response,
            HttpServletRequest request
    )
    {
        String refreshToken = readRefreshTokenFromRequest(body,request).orElseThrow(() -> new BadCredentialsException("Refresh Token is Missing!!!"));
        if (!jwtService.isRefreshToken(refreshToken)){
            throw new BadCredentialsException("Invalid Refresh Token Type!!");
        }

        String jti = jwtService.getJti(refreshToken);
        UUID userId = jwtService.getUserId(refreshToken);
        RefreshToken storedRefreshToken = refreshTokenRepository.findByJti(jti).orElseThrow(() -> new BadCredentialsException("Refresh Token Not Recognized"));
        if (storedRefreshToken.isRevoked()){
            throw new BadCredentialsException("Refresh token expired or revoked");
        }
        if (storedRefreshToken.getExpiresAt().isBefore(Instant.now())){
            throw new BadCredentialsException("Refresh token Expired!!");
        }
        if (!storedRefreshToken.getUser().getId().equals(userId)){
            throw new BadCredentialsException("Refresh token does not belong to this User!!");
        }
        // rotate refresh token:
        storedRefreshToken.setRevoked(true);

        String newJti = UUID.randomUUID().toString();
        storedRefreshToken.setReplacedByToken(newJti);
        refreshTokenRepository.save(storedRefreshToken);

        User user = storedRefreshToken.getUser();

        var newRefreshTokenOb = RefreshToken.builder()
                .jti(newJti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .revoked(false)
                .build();

        refreshTokenRepository.save(newRefreshTokenOb);
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user,newRefreshTokenOb.getJti());

        cookieService.attachRefreshCookie(response,newRefreshToken,(int) jwtService.getRefreshTtlSeconds());
        cookieService.addNoStoreHeaders(response);
        return ResponseEntity.ok(TokenResponse.of(newAccessToken,newRefreshToken,jwtService.getAccessTtlSeconds(),modelMapper.map(user,UserDto.class)));

    }

    private Optional<String> readRefreshTokenFromRequest(RefreshTokenRequest body, HttpServletRequest request) {

        // 1. prefer reading refresh token from cookie
        if (request.getCookies() != null){
            Optional<String> fromCookie = Arrays.stream(request.getCookies())
                    .filter(c -> cookieService.getRefreshTokenCookieName().equals(c.getName()))
                    .map(Cookie::getValue)
                    .filter(v -> v != null && !v.isBlank())
                    .findFirst();
            if (fromCookie.isPresent()){
                return fromCookie;
            }
        }

        // 2. body:
        if (body != null && body.refreshToken() != null && !body.refreshToken().isBlank()){
            return Optional.of(body.refreshToken());
        }

        //3. custom header
        String refreshHeader = request.getHeader("X-Refresh-Token");
        if (refreshHeader != null && !refreshHeader.isBlank()){
            return Optional.of(refreshHeader.trim());
        }

        //Authorization = Bearer <Token>
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.regionMatches(true,0,"Bearer ",0,7)){
            String candidate = authHeader.substring(7).trim();
            if (!candidate.isEmpty()){
                try {
                    if (jwtService.isRefreshToken(candidate)){
                        return Optional.of(candidate);
                    }
                }catch (Exception ignored){

                }
            }
        }
        return Optional.empty();
    }

    @PostMapping("/register")
    ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDto));
    }
}
