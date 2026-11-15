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
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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

    @Test
    void createAcceptsValidCoordinates() throws Exception {
        String suffix = uniqueSuffix();

        var owner = saveUser(
                "coordinates-owner-" + suffix,
                Role.USER
        );
        var category = saveCategory(
                "Coordinates-" + suffix
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
                                          "latitude": -12.046374,
                                          "longitude": -77.042793,
                                          "wantedItem": "Libro",
                                          "condition": "GOOD"
                                        }
                                        """.formatted(
                                                category.getId()
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath(
                        "$.latitude",
                        is(-12.046374)
                ))
                .andExpect(jsonPath(
                        "$.longitude",
                        is(-77.042793)
                ));
    }

    @Test
    void createRejectsIncompleteCoordinates() throws Exception {
        String suffix = uniqueSuffix();

        var owner = saveUser(
                "incomplete-owner-" + suffix,
                Role.USER
        );
        var category = saveCategory(
                "Incomplete-" + suffix
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
                                          "latitude": -12.046374,
                                          "wantedItem": "Libro",
                                          "condition": "GOOD"
                                        }
                                        """.formatted(
                                                category.getId()
                                        )
                                )
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRejectsCoordinatesOutsideRange() throws Exception {
        String suffix = uniqueSuffix();

        var owner = saveUser(
                "range-owner-" + suffix,
                Role.USER
        );
        var category = saveCategory(
                "Range-" + suffix
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
                                          "latitude": 91,
                                          "longitude": -181,
                                          "wantedItem": "Libro",
                                          "condition": "GOOD"
                                        }
                                        """.formatted(
                                                category.getId()
                                        )
                                )
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void ownerCanUpdateItemCoordinates() throws Exception {
        String suffix = uniqueSuffix();

        var owner = saveUser(
                "update-coordinates-" + suffix,
                Role.USER
        );
        var category = saveCategory(
                "UpdateCoordinates-" + suffix
        );
        var item = saveItem(owner, category);

        mockMvc.perform(
                        put(
                                BASE + "/items/" + item.getId()
                        )
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
                                          "name": "Bicicleta actualizada",
                                          "categoryId": %d,
                                          "location": "Cusco",
                                          "latitude": -13.531950,
                                          "longitude": -71.967463,
                                          "wantedItem": "Guitarra",
                                          "condition": "LIKE_NEW"
                                        }
                                        """.formatted(
                                                category.getId()
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.location",
                        is("Cusco")
                ))
                .andExpect(jsonPath(
                        "$.latitude",
                        is(-13.531950)
                ))
                .andExpect(jsonPath(
                        "$.longitude",
                        is(-71.967463)
                ));
    }

    @Test
    void nonOwnerCannotUpdateItem() throws Exception {
        String suffix = uniqueSuffix();

        var owner = saveUser(
                "update-owner-" + suffix,
                Role.USER
        );
        var stranger = saveUser(
                "update-stranger-" + suffix,
                Role.USER
        );
        var category = saveCategory(
                "ProtectedUpdate-" + suffix
        );
        var item = saveItem(owner, category);

        mockMvc.perform(
                        put(
                                BASE + "/items/" + item.getId()
                        )
                                .header(
                                        "Authorization",
                                        bearer(stranger)
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "name": "Intento ajeno",
                                          "categoryId": %d,
                                          "location": "Lima",
                                          "latitude": -12.046374,
                                          "longitude": -77.042793,
                                          "wantedItem": "Libro",
                                          "condition": "GOOD"
                                        }
                                        """.formatted(
                                                category.getId()
                                        )
                                )
                )
                .andExpect(status().isForbidden());
    }
}
