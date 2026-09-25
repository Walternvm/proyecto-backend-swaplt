package com.example.proyectobackendswaplt.auth.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserRegisteredEvent extends ApplicationEvent {
    private final String userName;
    private final String userEmail;

    public UserRegisteredEvent(Object source, String userName, String userEmail) {
        super(source);
        this.userName = userName;
        this.userEmail = userEmail;
    }
}