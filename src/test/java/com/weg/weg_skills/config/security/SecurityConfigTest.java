package com.weg.weg_skills.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityConfigTest {

    private final SecurityConfig config = new SecurityConfig();

    @Test
    void shouldCreateSecretKeyFromBase64() {
        JwtProperties properties = new JwtProperties(
                "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
                Duration.ofHours(1),
                "weg-skills-api"
        );

        var key = config.jwtSecretKey(properties);

        assertThat(key.getAlgorithm()).isEqualTo("HmacSHA256");
        assertThat(key.getEncoded()).hasSize(32);
    }

    @Test
    void shouldRejectInvalidOrShortSecrets() {
        assertThatThrownBy(() -> config.jwtSecretKey(
                new JwtProperties("not-base64!", Duration.ofHours(1), "issuer")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Base64");

        assertThatThrownBy(() -> config.jwtSecretKey(
                new JwtProperties("c2hvcnQ=", Duration.ofHours(1), "issuer")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }

    @Test
    void shouldConfigureCors() {
        var source = config.corsConfigurationSource(new CorsProperties(List.of("http://localhost:3000")));

        var cors = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/courses"));

        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOrigins()).containsExactly("http://localhost:3000");
        assertThat(cors.getAllowedMethods()).contains("GET", "POST", "PATCH", "DELETE", "OPTIONS");
        assertThat(cors.getAllowedHeaders()).contains("Authorization", "Content-Type", "Accept");
        assertThat(cors.getExposedHeaders()).contains(
                "Retry-After",
                "X-RateLimit-Limit",
                "X-RateLimit-Remaining"
        );
        assertThat(cors.getAllowCredentials()).isFalse();
        assertThat(cors.getMaxAge()).isEqualTo(3600L);
    }

    @Test
    void shouldConvertRolesClaimToAuthorities() {
        Instant now = Instant.now();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("user@example.com")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .claim("roles", List.of("STUDENT", "ADMIN"))
                .build();

        var authentication = config.jwtAuthenticationConverter().convert(jwt);

        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .contains("ROLE_STUDENT", "ROLE_ADMIN");
    }
}
