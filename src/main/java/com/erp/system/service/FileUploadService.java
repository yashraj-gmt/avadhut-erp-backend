package com.erp.system.service;

import com.erp.system.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Reusable file-upload service.
 *
 * Upload directory layout (under app.file.upload-dir):
 *   uploads/
 *     products/
 *       images/   ← product gallery images
 *       qr/       ← product QR codes
 *     categories/
 *       icons/    ← optional category icons
 *
 * Stored DB value  : relative path  e.g. "products/images/uuid.jpg"
 * Served HTTP URL  : /uploads/products/images/uuid.jpg
 *   (configured via FileUploadConfig WebMvcConfigurer)
 */
@Slf4j
@Service
public class FileUploadService {

    @Value("${app.file.upload-dir:./uploads}")
    private String uploadBaseDir;

    /** Sub-directory constants — pass to upload methods */
    public static final String DIR_PRODUCT_IMAGES = "products/images";
    public static final String DIR_PRODUCT_QR     = "products/qr";
    public static final String DIR_CATEGORY_ICONS = "categories/icons";

    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif"
    );

    private static final Set<String> ALLOWED_QR_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;  // 10 MB

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Upload a single image file into the given sub-directory.
     *
     * @param file         the uploaded file
     * @param subDirectory one of the DIR_* constants, or a custom path
     * @return relative path stored in DB (e.g. "products/images/uuid.jpg")
     */
    public String uploadImage(MultipartFile file, String subDirectory) {
        validateImageFile(file, ALLOWED_IMAGE_CONTENT_TYPES);
        return doUpload(file, subDirectory);
    }

    /**
     * Upload a QR code image.
     *
     * @return relative path, e.g. "products/qr/uuid.png"
     */
    public String uploadQrCode(MultipartFile file) {
        validateImageFile(file, ALLOWED_QR_CONTENT_TYPES);
        return doUpload(file, DIR_PRODUCT_QR);
    }

    /**
     * Upload multiple images at once.
     *
     * @return list of relative paths in the same order as input files
     */
    public List<String> uploadMultipleImages(List<MultipartFile> files, String subDirectory) {
        if (files == null || files.isEmpty()) return List.of();

        List<String> paths = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                paths.add(uploadImage(file, subDirectory));
            }
        }
        return paths;
    }

    /**
     * Delete a file by its relative DB path.
     * Silently ignores if the file does not exist.
     */
    public void deleteFile(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return;
        try {
            Path path = resolveAbsolutePath(relativePath);
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                log.info("Deleted file: {}", relativePath);
            } else {
                log.warn("File not found for deletion: {}", relativePath);
            }
        } catch (IOException e) {
            log.warn("Could not delete file [{}]: {}", relativePath, e.getMessage());
        }
    }

    /**
     * Delete multiple files. Never throws — logs warnings on failures.
     */
    public void deleteFiles(List<String> relativePaths) {
        if (relativePaths == null) return;
        relativePaths.forEach(this::deleteFile);
    }

    /**
     * Convert a relative DB path to a public HTTP URL.
     * e.g. "products/images/uuid.jpg" → "/uploads/products/images/uuid.jpg"
     */
    public String toPublicUrl(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return null;
        return "/uploads/" + relativePath;
    }

    // ── Internal helpers ──────────────────────────────────────────────────

    private String doUpload(MultipartFile file, String subDirectory) {
        try {
            Path uploadPath = resolveAbsolutePath(subDirectory);
            Files.createDirectories(uploadPath);

            String originalName = StringUtils.cleanPath(
                    Objects.requireNonNull(file.getOriginalFilename(), "Filename is null")
            );
            String extension    = extractExtension(originalName);
            String uniqueName   = UUID.randomUUID() + "." + extension;
            Path   targetPath   = uploadPath.resolve(uniqueName);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String relativePath = subDirectory + "/" + uniqueName;
            log.info("File uploaded → {}", relativePath);
            return relativePath;

        } catch (IOException ex) {
            log.error("File upload failed for subDir [{}]", subDirectory, ex);
            throw new AppException(
                    "File upload failed: " + ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private void validateImageFile(MultipartFile file, Set<String> allowedTypes) {
        if (file == null || file.isEmpty()) {
            throw new AppException("File cannot be empty", HttpStatus.BAD_REQUEST);
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new AppException(
                    "File size exceeds the 10 MB limit (received: "
                            + (file.getSize() / 1024 / 1024) + " MB)",
                    HttpStatus.BAD_REQUEST
            );
        }
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType.toLowerCase())) {
            throw new AppException(
                    "Unsupported file type [" + contentType + "]. Allowed: " + allowedTypes,
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE
            );
        }
    }

    private Path resolveAbsolutePath(String relativePath) {
        return Paths.get(uploadBaseDir, relativePath).toAbsolutePath().normalize();
    }

    private String extractExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return (dot > 0 && dot < fileName.length() - 1)
                ? fileName.substring(dot + 1).toLowerCase()
                : "jpg";
    }
}