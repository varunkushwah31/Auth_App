package com.authApp.AuthApp_Backend.services;

import com.authApp.AuthApp_Backend.dtos.UserDto;

public interface AuthService {
    UserDto registerUser(UserDto userDto);


    //UserDto loginUser(UserDto userDto);

}
