package com.example.proyectobackendswaplt.category;

import com.example.proyectobackendswaplt.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoryControllerTest extends IntegrationTestSupport {

    @Test
    void publicCanListAndGetById() throws Exception {
        var category = saveCategory("Public-" + uniqueSuffix());

        mockMvc.perform(get(BASE + "/categories"))
                .andExpect(status().isOk());

        mockMvc.perform(get(BASE + "/categories/" + category.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void getByIdNotFoundReturns404() throws Exception {
        mockMvc.perform(get(BASE + "/categories/999999"))
                .andExpect(status().isNotFound());
    }
}
