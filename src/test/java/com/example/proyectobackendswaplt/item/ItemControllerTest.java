package com.example.proyectobackendswaplt.item;

import com.example.proyectobackendswaplt.support.IntegrationTestSupport;
import com.example.proyectobackendswaplt.user.domain.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ItemControllerTest extends IntegrationTestSupport {

    @Test
    void currentUserCanListOnlyTheirItems() throws Exception {
        var owner = saveUser("owner-" + uniqueSuffix(), Role.USER);
        var other = saveUser("other-" + uniqueSuffix(), Role.USER);
        var category = saveCategory("Items-" + uniqueSuffix());
        var mine = saveItem(owner, category);
        saveItem(other, category);

        mockMvc.perform(get(BASE + "/users/me/items")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(owner)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath(
                        "$[0].id",
                        is(mine.getId().intValue())
                ));
    }
}