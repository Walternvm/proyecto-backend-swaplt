package com.example.proyectobackendswaplt;

import com.example.proyectobackendswaplt.category.domain.Category;
import com.example.proyectobackendswaplt.item.domain.Item;
import com.example.proyectobackendswaplt.item.domain.ItemCondition;
import com.example.proyectobackendswaplt.item.domain.ItemState;
import com.example.proyectobackendswaplt.proposal.domain.Proposal;
import com.example.proyectobackendswaplt.proposal.domain.ProposalService;
import com.example.proyectobackendswaplt.review.domain.Review;
import com.example.proyectobackendswaplt.support.IntegrationTestSupport;
import com.example.proyectobackendswaplt.user.domain.Role;
import com.example.proyectobackendswaplt.user.domain.User;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProyectoBackendSwapltApplicationTests extends IntegrationTestSupport {
    @Autowired
    private Validator validator;

    @Autowired
    private ProposalService proposalService;

    @Test
    void contextLoads() {
    }

    @Test
    void registerLoginAndProtectUsers() throws Exception {
        String suffix = uniqueSuffix();
        String email = "register-" + suffix + "@example.com";

        mockMvc.perform(post(BASE + "/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ana",
                                  "email": "%s",
                                  "password": "Password123"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.role", is("USER")));

        mockMvc.perform(post(BASE + "/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "Password123"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()));

        mockMvc.perform(get(BASE + "/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validatesRequiredFieldsAndRating() {
        Category category = new Category();
        category.setName(" ");

        Item item = new Item();
        item.setWantedItem(" ");

        Review review = new Review();
        review.setRating(6);

        assertThat(validator.validate(category))
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("name"));

        assertThat(validator.validate(item))
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("wantedItem"));

        assertThat(validator.validate(review))
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("rating"));
    }

    @Test
    void exchangeHasTwoDifferentParticipants() {
        String suffix = uniqueSuffix();

        User offeringUser = saveUser(
                "exchange-offering-" + suffix,
                Role.USER
        );

        User receivingUser = saveUser(
                "exchange-receiving-" + suffix,
                Role.USER
        );

        Category category = saveCategory("ExchangeCategory-" + suffix);

        Item offeredItem = saveItem(offeringUser, category);
        Item requestedItem = saveItem(receivingUser, category);

        Proposal proposal = new Proposal();
        proposal.setUser(offeringUser);
        proposal.setOfferedItem(offeredItem);
        proposal.setRequestedItem(requestedItem);
        proposal = proposalRepository.save(proposal);

        var authentication = new UsernamePasswordAuthenticationToken(
                receivingUser.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        try {
            var exchange = proposalService.acceptProposal(proposal.getId());

            assertThat(exchange.getOfferingUser().getId())
                    .isEqualTo(offeringUser.getId());

            assertThat(exchange.getReceivingUser().getId())
                    .isEqualTo(receivingUser.getId());

            assertThat(exchange.getOfferingUser().getId())
                    .isNotEqualTo(exchange.getReceivingUser().getId());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void createsResourcesWithIdsAndFlatResponses() throws Exception {
        String suffix = uniqueSuffix();

        User receiver = saveUser(
                "resource-receiver-" + suffix,
                Role.USER
        );

        User proposer = saveUser(
                "resource-proposer-" + suffix,
                Role.USER
        );

        Category category = saveCategory("ResourceCategory-" + suffix);

        Long requestedItemId = idOf(
                mockMvc.perform(post(BASE + "/items")
                                .header("Authorization", bearer(receiver))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "Requested item",
                                          "description": "Requested item description",
                                          "categoryId": %d,
                                          "location": "Lima",
                                          "wantedItem": "Another item",
                                          "condition": "GOOD"
                                        }
                                        """.formatted(category.getId())))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id", notNullValue()))
                        .andExpect(jsonPath("$.ownerId",
                                is(receiver.getId().intValue())))
                        .andExpect(jsonPath("$.categoryId",
                                is(category.getId().intValue())))
        );

        Long offeredItemId = idOf(
                mockMvc.perform(post(BASE + "/items")
                                .header("Authorization", bearer(proposer))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "Offered item",
                                          "description": "Offered item description",
                                          "categoryId": %d,
                                          "location": "Lima",
                                          "wantedItem": "Requested item",
                                          "condition": "LIKE_NEW"
                                        }
                                        """.formatted(category.getId())))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id", notNullValue()))
        );

        mockMvc.perform(post(BASE + "/proposals")
                        .header("Authorization", bearer(proposer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "offeredItemId": %d,
                                  "requestedItemId": %d,
                                  "message": "Deseo intercambiar"
                                }
                                """.formatted(
                                offeredItemId,
                                requestedItemId
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.offeredItemId",
                        is(offeredItemId.intValue())))
                .andExpect(jsonPath("$.requestedItemId",
                        is(requestedItemId.intValue())))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    void refreshTokenRotatesAndCannotBeReused() throws Exception {
        String refreshToken = registerAndGetRefreshToken();

        String replacementToken = objectMapper.readTree(
                        mockMvc.perform(post(BASE + "/auth/refresh")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("""
                                                {
                                                  "refreshToken": "%s"
                                                }
                                                """.formatted(refreshToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token", notNullValue()))
                                .andExpect(jsonPath("$.refreshToken",
                                        notNullValue()))
                                .andReturn()
                                .getResponse()
                                .getContentAsString()
                )
                .get("refreshToken")
                .asText();

        assertThat(replacementToken).isNotEqualTo(refreshToken);

        mockMvc.perform(post(BASE + "/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRevokesRefreshToken() throws Exception {
        String refreshToken = registerAndGetRefreshToken();

        mockMvc.perform(post(BASE + "/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post(BASE + "/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void proposalsAreOnlyVisibleToParticipants() throws Exception {
        String suffix = uniqueSuffix();

        User owner = saveUser(
                "visible-owner-" + suffix,
                Role.USER
        );

        User proposer = saveUser(
                "visible-proposer-" + suffix,
                Role.USER
        );

        User outsider = saveUser(
                "visible-outsider-" + suffix,
                Role.USER
        );

        Category category = saveCategory("VisibleCategory-" + suffix);
        Item requestedItem = saveItem(owner, category);
        Item offeredItem = saveItem(proposer, category);

        Long proposalId = idOf(
                postProposal(proposer, offeredItem, requestedItem)
                        .andExpect(status().isCreated())
        );

        mockMvc.perform(get(BASE + "/proposals")
                        .header("Authorization", bearer(proposer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get(BASE + "/proposals")
                        .header("Authorization", bearer(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get(BASE + "/proposals")
                        .header("Authorization", bearer(outsider)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get(BASE + "/proposals/" + proposalId)
                        .header("Authorization", bearer(outsider)))
                .andExpect(status().isForbidden());
    }

    @Test
    void itemsCanBeFilteredAndPaginated() throws Exception {
        String suffix = uniqueSuffix();

        User owner = saveUser(
                "filter-owner-" + suffix,
                Role.USER
        );

        Category books = saveCategory("Books-" + suffix);
        Category games = saveCategory("Games-" + suffix);

        Item book = saveItem(owner, books);
        book.setName("Spring Boot Book");
        book.setLocation("Lima");
        book.setCondition(ItemCondition.GOOD);
        itemRepository.save(book);

        Item game = saveItem(owner, games);
        game.setName("Board Game");
        game.setLocation("Arequipa");
        game.setCondition(ItemCondition.LIKE_NEW);
        itemRepository.save(game);

        mockMvc.perform(get(BASE + "/items")
                        .param("categoryId", books.getId().toString())
                        .param("condition", "GOOD")
                        .param("location", "Lima")
                        .param("q", "Spring")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id",
                        is(book.getId().intValue())))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(10)))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    void usersManageFavoritesInterestsAndRecommendations() throws Exception {
        String suffix = uniqueSuffix();

        User currentUser = saveUser(
                "preferences-user-" + suffix,
                Role.USER
        );

        User owner = saveUser(
                "preferences-owner-" + suffix,
                Role.USER
        );

        Category category = saveCategory(
                "PreferencesCategory-" + suffix
        );

        Item recommendation = saveItem(owner, category);

        mockMvc.perform(put(BASE
                        + "/users/me/favorites/"
                        + recommendation.getId())
                        .header("Authorization", bearer(currentUser)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE + "/users/me/favorites")
                        .header("Authorization", bearer(currentUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath(
                        "$[0].id",
                        is(recommendation.getId().intValue())
                ));

        mockMvc.perform(put(BASE
                        + "/users/me/interests/"
                        + category.getId())
                        .header("Authorization", bearer(currentUser)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE + "/users/me/recommendations")
                        .header("Authorization", bearer(currentUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath(
                        "$[0].id",
                        is(recommendation.getId().intValue())
                ));

        mockMvc.perform(delete(BASE
                        + "/users/me/favorites/"
                        + recommendation.getId())
                        .header("Authorization", bearer(currentUser)))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete(BASE
                        + "/users/me/interests/"
                        + category.getId())
                        .header("Authorization", bearer(currentUser)))
                .andExpect(status().isNoContent());
    }

    @Test
    void fullExchangeFlowCompletesAndAllowsReview() throws Exception {
        String suffix = uniqueSuffix();

        User owner = saveUser(
                "flow-owner-" + suffix,
                Role.USER
        );

        User proposer = saveUser(
                "flow-proposer-" + suffix,
                Role.USER
        );

        Category category = saveCategory("FlowCategory-" + suffix);
        Item requestedItem = saveItem(owner, category);
        Item offeredItem = saveItem(proposer, category);

        Long proposalId = idOf(
                postProposal(proposer, offeredItem, requestedItem)
                        .andExpect(status().isCreated())
        );

        Long exchangeId = idOf(
                mockMvc.perform(patch(BASE
                                + "/proposals/"
                                + proposalId
                                + "/accept")
                                .header("Authorization",
                                        bearer(owner)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.status",
                                is("PENDING")))
        );

        mockMvc.perform(patch(BASE
                        + "/exchanges/"
                        + exchangeId
                        + "/confirm")
                        .header("Authorization", bearer(proposer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.offeringUserConfirmed",
                        is(true)))
                .andExpect(jsonPath("$.status", is("PENDING")));

        mockMvc.perform(patch(BASE
                        + "/exchanges/"
                        + exchangeId
                        + "/confirm")
                        .header("Authorization", bearer(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receivingUserConfirmed",
                        is(true)))
                .andExpect(jsonPath("$.status", is("COMPLETED")));

        Item updatedOfferedItem =
                itemRepository.findById(offeredItem.getId())
                        .orElseThrow();

        Item updatedRequestedItem =
                itemRepository.findById(requestedItem.getId())
                        .orElseThrow();

        assertThat(updatedOfferedItem.getState())
                .isEqualTo(ItemState.TRADED);

        assertThat(updatedRequestedItem.getState())
                .isEqualTo(ItemState.TRADED);

        postReview(proposer, exchangeId, 5)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.receiverId",
                        is(owner.getId().intValue())));

        postReview(proposer, exchangeId, 4)
                .andExpect(status().isConflict());

        mockMvc.perform(get(BASE
                        + "/reviews/users/"
                        + owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void cancellingExchangeReleasesReservedItems() throws Exception {
        String suffix = uniqueSuffix();

        User owner = saveUser(
                "cancel-owner-" + suffix,
                Role.USER
        );

        User proposer = saveUser(
                "cancel-proposer-" + suffix,
                Role.USER
        );

        Category category = saveCategory("CancelCategory-" + suffix);
        Item requestedItem = saveItem(owner, category);
        Item offeredItem = saveItem(proposer, category);

        Long proposalId = idOf(
                postProposal(proposer, offeredItem, requestedItem)
                        .andExpect(status().isCreated())
        );

        Long exchangeId = idOf(
                mockMvc.perform(patch(BASE
                                + "/proposals/"
                                + proposalId
                                + "/accept")
                                .header("Authorization", bearer(owner)))
                        .andExpect(status().isCreated())
        );

        assertThat(itemRepository.findById(offeredItem.getId())
                .orElseThrow().getState())
                .isEqualTo(ItemState.RESERVED);

        assertThat(itemRepository.findById(requestedItem.getId())
                .orElseThrow().getState())
                .isEqualTo(ItemState.RESERVED);

        mockMvc.perform(delete(BASE
                        + "/items/"
                        + offeredItem.getId())
                        .header("Authorization", bearer(proposer)))
                .andExpect(status().isConflict());

        mockMvc.perform(patch(BASE
                        + "/exchanges/"
                        + exchangeId
                        + "/cancel")
                        .header("Authorization", bearer(proposer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        assertThat(itemRepository.findById(offeredItem.getId())
                .orElseThrow().getState())
                .isEqualTo(ItemState.AVAILABLE);

        assertThat(itemRepository.findById(requestedItem.getId())
                .orElseThrow().getState())
                .isEqualTo(ItemState.AVAILABLE);

        mockMvc.perform(patch(BASE
                        + "/exchanges/"
                        + exchangeId
                        + "/confirm")
                        .header("Authorization", bearer(owner)))
                .andExpect(status().isConflict());
    }

    private String registerAndGetRefreshToken() throws Exception {
        String suffix = uniqueSuffix();
        String email = "refresh-" + suffix + "@example.com";

        String response = mockMvc.perform(post(BASE + "/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Refresh User",
                                  "email": "%s",
                                  "password": "Password123"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response)
                .get("refreshToken")
                .asText();
    }

    private ResultActions postProposal(User proposer, Item offeredItem, Item requestedItem) throws Exception {
        return mockMvc.perform(post(BASE + "/proposals")
                .header("Authorization", bearer(proposer))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "offeredItemId": %d,
                          "requestedItemId": %d,
                          "message": "Propuesta de prueba"
                        }
                        """.formatted(
                        offeredItem.getId(),
                        requestedItem.getId()
                )));
    }

    private ResultActions postReview(User author, Long exchangeId, int rating) throws Exception {
        return mockMvc.perform(post(BASE + "/reviews")
                .header("Authorization", bearer(author))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "exchangeId": %d,
                          "rating": %d,
                          "comment": "Intercambio correcto"
                        }
                        """.formatted(exchangeId, rating)));
    }
}