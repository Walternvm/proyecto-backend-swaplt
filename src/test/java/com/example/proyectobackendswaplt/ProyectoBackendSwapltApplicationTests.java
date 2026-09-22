package com.example.proyectobackendswaplt;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.validation.Validator;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.example.proyectobackendswaplt.category.domain.Category;
import com.example.proyectobackendswaplt.category.infrastructure.CategoryRepository;
import com.example.proyectobackendswaplt.user.domain.User;
import com.example.proyectobackendswaplt.user.infrastructure.UserRepository;
import com.example.proyectobackendswaplt.item.domain.Item;
import com.example.proyectobackendswaplt.item.infrastructure.ItemRepository;
import com.example.proyectobackendswaplt.publication.domain.Publication;
import com.example.proyectobackendswaplt.publication.infrastructure.PublicationRepository;
import com.example.proyectobackendswaplt.proposal.domain.Proposal;
import com.example.proyectobackendswaplt.proposal.infrastructure.ProposalRepository;
import com.example.proyectobackendswaplt.proposal.domain.ProposalService;
import com.example.proyectobackendswaplt.review.domain.Review;
import java.util.UUID;
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

    @Test
    void contextLoads() {
    }

    @Test
    void registerLoginAndProtectUsers() throws Exception {
        mockMvc.perform(get("/api/users")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/register").contentType("application/json")
                .content("{\"name\":\"Ana\",\"email\":\"ana@example.com\",\"password\":\"password123\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.role").value("USER"));
        String login = mockMvc.perform(post("/api/auth/login").contentType("application/json")
                .content("{\"email\":\"ana@example.com\",\"password\":\"password123\"}"))
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

}
