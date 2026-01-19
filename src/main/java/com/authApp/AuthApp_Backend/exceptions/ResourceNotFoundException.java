package com.authApp.AuthApp_Backend.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(){
        super("Resource Doesn't Exist!!!");
    }
}
