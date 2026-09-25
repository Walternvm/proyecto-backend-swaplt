package com.example.proyectobackendswaplt.auth;

import com.example.proyectobackendswaplt.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthValidationTest extends IntegrationTestSupport {

    @Test
    void registerRejectsWeakPassword() throws Exception {
        mockMvc.perform(post(BASE + "/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Luis",
                                  "email": "luis-%s@example.com",
                                  "password": "alllowercase"
                                }
                                """.formatted(uniqueSuffix())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerRejectsBlankName() throws Exception {
        mockMvc.perform(post(BASE + "/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "email": "blank-%s@example.com",
                                  "password": "Password123"
                                }
                                """.formatted(uniqueSuffix())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerRejectsDuplicateEmail() throws Exception {
        String email = "duplicate-" + uniqueSuffix() + "@example.com";

        String body = """
                {
                  "name": "Duplicate User",
                  "email": "%s",
                  "password": "Password123"
                }
                """.formatted(email);

        mockMvc.perform(post(BASE + "/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post(BASE + "/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void loginWithWrongPasswordReturnsUnauthorized() throws Exception {
        String email = "wrong-password-" + uniqueSuffix() + "@example.com";

        mockMvc.perform(post(BASE + "/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Wrong Password User",
                                  "email": "%s",
                                  "password": "Password123"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(BASE + "/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "OtraClave123"
                                }
                                """.formatted(email)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithUnknownEmailReturnsUnauthorized() throws Exception {
        mockMvc.perform(post(BASE + "/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "unknown-%s@example.com",
                                  "password": "Password123"
                                }
                                """.formatted(uniqueSuffix())))
                .andExpect(status().isUnauthorized());
    }
}