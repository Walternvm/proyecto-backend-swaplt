package com.example.proyectobackendswaplt;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import jakarta.validation.Validator;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.example.proyectobackendswaplt.category.domain.Category;
import com.example.proyectobackendswaplt.category.infrastructure.CategoryRepository;
import com.example.proyectobackendswaplt.user.domain.Role;
import com.example.proyectobackendswaplt.user.domain.User;
import com.example.proyectobackendswaplt.user.infrastructure.UserRepository;
import com.example.proyectobackendswaplt.item.domain.Item;
import com.example.proyectobackendswaplt.item.domain.ItemCondition;
import com.example.proyectobackendswaplt.item.infrastructure.ItemRepository;
import com.example.proyectobackendswaplt.publication.domain.Publication;
import com.example.proyectobackendswaplt.publication.infrastructure.PublicationRepository;
import com.example.proyectobackendswaplt.proposal.domain.Proposal;
import com.example.proyectobackendswaplt.proposal.infrastructure.ProposalRepository;
import com.example.proyectobackendswaplt.proposal.domain.ProposalService;
import com.example.proyectobackendswaplt.review.domain.Review;
import com.example.proyectobackendswaplt.auth.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:swaplt;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=local-test-secret-at-least-thirty-two-characters"
})
@AutoConfigureMockMvc
class ProyectoBackendSwapltApplicationTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private Validator validator;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private PublicationRepository publicationRepository;
    @Autowired
    private ProposalRepository proposalRepository;
    @Autowired
    private ProposalService proposalService;
    @Autowired
    private JwtService jwtService;

    @Test
    void contextLoads() {
    }

    @Test
    void registerLoginAndProtectUsers() throws Exception {
        mockMvc.perform(get("/api/users")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/register").contentType("application/json")
                        .content("{\"name\":\"Ana\",\"email\":\"ana@example.com\",\"password\":\"Password123\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.role").value("USER"));
        String login = mockMvc.perform(post("/api/auth/login").contentType("application/json")
                        .content("{\"email\":\"ana@example.com\",\"password\":\"Password123\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        String token = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(login).get("token").asText();
        mockMvc.perform(get("/api/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void validatesRequiredFieldsAndRating() {
        Category category = new Category();
        category.setName(" ");
        assertFalse(validator.validate(category).isEmpty());

        Publication publication = new Publication();
        publication.setWantedItem(" ");
        assertTrue(validator.validate(publication).stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("wantedItem")));

        Review review = new Review();
        review.setRating(6);
        assertTrue(validator.validate(review).stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("rating")));
    }

    @Test
    @Transactional
    void exchangeHasTwoDifferentParticipants() {
        String suffix = UUID.randomUUID().toString();
        User offerer = new User();
        offerer.setName("Offerer");
        offerer.setEmail("offerer-" + suffix + "@example.com");
        offerer.setPassword("password-hash");
        userRepository.save(offerer);

        User receiver = new User();
        receiver.setName("Receiver");
        receiver.setEmail("receiver-" + suffix + "@example.com");
        receiver.setPassword("password-hash");
        userRepository.save(receiver);

        Category category = new Category();
        category.setName("Category-" + suffix);
        categoryRepository.save(category);

        Item offered = new Item();
        offered.setUser(offerer);
        offered.setCategory(category);
        offered.setName("Book");
        offered.setLocation("Lima");
        itemRepository.save(offered);

        Item requested = new Item();
        requested.setUser(receiver);
        requested.setCategory(category);
        requested.setName("Game");
        requested.setLocation("Lima");
        itemRepository.save(requested);

        Publication publication = new Publication();
        publication.setUser(receiver);
        publication.setItem(requested);
        publication.setWantedItem("Book");
        publicationRepository.save(publication);

        Proposal proposal = new Proposal();
        proposal.setUser(offerer);
        proposal.setOfferedItem(offered);
        proposal.setRequestedItem(requested);
        proposal.setPublication(publication);
        proposalRepository.save(proposal);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(receiver.getEmail(), null));
        try {
            var exchange = proposalService.acceptProposal(proposal.getId());
            assertEquals(offerer.getId(), exchange.getOfferingUser().getId());
            assertEquals(receiver.getId(), exchange.getReceivingUser().getId());
            assertNotEquals(exchange.getOfferingUser().getId(), exchange.getReceivingUser().getId());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @Transactional
    void createsResourcesWithIdsAndFlatResponses() throws Exception {
        String suffix = UUID.randomUUID().toString();
        User offerer = new User();
        offerer.setName("Offerer");
        offerer.setEmail("dto-offerer-" + suffix + "@example.com");
        offerer.setPassword("password-hash");
        userRepository.save(offerer);

        User receiver = new User();
        receiver.setName("Receiver");
        receiver.setEmail("dto-receiver-" + suffix + "@example.com");
        receiver.setPassword("password-hash");
        userRepository.save(receiver);

        Category category = new Category();
        category.setName("Dto-" + suffix);
        categoryRepository.save(category);

        String itemBody = "{\"name\":\"Book\",\"categoryId\":" + category.getId()
                + ",\"location\":\"Lima\",\"ownerId\":" + receiver.getId() + "}";
        String offeredJson = mockMvc.perform(post("/api/items")
                        .header("Authorization", "Bearer " + jwtService.createToken(offerer))
                        .contentType("application/json").content(itemBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ownerId").value(offerer.getId().intValue()))
                .andExpect(jsonPath("$.user").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        Long offeredItemId = new ObjectMapper().readTree(offeredJson).get("id").asLong();

        String requestedJson = mockMvc.perform(post("/api/items")
                        .header("Authorization", "Bearer " + jwtService.createToken(receiver))
                        .contentType("application/json").content(itemBody))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        Long requestedItemId = new ObjectMapper().readTree(requestedJson).get("id").asLong();

        String publicationJson = mockMvc.perform(post("/api/publications")
                        .header("Authorization", "Bearer " + jwtService.createToken(receiver))
                        .contentType("application/json")
                        .content("{\"itemId\":" + requestedItemId + ",\"wantedItem\":\"Book\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itemId").value(requestedItemId.intValue()))
                .andExpect(jsonPath("$.item").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        Long publicationId = new ObjectMapper().readTree(publicationJson).get("id").asLong();

        mockMvc.perform(post("/api/proposals")
                        .header("Authorization", "Bearer " + jwtService.createToken(offerer))
                        .contentType("application/json")
                        .content("{\"offeredItemId\":" + offeredItemId + ",\"publicationId\":" + publicationId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.proposerId").value(offerer.getId().intValue()))
                .andExpect(jsonPath("$.publicationId").value(publicationId.intValue()))
                .andExpect(jsonPath("$.user").doesNotExist());
    }

    @Test
    void refreshTokenRotatesAndOldTokenIsRejected() throws Exception {
        String oldRefresh = registerAndGetRefreshToken();
        String body = "{\"refreshToken\":\"" + oldRefresh + "\"}";

        mockMvc.perform(post("/api/auth/refresh").contentType("application/json").content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").value(not(oldRefresh)));

        mockMvc.perform(post("/api/auth/refresh").contentType("application/json").content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRevokesRefreshToken() throws Exception {
        String refresh = registerAndGetRefreshToken();
        String body = "{\"refreshToken\":\"" + refresh + "\"}";

        mockMvc.perform(post("/api/auth/logout").contentType("application/json").content(body))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/refresh").contentType("application/json").content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void proposalsAreOnlyVisibleToParticipantsAndAdmin() throws Exception {
        String suffix = UUID.randomUUID().toString();
        User offerer = saveUser("vis-offerer-" + suffix, Role.USER);
        User receiver = saveUser("vis-receiver-" + suffix, Role.USER);
        User outsider = saveUser("vis-outsider-" + suffix, Role.USER);
        User admin = saveUser("vis-admin-" + suffix, Role.ADMIN);

        Category category = saveCategory("Vis-" + suffix);

        Item offered = saveItem(offerer, category);
        Item requested = saveItem(receiver, category);

        Publication publication = savePublication(receiver, requested);

        Proposal proposal = new Proposal();
        proposal.setUser(offerer);
        proposal.setOfferedItem(offered);
        proposal.setRequestedItem(requested);
        proposal.setPublication(publication);
        proposalRepository.save(proposal);

        String url = "/api/proposals/" + proposal.getId();
        mockMvc.perform(get(url).header("Authorization", bearer(outsider))).andExpect(status().isForbidden());
        mockMvc.perform(get(url).header("Authorization", bearer(receiver))).andExpect(status().isOk());
        mockMvc.perform(get(url).header("Authorization", bearer(admin))).andExpect(status().isOk());

        mockMvc.perform(get("/api/proposals").header("Authorization", bearer(outsider)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/api/proposals").header("Authorization", bearer(offerer)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void itemsCanBeFilteredAndPaginated() throws Exception {
        String suffix = UUID.randomUUID().toString();
        User owner = saveUser("filter-owner-" + suffix, Role.USER);
        Category category = saveCategory("Filter-" + suffix);
        saveItem(owner, category, ItemCondition.NEW, "Lima Centro");
        saveItem(owner, category, ItemCondition.FAIR, "Arequipa");
        String categoryId = String.valueOf(category.getId());

        mockMvc.perform(get("/api/items").param("categoryId", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));

        mockMvc.perform(get("/api/items").param("categoryId", categoryId).param("condition", "NEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].condition").value("NEW"));

        mockMvc.perform(get("/api/items").param("categoryId", categoryId).param("location", "lima"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/items").param("categoryId", categoryId).param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalPages").value(2));

        mockMvc.perform(get("/api/items").param("state", "NOT_A_STATE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void usersManageFavoritesInterestsAndRecommendations() throws Exception {
        String suffix = UUID.randomUUID().toString();
        User owner = saveUser("fav-owner-" + suffix, Role.USER);
        User fan = saveUser("fav-fan-" + suffix, Role.USER);
        Category category = saveCategory("Fav-" + suffix);
        Publication publication = savePublication(owner, saveItem(owner, category));

        String favoriteUrl = "/api/users/me/favorites/" + publication.getId();
        mockMvc.perform(put(favoriteUrl).header("Authorization", bearer(fan))).andExpect(status().isNoContent());
        mockMvc.perform(put(favoriteUrl).header("Authorization", bearer(fan))).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/users/me/favorites").header("Authorization", bearer(fan)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(publication.getId().intValue()));

        mockMvc.perform(put(favoriteUrl).header("Authorization", bearer(owner))).andExpect(status().isConflict());

        mockMvc.perform(delete(favoriteUrl).header("Authorization", bearer(fan))).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/users/me/favorites").header("Authorization", bearer(fan)))
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(put("/api/users/me/interests/" + category.getId()).header("Authorization", bearer(fan)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/users/me/recommendations").header("Authorization", bearer(fan)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/users/me").header("Authorization", bearer(fan)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(fan.getEmail()));
        mockMvc.perform(get("/api/users").header("Authorization", bearer(fan)))
                .andExpect(status().isForbidden());
    }

    @Test
    void fullExchangeFlowRequiresBothConfirmationsAndSingleReview() throws Exception {
        String suffix = UUID.randomUUID().toString();
        User owner = saveUser("flow-owner-" + suffix, Role.USER);
        User proposer = saveUser("flow-proposer-" + suffix, Role.USER);
        User outsider = saveUser("flow-outsider-" + suffix, Role.USER);
        Category category = saveCategory("Flow-" + suffix);
        Item requested = saveItem(owner, category);
        Item offered = saveItem(proposer, category);
        Publication publication = savePublication(owner, requested);

        Long proposalId = idOf(postProposal(proposer, offered, publication).andExpect(status().isCreated()));
        postProposal(proposer, offered, publication).andExpect(status().isConflict());

        String acceptUrl = "/api/proposals/" + proposalId + "/accept";
        mockMvc.perform(put(acceptUrl).header("Authorization", bearer(proposer))).andExpect(status().isForbidden());
        Long exchangeId = idOf(mockMvc.perform(put(acceptUrl).header("Authorization", bearer(owner)))
                .andExpect(status().isCreated()));

        mockMvc.perform(get("/api/items/" + offered.getId())).andExpect(jsonPath("$.state").value("RESERVED"));
        postReview(proposer, exchangeId).andExpect(status().isConflict());

        String completeUrl = "/api/exchanges/" + exchangeId + "/complete";
        mockMvc.perform(put(completeUrl).header("Authorization", bearer(proposer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.offeringUserConfirmed").value(true));
        mockMvc.perform(put(completeUrl).header("Authorization", bearer(proposer))).andExpect(status().isConflict());
        mockMvc.perform(put(completeUrl).header("Authorization", bearer(outsider))).andExpect(status().isForbidden());
        mockMvc.perform(put(completeUrl).header("Authorization", bearer(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mockMvc.perform(get("/api/items/" + requested.getId())).andExpect(jsonPath("$.state").value("TRADED"));
        mockMvc.perform(get("/api/publications/" + publication.getId())).andExpect(jsonPath("$.status").value("CLOSED"));

        postReview(outsider, exchangeId).andExpect(status().isForbidden());
        postReview(proposer, exchangeId).andExpect(status().isCreated());
        postReview(proposer, exchangeId).andExpect(status().isConflict());
        mockMvc.perform(get("/api/reviews").param("userId", String.valueOf(owner.getId()))
                        .header("Authorization", bearer(outsider)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void cancellingReleasesItemsAndProposerCanWithdraw() throws Exception {
        String suffix = UUID.randomUUID().toString();
        User owner = saveUser("cancel-owner-" + suffix, Role.USER);
        User proposer = saveUser("cancel-proposer-" + suffix, Role.USER);
        Category category = saveCategory("Cancel-" + suffix);
        Item requested = saveItem(owner, category);
        Item offered = saveItem(proposer, category);
        Item spare = saveItem(proposer, category);
        Publication publication = savePublication(owner, requested);

        mockMvc.perform(post("/api/publications").header("Authorization", bearer(owner))
                        .contentType("application/json")
                        .content("{\"itemId\":" + requested.getId() + ",\"wantedItem\":\"Otro\"}"))
                .andExpect(status().isConflict());

        Long spareProposalId = idOf(postProposal(proposer, spare, publication).andExpect(status().isCreated()));
        String withdrawUrl = "/api/proposals/" + spareProposalId + "/cancel";
        mockMvc.perform(put(withdrawUrl).header("Authorization", bearer(owner))).andExpect(status().isForbidden());
        mockMvc.perform(put(withdrawUrl).header("Authorization", bearer(proposer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        mockMvc.perform(put(withdrawUrl).header("Authorization", bearer(proposer))).andExpect(status().isConflict());

        Long proposalId = idOf(postProposal(proposer, offered, publication).andExpect(status().isCreated()));
        Long exchangeId = idOf(mockMvc.perform(put("/api/proposals/" + proposalId + "/accept")
                .header("Authorization", bearer(owner))).andExpect(status().isCreated()));

        mockMvc.perform(delete("/api/items/" + offered.getId()).header("Authorization", bearer(proposer)))
                .andExpect(status().isConflict());

        mockMvc.perform(put("/api/exchanges/" + exchangeId + "/cancel").header("Authorization", bearer(proposer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(get("/api/items/" + offered.getId())).andExpect(jsonPath("$.state").value("AVAILABLE"));
        mockMvc.perform(get("/api/items/" + requested.getId())).andExpect(jsonPath("$.state").value("AVAILABLE"));
        mockMvc.perform(put("/api/exchanges/" + exchangeId + "/complete").header("Authorization", bearer(owner)))
                .andExpect(status().isConflict());
    }

    private String registerAndGetRefreshToken() throws Exception {
        String email = "refresh-" + UUID.randomUUID() + "@example.com";
        String json = mockMvc.perform(post("/api/auth/register").contentType("application/json")
                        .content("{\"name\":\"Rita\",\"email\":\"" + email + "\",\"password\":\"Password123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        return new ObjectMapper().readTree(json).get("refreshToken").asText();
    }

    private ResultActions postProposal(User proposer, Item offered, Publication publication) throws Exception {
        return mockMvc.perform(post("/api/proposals").header("Authorization", bearer(proposer))
                .contentType("application/json")
                .content("{\"offeredItemId\":" + offered.getId() + ",\"publicationId\":" + publication.getId() + "}"));
    }

    private ResultActions postReview(User author, Long exchangeId) throws Exception {
        return mockMvc.perform(post("/api/reviews").header("Authorization", bearer(author))
                .contentType("application/json")
                .content("{\"exchangeId\":" + exchangeId + ",\"rating\":5,\"comment\":\"Todo bien\"}"));
    }

    private Long idOf(ResultActions actions) throws Exception {
        String json = actions.andReturn().getResponse().getContentAsString();
        return new ObjectMapper().readTree(json).get("id").asLong();
    }

    private User saveUser(String name, Role role) {
        User user = new User();
        user.setName(name);
        user.setEmail(name + "@example.com");
        user.setPassword("password-hash");
        user.setRole(role);
        return userRepository.save(user);
    }

    private Category saveCategory(String name) {
        Category category = new Category();
        category.setName(name);
        return categoryRepository.save(category);
    }

    private Item saveItem(User owner, Category category) {
        return saveItem(owner, category, null, "Lima");
    }

    private Item saveItem(User owner, Category category, ItemCondition condition, String location) {
        Item item = new Item();
        item.setUser(owner);
        item.setCategory(category);
        item.setName("Item");
        item.setLocation(location);
        item.setCondition(condition);
        return itemRepository.save(item);
    }

    private Publication savePublication(User owner, Item item) {
        Publication publication = new Publication();
        publication.setUser(owner);
        publication.setItem(item);
        publication.setWantedItem("Anything");
        return publicationRepository.save(publication);
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.createToken(user);
    }
}