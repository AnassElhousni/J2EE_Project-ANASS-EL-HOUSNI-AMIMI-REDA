package com.example.microserviceproduits.web.exception;

public class CommandeNotFoundException extends Exception {
    public CommandeNotFoundException(String message) {
        super(message);
    }
}
