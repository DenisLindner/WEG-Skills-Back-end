package com.weg.weg_skills.config;

import com.weg.weg_skills.dto.CourseWithRatingResponseDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import tools.jackson.databind.type.TypeFactory;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    RedisCacheConfiguration redisCacheConfiguration() {
        var listType = TypeFactory.createDefaultInstance()
                .constructCollectionType(List.class, CourseWithRatingResponseDTO.class);
        var serializer = new JacksonJsonRedisSerializer<List<CourseWithRatingResponseDTO>>(listType);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(2))
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> "weg-skills:v1:" + cacheName + "::")
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
    }
}
