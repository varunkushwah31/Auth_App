package com.authApp.AuthApp_Backend.repository;

import com.authApp.AuthApp_Backend.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

}
