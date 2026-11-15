package com.example.proyectobackendswaplt.item;

import com.example.proyectobackendswaplt.support.IntegrationTestSupport;
import com.example.proyectobackendswaplt.user.domain.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import com.example.proyectobackendswaplt.item.infrastructure.ItemImageStorageService;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ItemControllerTest extends IntegrationTestSupport {

    @MockitoBean
    private ItemImageStorageService itemImageStorageService;

    @Test
    void currentUserCanListOnlyTheirItems() throws Exception {
        var owner = saveUser("owner-" + uniqueSuffix(), Role.USER);
        var other = saveUser("other-" + uniqueSuffix(), Role.USER);
        var category = saveCategory("Items-" + uniqueSuffix());
        var mine = saveItem(owner, category);
        saveItem(other, category);

        mockMvc.perform(get(BASE + "/users/me/items")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearer(owner)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath(
                        "$[0].id",
                        is(mine.getId().intValue())
                ));
    }

    @Test
    void ownerCanUploadItemImage() throws Exception {
        var owner = saveUser(
                "image-owner-" + uniqueSuffix(),
                Role.USER
        );
        var category = saveCategory(
                "Image-" + uniqueSuffix()
        );
        var item = saveItem(owner, category);

        String previousKey =
                "items/" + item.getId() + "/previous.png";

        item.setImageKey(previousKey);
        itemRepository.saveAndFlush(item);

        var image = new MockMultipartFile(
                "image",
                "item.png",
                "image/png",
                new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47}
        );

        when(itemImageStorageService.upload(
                eq(item.getId()),
                any()
        )).thenReturn(
                "items/" + item.getId() + "/image.png"
        );

        mockMvc.perform(
                        multipart(
                                HttpMethod.PUT,
                                BASE + "/items/"
                                        + item.getId()
                                        + "/image"
                        )
                                .file(image)
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearer(owner)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.imageUrl",
                        is(
                                "http://localhost:8080/api/v1/items/"
                                        + item.getId()
                                        + "/image"
                        )
                ));

        verify(itemImageStorageService).upload(
                eq(item.getId()),
                any()
        );
        verify(itemImageStorageService).delete(previousKey);
    }

    @Test
    void deletingItemRemovesStoredImage() throws Exception {
        var owner = saveUser(
                "delete-image-owner-" + uniqueSuffix(),
                Role.USER
        );
        var category = saveCategory(
                "Delete-image-" + uniqueSuffix()
        );
        var item = saveItem(owner, category);

        String imageKey =
                "items/" + item.getId() + "/image.png";

        item.setImageKey(imageKey);
        itemRepository.saveAndFlush(item);

        mockMvc.perform(
                        delete(
                                BASE + "/items/" + item.getId()
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearer(owner)
                                )
                )
                .andExpect(status().isNoContent());

        verify(itemImageStorageService).delete(imageKey);
    }

    @Test
    void nonOwnerCannotUploadItemImage() throws Exception {
        var owner = saveUser(
                "real-owner-" + uniqueSuffix(),
                Role.USER
        );
        var other = saveUser(
                "other-user-" + uniqueSuffix(),
                Role.USER
        );
        var category = saveCategory(
                "Protected-" + uniqueSuffix()
        );
        var item = saveItem(owner, category);

        var image = new MockMultipartFile(
                "image",
                "item.png",
                "image/png",
                new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47}
        );

        mockMvc.perform(
                        multipart(
                                HttpMethod.PUT,
                                BASE + "/items/"
                                        + item.getId()
                                        + "/image"
                        )
                                .file(image)
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearer(other)
                                )
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(itemImageStorageService);
    }

    @Test
    void itemImageEndpointRedirectsToStorage() throws Exception {
        var owner = saveUser(
                "redirect-owner-" + uniqueSuffix(),
                Role.USER
        );
        var category = saveCategory(
                "Redirect-" + uniqueSuffix()
        );
        var item = saveItem(owner, category);

        item.setImageKey(
                "items/" + item.getId() + "/image.png"
        );
        itemRepository.save(item);

        when(itemImageStorageService.createReadUrl(
                item.getImageKey()
        )).thenReturn(
                "https://example.com/signed-image"
        );

        mockMvc.perform(
                        get(
                                BASE + "/items/"
                                        + item.getId()
                                        + "/image"
                        )
                )
                .andExpect(status().isFound())
                .andExpect(header().string(
                        HttpHeaders.LOCATION,
                        "https://example.com/signed-image"
                ));
    }
}