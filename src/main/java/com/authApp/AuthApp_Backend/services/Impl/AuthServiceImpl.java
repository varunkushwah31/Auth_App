package com.authApp.AuthApp_Backend.services.Impl;

import com.authApp.AuthApp_Backend.dtos.UserDto;
import com.authApp.AuthApp_Backend.services.AuthService;
import com.authApp.AuthApp_Backend.services.UserService;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@AllArgsConstructor
@Getter
@Setter
@Builder
@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto registerUser(UserDto userDto) {
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        return userService.createUser(userDto);
    }
}
