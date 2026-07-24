package com.weg.weg_skills.config;

import com.weg.weg_skills.exceptions.TooManyRequestsException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock
    HandlerExceptionResolver exceptionResolver;
    @Mock
    FilterChain filterChain;

    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter(exceptionResolver, 2, 2);
    }

    @AfterEach
    void cleanSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldLimitAuthenticationRequestsByIp() throws Exception {
        filter.doFilter(request("POST", "/auth/login", "10.0.0.1"), new MockHttpServletResponse(), filterChain);
        filter.doFilter(request("POST", "/auth/login", "10.0.0.1"), new MockHttpServletResponse(), filterChain);

        MockHttpServletRequest blockedRequest = request("POST", "/auth/login", "10.0.0.1");
        MockHttpServletResponse blockedResponse = new MockHttpServletResponse();

        filter.doFilter(blockedRequest, blockedResponse, filterChain);

        verify(filterChain, times(2)).doFilter(
                any(),
                any()
        );
        verify(exceptionResolver).resolveException(
                eq(blockedRequest),
                eq(blockedResponse),
                isNull(),
                isA(TooManyRequestsException.class)
        );
        assertThat(blockedResponse.getHeader("X-RateLimit-Limit")).isEqualTo("2");
        assertThat(blockedResponse.getHeader("X-RateLimit-Remaining")).isEqualTo("0");
        assertThat(blockedResponse.getHeader("Retry-After")).isNotBlank();
    }

    @Test
    void shouldUseSeparateBucketsForAuthenticatedUsers() throws Exception {
        authenticate(1L);
        filter.doFilter(request("GET", "/courses", "10.0.0.1"), new MockHttpServletResponse(), filterChain);
        filter.doFilter(request("GET", "/courses", "10.0.0.1"), new MockHttpServletResponse(), filterChain);

        MockHttpServletRequest blockedRequest = request("GET", "/courses", "10.0.0.1");
        filter.doFilter(blockedRequest, new MockHttpServletResponse(), filterChain);

        authenticate(2L);
        filter.doFilter(request("GET", "/courses", "10.0.0.1"), new MockHttpServletResponse(), filterChain);

        verify(filterChain, times(3)).doFilter(
                any(),
                any()
        );
        verify(exceptionResolver, times(1)).resolveException(
                eq(blockedRequest),
                any(),
                isNull(),
                isA(TooManyRequestsException.class)
        );
    }

    @Test
    void shouldSkipOptionsAndOperationalRoutes() throws Exception {
        filter.doFilter(request("OPTIONS", "/courses", "10.0.0.1"), new MockHttpServletResponse(), filterChain);
        filter.doFilter(request("GET", "/actuator/health", "10.0.0.1"), new MockHttpServletResponse(), filterChain);
        filter.doFilter(request("GET", "/swagger-ui/index.html", "10.0.0.1"), new MockHttpServletResponse(), filterChain);

        verify(filterChain, times(3)).doFilter(
                any(),
                any()
        );
        verify(exceptionResolver, never()).resolveException(
                any(),
                any(),
                any(),
                any()
        );
    }

    private MockHttpServletRequest request(String method, String path, String remoteAddress) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, "/api" + path);
        request.setContextPath("/api");
        request.setRemoteAddr(remoteAddress);
        return request;
    }

    private void authenticate(Long userId) {
        Instant now = Instant.now();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("user@example.com")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .claim("userId", userId)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(jwt)
        );
    }
}
