package com.example.proyectobackendswaplt.proposal;

import com.example.proyectobackendswaplt.item.domain.ItemState;
import com.example.proyectobackendswaplt.support.IntegrationTestSupport;
import com.example.proyectobackendswaplt.user.domain.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProposalValidationTest extends IntegrationTestSupport {

    @Test
    void createOnOwnItemReturns409() throws Exception {

        String suffix = uniqueSuffix();

        var owner = saveUser(
                "prop-self-owner-" + suffix,
                Role.USER
        );

        var category = saveCategory(
                "PropSelf-" + suffix
        );

        var requestedItem = saveItem(owner, category);

        var offeredItem = saveItem(owner, category);

        mockMvc.perform(
                        post(BASE + "/proposals")
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
                                          "offeredItemId": %d,
                                          "requestedItemId": %d
                                        }
                                        """.formatted(
                                                offeredItem.getId(),
                                                requestedItem.getId()
                                        )
                                )
                )
                .andExpect(
                        status().isConflict()
                );
    }

    @Test
    void createWithUnavailableOfferedItemReturns409() throws Exception {

        String suffix = uniqueSuffix();

        var owner = saveUser(
                "prop-unavail-owner-" + suffix,
                Role.USER
        );

        var proposer = saveUser(
                "prop-unavail-proposer-" + suffix,
                Role.USER
        );

        var category = saveCategory(
                "PropUnavail-" + suffix
        );

        var requestedItem = saveItem(owner, category);

        var offeredItem = saveItem(proposer, category);

        offeredItem.setState(ItemState.RESERVED);
        itemRepository.save(offeredItem);

        mockMvc.perform(
                        post(BASE + "/proposals")
                                .header(
                                        "Authorization",
                                        bearer(proposer)
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "offeredItemId": %d,
                                          "requestedItemId": %d
                                        }
                                        """.formatted(
                                                offeredItem.getId(),
                                                requestedItem.getId()
                                        )
                                )
                )
                .andExpect(
                        status().isConflict()
                );
    }

    @Test
    void rejectByNonOwnerReturns403AndRejectTwiceReturns409() throws Exception {

        String suffix = uniqueSuffix();

        var owner = saveUser(
                "prop-rej-owner-" + suffix,
                Role.USER
        );

        var proposer = saveUser(
                "prop-rej-proposer-" + suffix,
                Role.USER
        );

        var category = saveCategory(
                "PropRej-" + suffix
        );

        var requestedItem = saveItem(owner, category);

        var offeredItem = saveItem(proposer, category);

        Long proposalId = idOf(
                mockMvc.perform(
                                post(BASE + "/proposals")
                                        .header(
                                                "Authorization",
                                                bearer(proposer)
                                        )
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(
                                                """
                                                {
                                                  "offeredItemId": %d,
                                                  "requestedItemId": %d
                                                }
                                                """.formatted(
                                                        offeredItem.getId(),
                                                        requestedItem.getId()
                                                )
                                        )
                        )
                        .andExpect(
                                status().isCreated()
                        )
        );

        mockMvc.perform(
                        patch(
                                BASE
                                        + "/proposals/"
                                        + proposalId
                                        + "/reject"
                        )
                                .header(
                                        "Authorization",
                                        bearer(proposer)
                                )
                )
                .andExpect(
                        status().isForbidden()
                );

        mockMvc.perform(
                        patch(
                                BASE
                                        + "/proposals/"
                                        + proposalId
                                        + "/reject"
                        )
                                .header(
                                        "Authorization",
                                        bearer(owner)
                                )
                )
                .andExpect(
                        status().isOk()
                );

        mockMvc.perform(
                        patch(
                                BASE
                                        + "/proposals/"
                                        + proposalId
                                        + "/reject"
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
    void findByIdNotFoundReturns404() throws Exception {

        var admin = saveUser(
                "prop-404-" + uniqueSuffix(),
                Role.ADMIN
        );

        mockMvc.perform(
                        get(BASE + "/proposals/999999")
                                .header(
                                        "Authorization",
                                        bearer(admin)
                                )
                )
                .andExpect(
                        status().isNotFound()
                );
    }
}
