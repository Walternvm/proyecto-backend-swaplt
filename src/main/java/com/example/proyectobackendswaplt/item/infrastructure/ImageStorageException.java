package com.example.proyectobackendswaplt.item.infrastructure;

import com.example.proyectobackendswaplt.common.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ImageStorageException extends ApiException {

    public ImageStorageException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message);
    }
}