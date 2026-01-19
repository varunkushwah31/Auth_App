package com.authApp.AuthApp_Backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "role_id")
public class Role {
    @Id
    private UUID id = UUID.randomUUID();
    @Column(unique = true,nullable = false)
    private String name;
}
