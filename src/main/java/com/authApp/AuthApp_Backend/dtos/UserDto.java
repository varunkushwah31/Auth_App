package com.authApp.AuthApp_Backend.dtos;

import com.authApp.AuthApp_Backend.entities.Provider;
import com.authApp.AuthApp_Backend.entities.Role;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private UUID id;
    private String email;
    private String name;
    private String password;
    private String image;
    private boolean enable = true;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
    private String gender;
    private Provider provider = Provider.LOCAL;
    private Set<RoleDto> roleSet = new HashSet<>();

}
