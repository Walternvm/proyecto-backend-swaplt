package com.example.proyectobackendswaplt.support;

import com.example.proyectobackendswaplt.auth.JwtService;
import com.example.proyectobackendswaplt.category.domain.Category;
import com.example.proyectobackendswaplt.category.infrastructure.CategoryRepository;
import com.example.proyectobackendswaplt.item.domain.Item;
import com.example.proyectobackendswaplt.item.domain.ItemCondition;
import com.example.proyectobackendswaplt.item.infrastructure.ItemRepository;
import com.example.proyectobackendswaplt.publication.domain.Publication;
import com.example.proyectobackendswaplt.publication.infrastructure.PublicationRepository;
import com.example.proyectobackendswaplt.proposal.infrastructure.ProposalRepository;
import com.example.proyectobackendswaplt.exchange.infrastructure.ExchangeRepository;
import com.example.proyectobackendswaplt.user.domain.Role;
import com.example.proyectobackendswaplt.user.domain.User;
import com.example.proyectobackendswaplt.user.infrastructure.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:swaplt-tests;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=local-test-secret-at-least-thirty-two-characters"
})
@AutoConfigureMockMvc
public abstract class IntegrationTestSupport {

    protected static final String BASE = "/api/v1";

    @Autowired protected MockMvc mockMvc;
    @Autowired protected UserRepository userRepository;
    @Autowired protected CategoryRepository categoryRepository;
    @Autowired protected ItemRepository itemRepository;
    @Autowired protected PublicationRepository publicationRepository;
    @Autowired protected ProposalRepository proposalRepository;
    @Autowired protected ExchangeRepository exchangeRepository;
    @Autowired protected JwtService jwtService;

    protected String uniqueSuffix() {
        return UUID.randomUUID().toString();
    }

    protected User saveUser(String name, Role role) {
        User user = new User();
        user.setName(name);
        user.setEmail(name + "@example.com");
        user.setPassword("password-hash");
        user.setRole(role);
        return userRepository.save(user);
    }

    protected Category saveCategory(String name) {
        Category category = new Category();
        category.setName(name);
        return categoryRepository.save(category);
    }

    protected Item saveItem(User owner, Category category) {
        return saveItem(owner, category, ItemCondition.NEW, "Lima");
    }

    protected Item saveItem(User owner, Category category, ItemCondition condition, String location) {
        Item item = new Item();
        item.setUser(owner);
        item.setCategory(category);
        item.setName("Item");
        item.setLocation(location);
        item.setCondition(condition);
        return itemRepository.save(item);
    }

    protected Publication savePublication(User owner, Item item) {
        Publication publication = new Publication();
        publication.setUser(owner);
        publication.setItem(item);
        publication.setWantedItem("Anything");
        return publicationRepository.save(publication);
    }

    protected String bearer(User user) {
        return "Bearer " + jwtService.createToken(user);
    }

    protected Long idOf(ResultActions actions) throws Exception {
        String json = actions.andReturn().getResponse().getContentAsString();
        return new ObjectMapper().readTree(json).get("id").asLong();
    }
}
