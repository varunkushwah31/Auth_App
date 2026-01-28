package com.authApp.AuthApp_Backend.security;

import com.authApp.AuthApp_Backend.helper.UserHelper;
import com.authApp.AuthApp_Backend.repository.UserRepository;
import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private static final Logger logger =
            LoggerFactory.getLogger(JwtAuthenticationFilter.class);


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        logger.info("Authorization header: {}", header);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if(!jwtService.isAccessToken(token)){
                filterChain.doFilter(request,response);
                return;
            }

            try {
                Jws<Claims> parsedToken = jwtService.parse(token);
                Claims claims = parsedToken.getPayload();

                UUID userUuid = UserHelper.parseUUID(claims.getSubject());

                userRepository.findById(userUuid).ifPresent(user -> {

                    //check for user enable or not
                    if(user.isEnable()){
                        List<GrantedAuthority> authorities =
                                user.getRoleSet() == null
                                        ? List.of()
                                        : user.getRoleSet()
                                        .stream()
                                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                                        .collect(Collectors.toList());

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        user,
                                        null,
                                        authorities
                                );

                        if(SecurityContextHolder.getContext().getAuthentication() == null) {
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    }
                });

            } catch (Exception e){
                request.setAttribute("error","Token Expired");
            }
        }

        // VERY IMPORTANT
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        return request.getRequestURI().startsWith("/api/v1/auth");
    }
}
