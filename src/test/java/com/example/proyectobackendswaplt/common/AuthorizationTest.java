package com.example.proyectobackendswaplt.common;

import com.example.proyectobackendswaplt.support.IntegrationTestSupport;
import com.example.proyectobackendswaplt.user.domain.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthorizationTest extends IntegrationTestSupport {

    @Test
    void unauthenticatedRequestsAreRejected() throws Exception {
        mockMvc.perform(get(BASE + "/users"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get(BASE + "/users/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post(BASE + "/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Unauthorized category"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void nonAdminCannotManageCategories() throws Exception {
        var user = saveUser(
                "permission-user-" + uniqueSuffix(),
                Role.USER
        );

        mockMvc.perform(post(BASE + "/categories")
                        .header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Forbidden-%s"
                                }
                                """.formatted(uniqueSuffix())))
                .andExpect(status().isForbidden());

        var category = saveCategory(
                "DeleteForbidden-" + uniqueSuffix()
        );

        mockMvc.perform(delete(BASE + "/categories/" + category.getId())
                        .header("Authorization", bearer(user)))
                .andExpect(status().isForbidden());
    }

    @Test
    void nonAdminCannotListAllUsers() throws Exception {
        var user = saveUser(
                "permission-list-" + uniqueSuffix(),
                Role.USER
        );

        mockMvc.perform(get(BASE + "/users")
                        .header("Authorization", bearer(user)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanManageCategories() throws Exception {
        var admin = saveUser(
                "permission-admin-" + uniqueSuffix(),
                Role.ADMIN
        );

        Long categoryId = idOf(
                mockMvc.perform(post(BASE + "/categories")
                                .header("Authorization", bearer(admin))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "AdminCategory-%s"
                                        }
                                        """.formatted(uniqueSuffix())))
                        .andExpect(status().isCreated())
        );

        mockMvc.perform(delete(BASE + "/categories/" + categoryId)
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());
    }
}