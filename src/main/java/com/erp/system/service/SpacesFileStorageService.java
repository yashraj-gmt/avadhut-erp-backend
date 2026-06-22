package com.erp.system.service;

import com.erp.system.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.*;

/**
 * Production file-storage service backed by DigitalOcean Spaces (S3-compatible).
 * <p>
 * Activated only in the 'production' Spring profile.
 * Annotated with @Primary so it takes precedence over the local-disk FileUploadService
 * when both beans exist on the classpath.
 * <p>
 * Key behaviours:
 *  - Files are uploaded under the same sub-directory structure as local storage
 *    (products/images/, products/qr/, etc.) so the rest of the codebase is unchanged.
 *  - The stored DB value is the relative key, e.g. "products/images/uuid.jpg"
 *  - toPublicUrl() returns the fully-qualified CDN / Spaces URL.
 *  - deleteFile() removes the object from Spaces (silently ignores missing keys).
 */
@Slf4j
@Service
@Primary
@Profile("production")
@RequiredArgsConstructor
public class SpacesFileStorageService extends FileUploadService {

    private final S3Client spacesS3Client;

    @Value("${spaces.bucket}")
    private String bucket;

    @Value("${spaces.cdn-url}")
    private String cdnUrl;   // e.g. https://erp-uploads.blr1.cdn.digitaloceanspaces.com
                             // or   https://erp-uploads.blr1.digitaloceanspaces.com

    // ── Allowed types (mirrors parent) ────────────────────────────────────

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif"
    );
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 MB

    // ── Public API (overrides local-disk implementations) ─────────────────

    /**
     * Upload a single image to Spaces.
     *
     * @param file         the uploaded file
     * @param subDirectory one of the DIR_* constants, e.g. "products/images"
     * @return relative key stored in DB, e.g. "products/images/uuid.jpg"
     */
    @Override
    public String uploadImage(MultipartFile file, String subDirectory) {
        validateFile(file);
        return doUpload(file, subDirectory);
    }

    /**
     * Upload a QR-code image.
     */
    @Override
    public String uploadQrCode(MultipartFile file) {
        validateFile(file);
        return doUpload(file, DIR_PRODUCT_QR);
    }

    /**
     * Upload multiple images to Spaces.
     */
    @Override
    public List<String> uploadMultipleImages(List<MultipartFile> files, String subDirectory) {
        if (files == null || files.isEmpty()) return List.of();
        List<String> keys = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                keys.add(uploadImage(file, subDirectory));
            }
        }
        return keys;
    }

    /**
     * Delete an object from Spaces by its relative DB key.
     * Silently ignores if the object does not exist.
     */
    @Override
    public void deleteFile(String relativeKey) {
        if (relativeKey == null || relativeKey.isBlank()) return;
        try {
            spacesS3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(relativeKey)
                    .build());
            log.info("Deleted from Spaces: {}", relativeKey);
        } catch (NoSuchKeyException e) {
            log.warn("Object not found in Spaces (ignoring): {}", relativeKey);
        } catch (Exception e) {
            log.warn("Could not delete Spaces object [{}]: {}", relativeKey, e.getMessage());
        }
    }

    /**
     * Delete multiple objects. Never throws — logs warnings on failure.
     */
    @Override
    public void deleteFiles(List<String> relativeKeys) {
        if (relativeKeys == null) return;
        relativeKeys.forEach(this::deleteFile);
    }

    /**
     * Convert a relative DB key to a public URL.
     * e.g. "products/images/uuid.jpg"
     *    → "https://erp-uploads.blr1.cdn.digitaloceanspaces.com/products/images/uuid.jpg"
     */
    @Override
    public String toPublicUrl(String relativeKey) {
        if (relativeKey == null || relativeKey.isBlank()) return null;
        // Strip any leading slash
        String clean = relativeKey.startsWith("/") ? relativeKey.substring(1) : relativeKey;
        return cdnUrl.replaceAll("/$", "") + "/" + clean;
    }

    // ── Internal helpers ──────────────────────────────────────────────────

    private String doUpload(MultipartFile file, String subDirectory) {
        String originalName = StringUtils.cleanPath(
                Objects.requireNonNull(file.getOriginalFilename(), "Filename is null")
        );
        String extension  = extractExtension(originalName);
        String uniqueName = UUID.randomUUID() + "." + extension;
        String key        = subDirectory + "/" + uniqueName;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .acl(ObjectCannedACL.PUBLIC_READ)   // files are publicly readable
                    .build();

            spacesS3Client.putObject(request, RequestBody.fromInputStream(
                    file.getInputStream(), file.getSize()
            ));

            log.info("Uploaded to Spaces: {}", key);
            return key;

        } catch (IOException e) {
            log.error("Failed to read file input stream for Spaces upload", e);
            throw new AppException("File upload failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (S3Exception e) {
            log.error("Spaces S3 error during upload of key [{}]: {}", key, e.awsErrorDetails().errorMessage());
            throw new AppException("File upload to storage failed: " + e.awsErrorDetails().errorMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException("File cannot be empty", HttpStatus.BAD_REQUEST);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(
                    "File size exceeds the 10 MB limit (received: "
                            + (file.getSize() / 1024 / 1024) + " MB)",
                    HttpStatus.BAD_REQUEST
            );
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new AppException(
                    "Unsupported file type [" + contentType + "]. Allowed: " + ALLOWED_IMAGE_TYPES,
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE
            );
        }
    }

    private String extractExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return (dot > 0 && dot < fileName.length() - 1)
                ? fileName.substring(dot + 1).toLowerCase()
                : "jpg";
    }
}
