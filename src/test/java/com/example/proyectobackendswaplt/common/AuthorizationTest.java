package com.example.proyectobackendswaplt.common;

import com.example.proyectobackendswaplt.support.IntegrationTestSupport;
import com.example.proyectobackendswaplt.user.domain.Role;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthorizationTest extends IntegrationTestSupport {

    @Test
    void unauthenticatedRequestsAreRejected() throws Exception {
        mockMvc.perform(get(BASE + "/users")).andExpect(status().isUnauthorized());
        mockMvc.perform(get(BASE + "/users/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(post(BASE + "/categories").contentType("application/json")
                        .content("{\"name\":\"X\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void nonAdminCannotManageCategories() throws Exception {
        var user = saveUser("perm-user-" + uniqueSuffix(), Role.USER);
        mockMvc.perform(post(BASE + "/categories").header("Authorization", bearer(user))
                        .contentType("application/json").content("{\"name\":\"Nueva\"}"))
                .andExpect(status().isForbidden());

        var category = saveCategory("ParaBorrar-" + uniqueSuffix());
        mockMvc.perform(delete(BASE + "/categories/" + category.getId()).header("Authorization", bearer(user)))
                .andExpect(status().isForbidden());
    }

    @Test
    void nonAdminCannotListAllUsers() throws Exception {
        var user = saveUser("perm-list-" + uniqueSuffix(), Role.USER);
        mockMvc.perform(get(BASE + "/users").header("Authorization", bearer(user)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanManageCategories() throws Exception {
        var admin = saveUser("perm-admin-" + uniqueSuffix(), Role.ADMIN);
        String id = idOf(mockMvc.perform(post(BASE + "/categories").header("Authorization", bearer(admin))
                        .contentType("application/json").content("{\"name\":\"AdminCat-" + uniqueSuffix() + "\"}"))
                .andExpect(status().isCreated())).toString();
        mockMvc.perform(delete(BASE + "/categories/" + id).header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());
    }
}