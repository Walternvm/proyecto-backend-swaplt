package com.example.proyectobackendswaplt.auth;

import com.example.proyectobackendswaplt.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthValidationTest extends IntegrationTestSupport {

    @Test
    void registerRejectsWeakPassword() throws Exception {
        mockMvc.perform(post(BASE + "/auth/register").contentType("application/json")
                        .content("{\"name\":\"Luis\",\"email\":\"luis-" + uniqueSuffix() + "@example.com\",\"password\":\"alllowercase\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerRejectsBlankName() throws Exception {
        mockMvc.perform(post(BASE + "/auth/register").contentType("application/json")
                        .content("{\"name\":\"\",\"email\":\"blank-" + uniqueSuffix() + "@example.com\",\"password\":\"Password123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerRejectsDuplicateEmail() throws Exception {
        String email = "dup-" + uniqueSuffix() + "@example.com";
        String body = "{\"name\":\"Dup\",\"email\":\"" + email + "\",\"password\":\"Password123\"}";
        mockMvc.perform(post(BASE + "/auth/register").contentType("application/json").content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(post(BASE + "/auth/register").contentType("application/json").content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void loginWithWrongPasswordReturnsUnauthorized() throws Exception {
        String email = "wrongpass-" + uniqueSuffix() + "@example.com";
        mockMvc.perform(post(BASE + "/auth/register").contentType("application/json")
                .content("{\"name\":\"Wp\",\"email\":\"" + email + "\",\"password\":\"Password123\"}"));
        mockMvc.perform(post(BASE + "/auth/login").contentType("application/json")
                        .content("{\"email\":\"" + email + "\",\"password\":\"OtraClave123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithUnknownEmailReturnsUnauthorized() throws Exception {
        mockMvc.perform(post(BASE + "/auth/login").contentType("application/json")
                        .content("{\"email\":\"noexiste-" + uniqueSuffix() + "@example.com\",\"password\":\"Password123\"}"))
                .andExpect(status().isUnauthorized());
    }
}