package com.authApp.AuthApp_Backend.entities;

import jakarta.persistence.*;
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
@Entity
@Table(name = "user_id")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String email;
    private String name;
    private String password;
    private String image;
    private boolean enable = true;
    private String gender;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();


    @Enumerated(EnumType.STRING)
    private Provider provider = Provider.LOCAL;

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",joinColumns = @JoinColumn(name = "user_id"),inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roleSet = new HashSet<>();

    @PrePersist
    protected void onCreate(){
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate(){
        updatedAt = Instant.now();
    }
}
