package com.authApp.AuthApp_Backend.controllers;

import com.authApp.AuthApp_Backend.dtos.LoginRequest;
import com.authApp.AuthApp_Backend.dtos.TokenResponse;
import com.authApp.AuthApp_Backend.dtos.UserDto;
import com.authApp.AuthApp_Backend.entities.User;
import com.authApp.AuthApp_Backend.repository.UserRepository;
import com.authApp.AuthApp_Backend.security.JwtService;
import com.authApp.AuthApp_Backend.services.AuthService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
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

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody LoginRequest loginRequest){

        //authenticate
        Authentication authentication = authenticate(loginRequest);
        User user = userRepository.findByEmail(loginRequest.email()).orElseThrow(() -> new BadCredentialsException("User does not Exist!!!"));

        if(!user.isEnable()){
            throw new DisabledException("User is disabled");
        }

        //generate token

        String accessToken = jwtService.generateAccessToken(user);

        TokenResponse response = TokenResponse.of(accessToken,"",jwtService.getAccessTtlSeconds(),modelMapper.map(user, UserDto.class));

        return ResponseEntity.ok(response);

    }

    private Authentication authenticate(LoginRequest loginRequest) {
        try {
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(),loginRequest.password()));
        } catch (Exception e) {
            throw new BadCredentialsException("Username or Password Invalid");
        }
    }

    @PostMapping("/register")
    ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDto));
    }
}
