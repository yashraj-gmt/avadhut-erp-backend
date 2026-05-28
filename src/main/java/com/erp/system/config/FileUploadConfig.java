package com.erp.system.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

import java.nio.file.Paths;

/**
 * Maps the on-disk upload directory to the /uploads/** URL prefix
 * so that uploaded files are accessible via:
 *   GET http://host:port/uploads/products/images/uuid.jpg
 */
@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${app.file.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absoluteUploadPath =
                Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();

        if (!absoluteUploadPath.endsWith("/")) {
            absoluteUploadPath = absoluteUploadPath + "/";
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(absoluteUploadPath)
                .setCachePeriod(3600);
    }
}