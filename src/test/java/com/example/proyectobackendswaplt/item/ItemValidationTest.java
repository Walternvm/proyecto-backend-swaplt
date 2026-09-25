package com.example.proyectobackendswaplt.item;

import com.example.proyectobackendswaplt.item.domain.ItemState;
import com.example.proyectobackendswaplt.support.IntegrationTestSupport;
import com.example.proyectobackendswaplt.user.domain.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ItemValidationTest extends IntegrationTestSupport {

    @Test
    void createRejectsBlankNameAndMissingCategory() throws Exception {

        var owner = saveUser(
                "item-owner-" + uniqueSuffix(),
                Role.USER
        );

        mockMvc.perform(
                        post(BASE + "/items")
                                .header(
                                        "Authorization",
                                        bearer(owner)
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "name": "",
                                          "location": "Lima",
                                          "wantedItem": "Libro",
                                          "condition": "GOOD"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    void createRejectsBlankWantedItem() throws Exception {

        String suffix = uniqueSuffix();

        var owner = saveUser(
                "item-wanted-owner-" + suffix,
                Role.USER
        );

        var category = saveCategory(
                "ItemWanted-" + suffix
        );

        mockMvc.perform(
                        post(BASE + "/items")
                                .header(
                                        "Authorization",
                                        bearer(owner)
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "name": "Bicicleta",
                                          "categoryId": %d,
                                          "location": "Lima",
                                          "wantedItem": "",
                                          "condition": "GOOD"
                                        }
                                        """.formatted(
                                                category.getId()
                                        )
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    void createRejectsMissingCondition() throws Exception {

        String suffix = uniqueSuffix();

        var owner = saveUser(
                "item-condition-owner-" + suffix,
                Role.USER
        );

        var category = saveCategory(
                "ItemCondition-" + suffix
        );

        mockMvc.perform(
                        post(BASE + "/items")
                                .header(
                                        "Authorization",
                                        bearer(owner)
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "name": "Bicicleta",
                                          "categoryId": %d,
                                          "location": "Lima",
                                          "wantedItem": "Libro"
                                        }
                                        """.formatted(
                                                category.getId()
                                        )
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    void findByIdNotFoundReturns404() throws Exception {

        mockMvc.perform(
                        get(BASE + "/items/999999")
                )
                .andExpect(
                        status().isNotFound()
                );
    }

    @Test
    void deleteByNonOwnerReturns403() throws Exception {

        String suffix = uniqueSuffix();

        var owner = saveUser(
                "item-del-owner-" + suffix,
                Role.USER
        );

        var stranger = saveUser(
                "item-del-stranger-" + suffix,
                Role.USER
        );

        var category = saveCategory(
                "ItemDel-" + suffix
        );

        var item = saveItem(owner, category);

        mockMvc.perform(
                        delete(
                                BASE
                                        + "/items/"
                                        + item.getId()
                        )
                                .header(
                                        "Authorization",
                                        bearer(stranger)
                                )
                )
                .andExpect(
                        status().isForbidden()
                );
    }

    @Test
    void deleteReservedItemReturns409() throws Exception {

        String suffix = uniqueSuffix();

        var owner = saveUser(
                "item-res-owner-" + suffix,
                Role.USER
        );

        var category = saveCategory(
                "ItemRes-" + suffix
        );

        var item = saveItem(owner, category);

        item.setState(ItemState.RESERVED);
        itemRepository.save(item);

        mockMvc.perform(
                        delete(
                                BASE
                                        + "/items/"
                                        + item.getId()
                        )
                                .header(
                                        "Authorization",
                                        bearer(owner)
                                )
                )
                .andExpect(
                        status().isConflict()
                );
    }
}
