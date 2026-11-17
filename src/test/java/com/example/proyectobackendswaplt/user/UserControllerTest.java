package com.example.proyectobackendswaplt.user;

import com.example.proyectobackendswaplt.support.IntegrationTestSupport;
import com.example.proyectobackendswaplt.user.domain.Role;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends IntegrationTestSupport {

    @Test
    void authenticatedUserCanViewSafePublicProfile() throws Exception {
        var requester = saveUser(
                "requester-" + uniqueSuffix(),
                Role.USER
        );

        var profileOwner = saveUser(
                "profile-owner-" + uniqueSuffix(),
                Role.USER
        );

        mockMvc.perform(
                        get(
                                BASE
                                        + "/users/"
                                        + profileOwner.getId()
                                        + "/profile"
                        )
                                .header(
                                        "Authorization",
                                        bearer(requester)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(profileOwner.getId())
                )
                .andExpect(
                        jsonPath("$.name")
                                .value(profileOwner.getName())
                )
                .andExpect(
                        jsonPath("$.email").doesNotExist()
                )
                .andExpect(
                        jsonPath("$.role").doesNotExist()
                );
    }
}