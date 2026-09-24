package com.example.proyectobackendswaplt.review;

import com.example.proyectobackendswaplt.support.IntegrationTestSupport;
import com.example.proyectobackendswaplt.user.domain.Role;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReviewValidationTest extends IntegrationTestSupport {

    @Test
    void createRejectsRatingOutOfRange() throws Exception {
        var user = saveUser("review-rating-" + uniqueSuffix(), Role.USER);
        mockMvc.perform(post(BASE + "/reviews").header("Authorization", bearer(user))
                        .contentType("application/json")
                        .content("{\"exchangeId\":1,\"rating\":0}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(BASE + "/reviews").header("Authorization", bearer(user))
                        .contentType("application/json")
                        .content("{\"exchangeId\":1,\"rating\":6}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createForNonCompletedExchangeReturns409() throws Exception {
        var suffix = uniqueSuffix();
        var owner = saveUser("review-pending-owner-" + suffix, Role.USER);
        var proposer = saveUser("review-pending-proposer-" + suffix, Role.USER);
        var category = saveCategory("ReviewPending-" + suffix);
        var requested = saveItem(owner, category);
        var offered = saveItem(proposer, category);
        var publication = savePublication(owner, requested);

        Long proposalId = idOf(mockMvc.perform(post(BASE + "/proposals").header("Authorization", bearer(proposer))
                        .contentType("application/json")
                        .content("{\"offeredItemId\":" + offered.getId() + ",\"publicationId\":" + publication.getId() + "}"))
                .andExpect(status().isCreated()));
        Long exchangeId = idOf(mockMvc.perform(put(BASE + "/proposals/" + proposalId + "/accept")
                .header("Authorization", bearer(owner))).andExpect(status().isCreated()));

        mockMvc.perform(post(BASE + "/reviews").header("Authorization", bearer(proposer))
                        .contentType("application/json")
                        .content("{\"exchangeId\":" + exchangeId + ",\"rating\":5}"))
                .andExpect(status().isConflict());
    }

    @Test
    void createByNonParticipantReturns403() throws Exception {
        var suffix = uniqueSuffix();
        var owner = saveUser("review-forbidden-owner-" + suffix, Role.USER);
        var proposer = saveUser("review-forbidden-proposer-" + suffix, Role.USER);
        var outsider = saveUser("review-forbidden-outsider-" + suffix, Role.USER);
        var category = saveCategory("ReviewForbidden-" + suffix);
        var requested = saveItem(owner, category);
        var offered = saveItem(proposer, category);
        var publication = savePublication(owner, requested);

        Long proposalId = idOf(mockMvc.perform(post(BASE + "/proposals").header("Authorization", bearer(proposer))
                        .contentType("application/json")
                        .content("{\"offeredItemId\":" + offered.getId() + ",\"publicationId\":" + publication.getId() + "}"))
                .andExpect(status().isCreated()));
        Long exchangeId = idOf(mockMvc.perform(put(BASE + "/proposals/" + proposalId + "/accept")
                .header("Authorization", bearer(owner))).andExpect(status().isCreated()));

        mockMvc.perform(post(BASE + "/reviews").header("Authorization", bearer(outsider))
                        .contentType("application/json")
                        .content("{\"exchangeId\":" + exchangeId + ",\"rating\":5}"))
                .andExpect(status().isForbidden());
    }
}
