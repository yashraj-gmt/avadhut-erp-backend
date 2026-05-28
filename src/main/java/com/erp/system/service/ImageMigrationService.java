package com.erp.system.service;

import com.erp.system.entity.ProductImage;
import com.erp.system.repository.ProductImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

/**
 * One-time migration: downloads every external image URL stored in
 * product_images, saves it under uploads/products/images/, and updates
 * the DB row with the new relative path.
 *
 * Trigger via POST /api/admin/migrate-images  (see ImageMigrationController)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageMigrationService {

    private final ProductImageRepository productImageRepository;

    @Value("${app.file.upload-dir:./uploads}")
    private String uploadBaseDir;

    // Only migrate rows whose image_url looks like an http(s) external URL
    private static final String HTTP_PREFIX  = "http://";
    private static final String HTTPS_PREFIX = "https://";

    // ── Public entry point ────────────────────────────────────────────────

    /**
     * Iterates all ProductImage rows that still hold an external URL,
     * downloads each image, stores it locally, and saves the new
     * relative path back to the DB.
     *
     * @return summary string  e.g. "Migrated 112/116 images. Failed: 4"
     */
    @Transactional
    public String migrateAllExternalImages() {

        List<ProductImage> images = productImageRepository.findAll();

        int success = 0, skipped = 0, failed = 0;

        for (ProductImage image : images) {

            String url = image.getImageUrl();

            // Skip rows already migrated (relative path, not http)
            if (!isExternalUrl(url)) {
                skipped++;
                continue;
            }

            try {
                String relativePath = downloadAndSave(url);
                image.setImageUrl(relativePath);

                // file_size: try to fill from the saved file
                try {
                    Path saved = Paths.get(uploadBaseDir, relativePath).toAbsolutePath();
                    image.setFileSize(Files.size(saved));
                } catch (Exception ignored) {}

                productImageRepository.save(image);
                success++;
                log.info("[{}] ✓  {} → {}", success, url, relativePath);

            } catch (Exception e) {
                failed++;
                log.warn("✗  Failed to download [{}]: {}", url, e.getMessage());
            }
        }

        String summary = String.format(
                "Migration complete. Success=%d | Skipped=%d | Failed=%d",
                success, skipped, failed);
        log.info(summary);
        return summary;
    }

    // ── Core download logic ───────────────────────────────────────────────

    /**
     * Downloads the image at {@code externalUrl} and writes it to
     * uploads/products/images/<uuid>.<ext>
     *
     * @return relative DB path, e.g. "products/images/abc-123.jpg"
     */
    private String downloadAndSave(String externalUrl) throws Exception {

        // 1. Build target directory
        Path targetDir = Paths.get(uploadBaseDir, FileUploadService.DIR_PRODUCT_IMAGES)
                .toAbsolutePath()
                .normalize();
        Files.createDirectories(targetDir);

        // 2. Derive extension from URL (fallback → jpg)
        String ext = extractExtension(externalUrl);

        // 3. Unique filename
        String fileName = UUID.randomUUID() + "." + ext;
        Path   filePath = targetDir.resolve(fileName);

        // 4. Open connection with browser-like headers
        //    (avoids 403 from servers that block plain Java UA)
        HttpURLConnection conn = (HttpURLConnection) new URL(externalUrl).openConnection();
        conn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) " +
                        "Chrome/124.0 Safari/537.36");
        conn.setRequestProperty("Accept",
                "image/avif,image/webp,image/apng,image/jpeg,*/*;q=0.8");
        conn.setRequestProperty("Referer", "https://www.google.com/");
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(20_000);
        conn.setInstanceFollowRedirects(true);

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("HTTP " + responseCode + " for URL: " + externalUrl);
        }

        // 5. Stream to disk
        try (InputStream in = conn.getInputStream()) {
            Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            conn.disconnect();
        }

        return FileUploadService.DIR_PRODUCT_IMAGES + "/" + fileName;
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private boolean isExternalUrl(String url) {
        if (url == null || url.isBlank()) return false;
        String lower = url.toLowerCase();
        return lower.startsWith(HTTP_PREFIX) || lower.startsWith(HTTPS_PREFIX);
    }

    /**
     * Tries to pull extension from the URL path.
     * Falls back to "jpg" for Google thumbnail URLs and anything without an ext.
     */
    private String extractExtension(String url) {
        try {
            String path = new URL(url).getPath();          // e.g. /images/photo.webp
            int dot = path.lastIndexOf('.');
            if (dot > 0 && dot < path.length() - 1) {
                String ext = path.substring(dot + 1).toLowerCase();
                // Only trust known image extensions
                if (List.of("jpg", "jpeg", "png", "webp", "gif").contains(ext)) {
                    return ext;
                }
            }
        } catch (Exception ignored) {}
        return "jpg";  // safe default for Google / Amazon thumbnail URLs
    }
}