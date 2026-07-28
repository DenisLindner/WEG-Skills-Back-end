package com.weg.weg_skills.service;

import com.weg.weg_skills.TestData;
import com.weg.weg_skills.dto.CourseCreateRequestDTO;
import com.weg.weg_skills.enums.UserRole;
import com.weg.weg_skills.mapper.CourseMapper;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.projection.CourseWithRatingProjection;
import com.weg.weg_skills.repository.CourseRepository;
import com.weg.weg_skills.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(CourseCacheTest.TestConfig.class)
class CourseCacheTest {

    @Autowired CourseService courseService;
    @Autowired CourseRepository courseRepository;
    @Autowired UserRepository userRepository;
    @Autowired CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        reset(courseRepository, userRepository);
        Objects.requireNonNull(cacheManager.getCache("topCourses")).clear();
    }

    @Test
    void shouldCacheAndEvictTopCourses() {
        CourseWithRatingProjection projection = mock(CourseWithRatingProjection.class);
        when(projection.getId()).thenReturn(1L);
        when(projection.getTitle()).thenReturn("Java");
        when(projection.getRating()).thenReturn(9.0);
        when(courseRepository.findMostEnrollmentsCourses(any(Pageable.class))).thenReturn(List.of(projection));

        courseService.findMostEnrollments();
        courseService.findMostEnrollments();

        verify(courseRepository).findMostEnrollmentsCourses(any(Pageable.class));

        var instructor = TestData.user(1L, UserRole.INSTRUCTOR);
        when(userRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        courseService.create(new CourseCreateRequestDTO("Spring", null), 1L);
        courseService.findMostEnrollments();

        verify(courseRepository, times(2)).findMostEnrollmentsCourses(any(Pageable.class));
    }

    @Configuration
    @EnableCaching(proxyTargetClass = true)
    static class TestConfig {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("topCourses");
        }

        @Bean
        CourseRepository courseRepository() {
            return mock(CourseRepository.class);
        }

        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean
        MediaService mediaService() {
            return mock(MediaService.class);
        }

        @Bean
        CourseService courseService(
                CourseRepository courseRepository,
                MediaService mediaService,
                UserRepository userRepository
        ) {
            return new CourseService(courseRepository, new CourseMapper(), mediaService, userRepository);
        }
    }
}
