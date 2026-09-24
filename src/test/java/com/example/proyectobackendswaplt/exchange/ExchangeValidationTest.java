package com.example.proyectobackendswaplt.exchange;

import com.example.proyectobackendswaplt.support.IntegrationTestSupport;
import com.example.proyectobackendswaplt.user.domain.Role;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExchangeValidationTest extends IntegrationTestSupport {

    @Test
    void findByIdNotFoundReturns404() throws Exception {
        var user = saveUser("exch-404-" + uniqueSuffix(), Role.ADMIN);
        mockMvc.perform(get(BASE + "/exchanges/999999").header("Authorization", bearer(user)))
                .andExpect(status().isNotFound());
    }

    @Test
    void confirmByNonParticipantReturns403() throws Exception {
        var suffix = uniqueSuffix();
        var owner = saveUser("exch-owner-" + suffix, Role.USER);
        var proposer = saveUser("exch-proposer-" + suffix, Role.USER);
        var outsider = saveUser("exch-outsider-" + suffix, Role.USER);
        var category = saveCategory("Exch-" + suffix);
        var requested = saveItem(owner, category);
        var offered = saveItem(proposer, category);
        var publication = savePublication(owner, requested);

        Long proposalId = idOf(mockMvc.perform(post(BASE + "/proposals").header("Authorization", bearer(proposer))
                        .contentType("application/json")
                        .content("{\"offeredItemId\":" + offered.getId() + ",\"publicationId\":" + publication.getId() + "}"))
                .andExpect(status().isCreated()));
        Long exchangeId = idOf(mockMvc.perform(put(BASE + "/proposals/" + proposalId + "/accept")
                .header("Authorization", bearer(owner))).andExpect(status().isCreated()));

        mockMvc.perform(put(BASE + "/exchanges/" + exchangeId + "/complete").header("Authorization", bearer(outsider)))
                .andExpect(status().isForbidden());
    }

    @Test
    void cancellingTwiceReturns409OnSecondAttempt() throws Exception {
        var suffix = uniqueSuffix();
        var owner = saveUser("exch-cancel-owner-" + suffix, Role.USER);
        var proposer = saveUser("exch-cancel-proposer-" + suffix, Role.USER);
        var category = saveCategory("ExchCancel-" + suffix);
        var requested = saveItem(owner, category);
        var offered = saveItem(proposer, category);
        var publication = savePublication(owner, requested);

        Long proposalId = idOf(mockMvc.perform(post(BASE + "/proposals").header("Authorization", bearer(proposer))
                        .contentType("application/json")
                        .content("{\"offeredItemId\":" + offered.getId() + ",\"publicationId\":" + publication.getId() + "}"))
                .andExpect(status().isCreated()));
        Long exchangeId = idOf(mockMvc.perform(put(BASE + "/proposals/" + proposalId + "/accept")
                .header("Authorization", bearer(owner))).andExpect(status().isCreated()));

        mockMvc.perform(put(BASE + "/exchanges/" + exchangeId + "/cancel").header("Authorization", bearer(proposer)))
                .andExpect(status().isOk());
        mockMvc.perform(put(BASE + "/exchanges/" + exchangeId + "/cancel").header("Authorization", bearer(proposer)))
                .andExpect(status().isConflict());
    }
}
