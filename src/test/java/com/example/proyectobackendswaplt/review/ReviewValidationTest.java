package com.example.proyectobackendswaplt.review;

import com.example.proyectobackendswaplt.support.IntegrationTestSupport;
import com.example.proyectobackendswaplt.user.domain.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReviewValidationTest extends IntegrationTestSupport {

    @Test
    void createRejectsRatingOutOfRange() throws Exception {

        var user = saveUser(
                "review-rating-" + uniqueSuffix(),
                Role.USER
        );

        mockMvc.perform(
                        post(BASE + "/reviews")
                                .header(
                                        "Authorization",
                                        bearer(user)
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "exchangeId": 1,
                                          "rating": 0
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        mockMvc.perform(
                        post(BASE + "/reviews")
                                .header(
                                        "Authorization",
                                        bearer(user)
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "exchangeId": 1,
                                          "rating": 6
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    void createForNonCompletedExchangeReturns409() throws Exception {

        String suffix = uniqueSuffix();

        var owner = saveUser(
                "review-pending-owner-" + suffix,
                Role.USER
        );

        var proposer = saveUser(
                "review-pending-proposer-" + suffix,
                Role.USER
        );

        var category = saveCategory(
                "ReviewPending-" + suffix
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
                        post(BASE + "/reviews")
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
                                          "exchangeId": %d,
                                          "rating": 5
                                        }
                                        """.formatted(exchangeId)
                                )
                )
                .andExpect(
                        status().isConflict()
                );
    }

    @Test
    void createByNonParticipantReturns403() throws Exception {

        String suffix = uniqueSuffix();

        var owner = saveUser(
                "review-forbidden-owner-" + suffix,
                Role.USER
        );

        var proposer = saveUser(
                "review-forbidden-proposer-" + suffix,
                Role.USER
        );

        var outsider = saveUser(
                "review-forbidden-outsider-" + suffix,
                Role.USER
        );

        var category = saveCategory(
                "ReviewForbidden-" + suffix
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
                        post(BASE + "/reviews")
                                .header(
                                        "Authorization",
                                        bearer(outsider)
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "exchangeId": %d,
                                          "rating": 5
                                        }
                                        """.formatted(exchangeId)
                                )
                )
                .andExpect(
                        status().isForbidden()
                );
    }
}
