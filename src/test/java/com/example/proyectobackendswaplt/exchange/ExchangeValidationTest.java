package com.example.proyectobackendswaplt.exchange;

import com.example.proyectobackendswaplt.support.IntegrationTestSupport;
import com.example.proyectobackendswaplt.user.domain.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExchangeValidationTest extends IntegrationTestSupport {

    @Test
    void findByIdNotFoundReturns404() throws Exception {

        var admin = saveUser(
                "exch-404-" + uniqueSuffix(),
                Role.ADMIN
        );

        mockMvc.perform(
                        get(BASE + "/exchanges/999999")
                                .header(
                                        "Authorization",
                                        bearer(admin)
                                )
                )
                .andExpect(
                        status().isNotFound()
                );
    }

    @Test
    void confirmByNonParticipantReturns403() throws Exception {

        String suffix = uniqueSuffix();

        var owner = saveUser(
                "exch-owner-" + suffix,
                Role.USER
        );

        var proposer = saveUser(
                "exch-proposer-" + suffix,
                Role.USER
        );

        var outsider = saveUser(
                "exch-outsider-" + suffix,
                Role.USER
        );

        var category = saveCategory(
                "Exch-" + suffix
        );

        var requestedItem =
                saveItem(owner, category);

        var offeredItem =
                saveItem(proposer, category);

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

        Long exchangeId = idOf(
                mockMvc.perform(
                                patch(
                                        BASE
                                                + "/proposals/"
                                                + proposalId
                                                + "/accept"
                                )
                                        .header(
                                                "Authorization",
                                                bearer(owner)
                                        )
                        )
                        .andExpect(
                                status().isCreated()
                        )
        );

        mockMvc.perform(
                        patch(
                                BASE
                                        + "/exchanges/"
                                        + exchangeId
                                        + "/confirm"
                        )
                                .header(
                                        "Authorization",
                                        bearer(outsider)
                                )
                )
                .andExpect(
                        status().isForbidden()
                );
    }

    @Test
    void cancellingTwiceReturns409OnSecondAttempt() throws Exception {

        String suffix = uniqueSuffix();

        var owner = saveUser(
                "exch-cancel-owner-" + suffix,
                Role.USER
        );

        var proposer = saveUser(
                "exch-cancel-proposer-" + suffix,
                Role.USER
        );

        var category = saveCategory(
                "ExchCancel-" + suffix
        );

        var requestedItem =
                saveItem(owner, category);

        var offeredItem =
                saveItem(proposer, category);

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

        Long exchangeId = idOf(
                mockMvc.perform(
                                patch(
                                        BASE
                                                + "/proposals/"
                                                + proposalId
                                                + "/accept"
                                )
                                        .header(
                                                "Authorization",
                                                bearer(owner)
                                        )
                        )
                        .andExpect(
                                status().isCreated()
                        )
        );

        mockMvc.perform(
                        patch(
                                BASE
                                        + "/exchanges/"
                                        + exchangeId
                                        + "/cancel"
                        )
                                .header(
                                        "Authorization",
                                        bearer(proposer)
                                )
                )
                .andExpect(
                        status().isOk()
                );

        mockMvc.perform(
                        patch(
                                BASE
                                        + "/exchanges/"
                                        + exchangeId
                                        + "/cancel"
                        )
                                .header(
                                        "Authorization",
                                        bearer(proposer)
                                )
                )
                .andExpect(
                        status().isConflict()
                );
    }
}
