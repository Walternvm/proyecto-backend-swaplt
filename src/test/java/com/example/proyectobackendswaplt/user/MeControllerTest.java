package com.example.proyectobackendswaplt.user;

import com.example.proyectobackendswaplt.support.IntegrationTestSupport;
import com.example.proyectobackendswaplt.user.domain.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MeControllerTest extends IntegrationTestSupport {

    @Test
    void authenticatedUserCanUpdateOwnName() throws Exception {
        var user = saveUser(
                "profile-" + uniqueSuffix(),
                Role.USER
        );

        mockMvc.perform(
                        put(BASE + "/users/me")
                                .header(
                                        "Authorization",
                                        bearer(user)
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of(
                                                        "name",
                                                        "  Nombre actualizado  "
                                                )
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id").value(user.getId())
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Nombre actualizado")
                );
    }

    @Test
    void blankNameIsRejected() throws Exception {
        var user = saveUser(
                "invalid-" + uniqueSuffix(),
                Role.USER
        );

        mockMvc.perform(
                        put(BASE + "/users/me")
                                .header(
                                        "Authorization",
                                        bearer(user)
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of("name", "   ")
                                        )
                                )
                )
                .andExpect(status().isBadRequest());
    }
}