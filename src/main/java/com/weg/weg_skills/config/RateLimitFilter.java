package com.weg.weg_skills.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.weg.weg_skills.exceptions.TooManyRequestsException;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private static final long GENERAL_LIMIT = 100;
    private static final long AUTH_LIMIT = 5;
    private static final long MAXIMUM_CLIENTS = 50_000;
    private static final Duration BUCKET_EXPIRATION = Duration.ofMinutes(15);
    private static final Duration REFILL_PERIOD = Duration.ofMinutes(1);

    private static final String LIMIT_HEADER = "X-RateLimit-Limit";
    private static final String REMAINING_HEADER = "X-RateLimit-Remaining";
    private static final String RETRY_AFTER_HEADER = "Retry-After";

    private static final Set<String> AUTH_ROUTES = Set.of(
            "/auth/login",
            "/auth/register"
    );

    private final HandlerExceptionResolver exceptionResolver;
    private final long generalLimit;
    private final long authLimit;
    private final Cache<String, Bucket> generalBuckets;
    private final Cache<String, Bucket> authBuckets;

    @Autowired
    public RateLimitFilter(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        this(exceptionResolver, GENERAL_LIMIT, AUTH_LIMIT);
    }

    RateLimitFilter(
            HandlerExceptionResolver exceptionResolver,
            long generalLimit,
            long authLimit
    ) {
        this.exceptionResolver = exceptionResolver;
        this.generalLimit = generalLimit;
        this.authLimit = authLimit;
        this.generalBuckets = createCache();
        this.authBuckets = createCache();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = getPathWithoutContextPath(request);

        if (shouldIgnore(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean authRoute = AUTH_ROUTES.contains(path);
        long limit = authRoute ? authLimit : generalLimit;
        String clientKey = authRoute ? getIpKey(request) : getClientKey(request);

        Cache<String, Bucket> buckets = authRoute ? authBuckets : generalBuckets;
        Bucket bucket = buckets.get(clientKey, ignored -> createBucket(limit));
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        response.setHeader(LIMIT_HEADER, Long.toString(limit));
        response.setHeader(REMAINING_HEADER, Long.toString(probe.getRemainingTokens()));

        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds = Math.max(
                1,
                TimeUnit.NANOSECONDS.toSeconds(
                        probe.getNanosToWaitForRefill() + TimeUnit.SECONDS.toNanos(1) - 1
                )
        );

        response.setHeader(RETRY_AFTER_HEADER, Long.toString(retryAfterSeconds));
        exceptionResolver.resolveException(
                request,
                response,
                null,
                new TooManyRequestsException()
        );
    }

    private Bucket createBucket(long limit) {
        return Bucket.builder()
                .addLimit(config -> config
                        .capacity(limit)
                        .refillGreedy(limit, REFILL_PERIOD)
                )
                .build();
    }

    private Cache<String, Bucket> createCache() {
        return Caffeine.newBuilder()
                .maximumSize(MAXIMUM_CLIENTS)
                .expireAfterAccess(BUCKET_EXPIRATION)
                .build();
    }

    private String getClientKey(HttpServletRequest request) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            Long userId = jwtAuthentication.getToken().getClaim("userId");

            if (userId != null) {
                return "user:" + userId;
            }
        }

        return getIpKey(request);
    }

    private String getIpKey(HttpServletRequest request) {
        return "ip:" + request.getRemoteAddr();
    }

    private String getPathWithoutContextPath(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }

    private boolean shouldIgnore(String path) {
        return path.startsWith("/docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator/health");
    }
}
