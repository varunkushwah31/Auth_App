package com.authApp.AuthApp_Backend.controllers;

import com.authApp.AuthApp_Backend.dtos.UserDto;
import com.authApp.AuthApp_Backend.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    //create user API
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDto));
    }

    //get user API
    @GetMapping
    public ResponseEntity<Iterable<UserDto>> getUsers(){
        return ResponseEntity.status(HttpStatus.OK).body(userService.getAllUsers());
    }

    //get user by email
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email){
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserByEmail(email));
    }
}
