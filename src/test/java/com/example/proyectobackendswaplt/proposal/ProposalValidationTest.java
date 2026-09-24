package com.example.proyectobackendswaplt.proposal;

import com.example.proyectobackendswaplt.item.domain.ItemState;
import com.example.proyectobackendswaplt.support.IntegrationTestSupport;
import com.example.proyectobackendswaplt.user.domain.Role;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProposalValidationTest extends IntegrationTestSupport {

    @Test
    void createOnOwnPublicationReturns409() throws Exception {
        var suffix = uniqueSuffix();
        var owner = saveUser("prop-self-owner-" + suffix, Role.USER);
        var category = saveCategory("PropSelf-" + suffix);
        var requested = saveItem(owner, category);
        var offered = saveItem(owner, category);
        var publication = savePublication(owner, requested);

        mockMvc.perform(post(BASE + "/proposals").header("Authorization", bearer(owner))
                        .contentType("application/json")
                        .content("{\"offeredItemId\":" + offered.getId() + ",\"publicationId\":" + publication.getId() + "}"))
                .andExpect(status().isConflict());
    }

    @Test
    void createWithUnavailableOfferedItemReturns409() throws Exception {
        var suffix = uniqueSuffix();
        var owner = saveUser("prop-unavail-owner-" + suffix, Role.USER);
        var proposer = saveUser("prop-unavail-proposer-" + suffix, Role.USER);
        var category = saveCategory("PropUnavail-" + suffix);
        var requested = saveItem(owner, category);
        var offered = saveItem(proposer, category);
        offered.setState(ItemState.RESERVED);
        itemRepository.save(offered);
        var publication = savePublication(owner, requested);

        mockMvc.perform(post(BASE + "/proposals").header("Authorization", bearer(proposer))
                        .contentType("application/json")
                        .content("{\"offeredItemId\":" + offered.getId() + ",\"publicationId\":" + publication.getId() + "}"))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectByNonOwnerReturns403AndRejectTwiceReturns409() throws Exception {
        var suffix = uniqueSuffix();
        var owner = saveUser("prop-rej-owner-" + suffix, Role.USER);
        var proposer = saveUser("prop-rej-proposer-" + suffix, Role.USER);
        var category = saveCategory("PropRej-" + suffix);
        var requested = saveItem(owner, category);
        var offered = saveItem(proposer, category);
        var publication = savePublication(owner, requested);

        Long proposalId = idOf(mockMvc.perform(post(BASE + "/proposals").header("Authorization", bearer(proposer))
                        .contentType("application/json")
                        .content("{\"offeredItemId\":" + offered.getId() + ",\"publicationId\":" + publication.getId() + "}"))
                .andExpect(status().isCreated()));

        mockMvc.perform(put(BASE + "/proposals/" + proposalId + "/reject").header("Authorization", bearer(proposer)))
                .andExpect(status().isForbidden());
        mockMvc.perform(put(BASE + "/proposals/" + proposalId + "/reject").header("Authorization", bearer(owner)))
                .andExpect(status().isOk());
        mockMvc.perform(put(BASE + "/proposals/" + proposalId + "/reject").header("Authorization", bearer(owner)))
                .andExpect(status().isConflict());
    }

    @Test
    void findByIdNotFoundReturns404() throws Exception {
        var user = saveUser("prop-404-" + uniqueSuffix(), Role.ADMIN);
        mockMvc.perform(get(BASE + "/proposals/999999").header("Authorization", bearer(user)))
                .andExpect(status().isNotFound());
    }
}
