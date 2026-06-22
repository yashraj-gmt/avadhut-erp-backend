package com.erp.system.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/**
 * Configures an AWS S3-compatible client pointing at DigitalOcean Spaces.
 * <p>
 * Active only in 'production' profile — local dev continues using the
 * local-disk FileUploadService (no S3 dependency needed during development).
 * <p>
 * Required environment variables (set in /opt/erp/.env.production):
 *   SPACES_ACCESS_KEY   — your DO Spaces access key
 *   SPACES_SECRET_KEY   — your DO Spaces secret key
 *   SPACES_ENDPOINT     — e.g. https://blr1.digitaloceanspaces.com
 *   SPACES_REGION       — e.g. blr1
 *   SPACES_BUCKET       — e.g. erp-uploads
 */
@Configuration
@Profile("production")
public class SpacesConfig {

    @Value("${spaces.access-key}")
    private String accessKey;

    @Value("${spaces.secret-key}")
    private String secretKey;

    @Value("${spaces.endpoint}")
    private String endpoint;

    @Value("${spaces.region}")
    private String region;

    @Bean
    public S3Client spacesS3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .serviceConfiguration(
                        S3Configuration.builder()
                                .pathStyleAccessEnabled(true)   // required for Spaces
                                .build()
                )
                .build();
    }
}
