package com.example.proyectobackendswaplt.item;

import com.example.proyectobackendswaplt.common.exception.BadRequestException;
import com.example.proyectobackendswaplt.item.infrastructure.ItemImageStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class ItemImageStorageServiceTest {

    @Test
    void rejectsInvalidImageContent() {
        var service = service();

        var file = new MockMultipartFile(
                "image",
                "fake.png",
                "image/png",
                "esto no es una imagen".getBytes()
        );

        assertThatThrownBy(
                () -> service.upload(1L, file)
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessage(
                        "La imagen debe ser JPEG, PNG o WebP"
                );
    }

    @Test
    void rejectsImageLargerThanFiveMegabytes() {
        var service = service();

        var file = new MockMultipartFile(
                "image",
                "large.png",
                "image/png",
                new byte[(5 * 1024 * 1024) + 1]
        );

        assertThatThrownBy(
                () -> service.upload(1L, file)
        )
                .isInstanceOf(BadRequestException.class)
                .hasMessage(
                        "La imagen no puede superar los 5 MB"
                );
    }

    private ItemImageStorageService service() {
        return new ItemImageStorageService(
                mock(S3Client.class),
                mock(S3Presigner.class),
                "test-bucket"
        );
    }
}