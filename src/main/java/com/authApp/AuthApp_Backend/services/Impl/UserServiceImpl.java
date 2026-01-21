package com.authApp.AuthApp_Backend.services.Impl;

import com.authApp.AuthApp_Backend.dtos.UserDto;
import com.authApp.AuthApp_Backend.entities.Provider;
import com.authApp.AuthApp_Backend.entities.User;
import com.authApp.AuthApp_Backend.exceptions.ResourceNotFoundException;
import com.authApp.AuthApp_Backend.helper.UserHelper;
import com.authApp.AuthApp_Backend.repository.UserRepository;
import com.authApp.AuthApp_Backend.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

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
        UUID uuid  = UserHelper.parseUUID(userId);
        User existingUser = userRepository
                .findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found With Given Id"));
        if(userDto.getName() != null) existingUser.setName(userDto.getName());
        if(userDto.getProvider() != null) existingUser.setProvider(userDto.getProvider());
        if(userDto.getGender() != null) existingUser.setGender(userDto.getGender());
        if(userDto.getImage() != null) existingUser.setImage(userDto.getImage());
        if(userDto.getPassword() != null) existingUser.setPassword(userDto.getPassword());
        if (userDto.getEnable() != null) existingUser.setEnable(userDto.getEnable());
        existingUser.setUpdatedAt(Instant.now());
        User updatedUser = userRepository.save(existingUser);
        return modelMapper.map(updatedUser, UserDto.class);

    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        UUID uid = UserHelper.parseUUID(userId);
        User user = userRepository
                .findById(uid)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot Delete As User With This ID Doesn't Exist!!!"));
        userRepository.delete(user);
    }

    @Override
    public UserDto getUserById(String userId) {
       UUID uuid = UserHelper.parseUUID(userId);
       User user = userRepository
               .findById(uuid)
               .orElseThrow(() -> new ResourceNotFoundException("User With This ID Doesn't Exist!!!"));
       return modelMapper.map(user,UserDto.class);
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
