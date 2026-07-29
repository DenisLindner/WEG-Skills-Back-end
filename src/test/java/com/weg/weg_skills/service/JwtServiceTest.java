package com.weg.weg_skills.service;

import com.weg.weg_skills.config.security.JwtProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock JwtEncoder encoder;

    @Test
    void shouldGenerateTokenWithApplicationClaims() {
        Jwt encodedJwt = Jwt.withTokenValue("encoded-token")
                .header("alg", "HS256")
                .subject("user@example.com")
                .build();
        when(encoder.encode(any())).thenReturn(encodedJwt);
        JwtService service = new JwtService(encoder,
                new JwtProperties("secret", Duration.ofHours(2), "weg-skills-api"));
        Instant before = Instant.now();

        JwtService.GeneratedToken token = service.generateToken(
                "user@example.com",
                10L,
                List.of(new SimpleGrantedAuthority("ROLE_STUDENT"), new SimpleGrantedAuthority("SCOPE_read"))
        );

        assertThat(token.value()).isEqualTo("encoded-token");
        assertThat(token.expiresAt()).isBetween(before.plus(Duration.ofHours(2)), Instant.now().plus(Duration.ofHours(2)));

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(encoder).encode(captor.capture());
        var claims = captor.getValue().getClaims();
        assertThat((String) claims.getClaim("iss")).isEqualTo("weg-skills-api");
        assertThat(claims.getSubject()).isEqualTo("user@example.com");
        assertThat((Long) claims.getClaim("userId")).isEqualTo(10L);
        assertThat(claims.getClaimAsStringList("roles")).containsExactly("STUDENT");
    }
}
