package com.erp.system.config;

import com.erp.system.security.CustomUserDetailsService;
import com.erp.system.security.JwtAccessDeniedHandler;
import com.erp.system.security.JwtAuthenticationEntryPoint;
import com.erp.system.security.JwtAuthenticationFilter;
import com.erp.system.security.SecurityConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService  userDetailsService;
    private final JwtAuthenticationFilter   jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint entryPoint;
    private final JwtAccessDeniedHandler    accessDeniedHandler;

    // ── Beans

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    // ── Security filter chain ─────────────────────────────────────────────

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SecurityConstants.PUBLIC_URLS).permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        // SUPER_ADMIN only
                        .requestMatchers("/api/super-admin/**")
                        .hasAuthority(SecurityConstants.ROLE_SUPER_ADMIN)

                        // SUPER_ADMIN or ADMIN
                        .requestMatchers("/api/admin/**")
                        .hasAnyAuthority(
                                SecurityConstants.ROLE_SUPER_ADMIN,
                                SecurityConstants.ROLE_ADMIN)

                        .anyRequest().authenticated()
                )

                .authenticationProvider(authenticationProvider())

                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ── CORS ──────────────────────────────────────────────────────────────

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // In production: replace with exact allowed origins
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

//    @Bean
//    public CommandLineRunner run(PasswordEncoder encoder) {
//        return args -> {
//            System.out.println("SuperAdmin : " + encoder.encode("SuperAdmin@123"));
//            System.out.println("Admin      : " + encoder.encode("Admin@123"));
//            System.out.println("Staff      : " + encoder.encode("Staff@123"));
//        };
//    }

}