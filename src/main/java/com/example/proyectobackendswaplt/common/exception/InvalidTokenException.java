package com.example.proyectobackendswaplt.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends ApiException {
    public InvalidTokenException() {
        super(HttpStatus.UNAUTHORIZED, "Refresh token invalido o expirado");
    }
}