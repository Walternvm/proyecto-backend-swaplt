package com.example.proyectobackendswaplt.item.infrastructure;

import com.example.proyectobackendswaplt.common.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
public class ItemImageStorageService {
    private static final long MAX_IMAGE_SIZE =
            5L * 1024L * 1024L;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;

    public ItemImageStorageService(
            S3Client s3Client,
            S3Presigner s3Presigner,
            @Value("${app.storage.s3.bucket}")
            String bucket
    ) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
    }

    public String upload(Long itemId, MultipartFile file) {
        byte[] content = readAndValidate(file);
        ImageType imageType = detectImageType(content);

        String key = "items/"
                + itemId
                + "/"
                + UUID.randomUUID()
                + "."
                + imageType.extension();

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(requireBucket())
                    .key(key)
                    .contentType(imageType.contentType())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromBytes(content)
            );

            return key;
        } catch (SdkException exception) {
            throw new ImageStorageException(
                    "No se pudo almacenar la imagen"
            );
        }
    }

    public String createReadUrl(String key) {
        try {
            GetObjectRequest objectRequest =
                    GetObjectRequest.builder()
                            .bucket(requireBucket())
                            .key(key)
                            .build();

            GetObjectPresignRequest request =
                    GetObjectPresignRequest.builder()
                            .signatureDuration(
                                    Duration.ofMinutes(10)
                            )
                            .getObjectRequest(objectRequest)
                            .build();

            return s3Presigner
                    .presignGetObject(request)
                    .url()
                    .toExternalForm();
        } catch (SdkException exception) {
            throw new ImageStorageException(
                    "No se pudo obtener la imagen"
            );
        }
    }

    public void delete(String key) {
        if (key == null || key.isBlank()) {
            return;
        }

        try {
            DeleteObjectRequest request =
                    DeleteObjectRequest.builder()
                            .bucket(requireBucket())
                            .key(key)
                            .build();

            s3Client.deleteObject(request);
        } catch (SdkException exception) {
            throw new ImageStorageException(
                    "No se pudo eliminar la imagen"
            );
        }
    }

    private byte[] readAndValidate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException(
                    "Debes seleccionar una imagen"
            );
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BadRequestException(
                    "La imagen no puede superar los 5 MB"
            );
        }

        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new BadRequestException(
                    "No se pudo leer la imagen"
            );
        }
    }

    private ImageType detectImageType(byte[] content) {
        if (isJpeg(content)) {
            return new ImageType("image/jpeg", "jpg");
        }

        if (isPng(content)) {
            return new ImageType("image/png", "png");
        }

        if (isWebp(content)) {
            return new ImageType("image/webp", "webp");
        }

        throw new BadRequestException(
                "La imagen debe ser JPEG, PNG o WebP"
        );
    }

    private boolean isJpeg(byte[] content) {
        return content.length >= 3
                && content[0] == (byte) 0xFF
                && content[1] == (byte) 0xD8
                && content[2] == (byte) 0xFF;
    }

    private boolean isPng(byte[] content) {
        byte[] signature = {
                (byte) 0x89, 0x50, 0x4E, 0x47,
                0x0D, 0x0A, 0x1A, 0x0A
        };

        if (content.length < signature.length) {
            return false;
        }

        for (int index = 0; index < signature.length; index++) {
            if (content[index] != signature[index]) {
                return false;
            }
        }

        return true;
    }

    private boolean isWebp(byte[] content) {
        return content.length >= 12
                && content[0] == 'R'
                && content[1] == 'I'
                && content[2] == 'F'
                && content[3] == 'F'
                && content[8] == 'W'
                && content[9] == 'E'
                && content[10] == 'B'
                && content[11] == 'P';
    }

    private String requireBucket() {
        if (bucket == null || bucket.isBlank()) {
            throw new ImageStorageException(
                    "El almacenamiento de imagenes no esta configurado"
            );
        }

        return bucket;
    }

    private record ImageType(
            String contentType,
            String extension
    ) {
    }
}