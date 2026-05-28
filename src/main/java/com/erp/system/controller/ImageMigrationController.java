package com.erp.system.controller;

import com.erp.system.service.ImageMigrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * One-time admin endpoint to migrate external product image URLs
 * to locally stored files.
 *
 * POST /api/admin/migrate-images
 *
 * Call this once after inserting the seed SQL.
 * Safe to call again — already-migrated rows are skipped.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class ImageMigrationController {

    private final ImageMigrationService imageMigrationService;

    @PostMapping("/migrate-images")
    public ResponseEntity<String> migrateImages() {
        String result = imageMigrationService.migrateAllExternalImages();
        return ResponseEntity.ok(result);
    }
}