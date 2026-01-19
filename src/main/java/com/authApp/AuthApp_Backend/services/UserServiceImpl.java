package com.authApp.AuthApp_Backend.services;

import com.authApp.AuthApp_Backend.dtos.UserDto;
import com.authApp.AuthApp_Backend.entities.Provider;
import com.authApp.AuthApp_Backend.entities.User;
import com.authApp.AuthApp_Backend.exceptions.ResourceNotFoundException;
import com.authApp.AuthApp_Backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {

        if (userDto.getEmail() == null || userDto.getEmail().isBlank()){
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        if(userRepository.existsByEmail(userDto.getEmail())){
            throw new IllegalArgumentException("User with Email already Exist!!");
        }

        User user = modelMapper.map(userDto, User.class);

        user.setProvider(userDto.getProvider() != null ? userDto.getProvider() : Provider.LOCAL);

        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser,UserDto.class);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with Email Does Not Exist"));
        return modelMapper.map(user,UserDto.class);
    }

    @Override
    public UserDto updateUser(String userId, UserDto userDto) {
        return null;
    }

    @Override
    public void deleteUser(String userId) {

    }

    @Override
    public UserDto getuserById(String userId) {
        return null;
    }

    @Override
    @Transactional
    public Iterable<UserDto> getAllUsers() {
        return userRepository
                .findAll()
                .stream()
                .map(user -> modelMapper.map(user,UserDto.class))
                .toList();
    }
}
