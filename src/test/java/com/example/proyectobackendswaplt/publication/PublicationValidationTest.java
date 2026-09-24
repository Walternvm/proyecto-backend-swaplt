package com.example.proyectobackendswaplt.publication;

import com.example.proyectobackendswaplt.item.domain.ItemState;
import com.example.proyectobackendswaplt.support.IntegrationTestSupport;
import com.example.proyectobackendswaplt.user.domain.Role;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PublicationValidationTest extends IntegrationTestSupport {

    @Test
    void createRejectsBlankWantedItem() throws Exception {
        var suffix = uniqueSuffix();
        var owner = saveUser("pub-owner-" + suffix, Role.USER);
        var category = saveCategory("PubBlank-" + suffix);
        var item = saveItem(owner, category);

        mockMvc.perform(post(BASE + "/publications").header("Authorization", bearer(owner))
                        .contentType("application/json")
                        .content("{\"itemId\":" + item.getId() + ",\"wantedItem\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createByNonOwnerOfItemReturns403() throws Exception {
        var suffix = uniqueSuffix();
        var owner = saveUser("pub-real-owner-" + suffix, Role.USER);
        var stranger = saveUser("pub-stranger-" + suffix, Role.USER);
        var category = saveCategory("PubForbidden-" + suffix);
        var item = saveItem(owner, category);

        mockMvc.perform(post(BASE + "/publications").header("Authorization", bearer(stranger))
                        .contentType("application/json")
                        .content("{\"itemId\":" + item.getId() + ",\"wantedItem\":\"Algo\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createWhenItemNotAvailableReturns409() throws Exception {
        var suffix = uniqueSuffix();
        var owner = saveUser("pub-unavail-owner-" + suffix, Role.USER);
        var category = saveCategory("PubUnavail-" + suffix);
        var item = saveItem(owner, category);
        item.setState(ItemState.RESERVED);
        itemRepository.save(item);

        mockMvc.perform(post(BASE + "/publications").header("Authorization", bearer(owner))
                        .contentType("application/json")
                        .content("{\"itemId\":" + item.getId() + ",\"wantedItem\":\"Algo\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void findByIdNotFoundReturns404() throws Exception {
        mockMvc.perform(get(BASE + "/publications/999999")).andExpect(status().isNotFound());
    }
}