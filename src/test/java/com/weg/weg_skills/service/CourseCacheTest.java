package com.weg.weg_skills.service;

import com.weg.weg_skills.TestData;
import com.weg.weg_skills.dto.CourseCreateRequestDTO;
import com.weg.weg_skills.enums.CourseStatus;
import com.weg.weg_skills.enums.UserRole;
import com.weg.weg_skills.mapper.CertificateMapper;
import com.weg.weg_skills.mapper.CourseMapper;
import com.weg.weg_skills.mapper.LessonProgressMapper;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.projection.CourseWithRatingProjection;
import com.weg.weg_skills.repository.CertificateRepository;
import com.weg.weg_skills.repository.CourseRepository;
import com.weg.weg_skills.repository.EnrollmentRepository;
import com.weg.weg_skills.repository.LessonProgressRepository;
import com.weg.weg_skills.repository.LessonRepository;
import com.weg.weg_skills.repository.ModuleRepository;
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
import static org.mockito.ArgumentMatchers.eq;
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
        when(courseRepository.findMostEnrollmentsCourses(eq(CourseStatus.PUBLISHED), any(Pageable.class))).thenReturn(List.of(projection));

        courseService.findMostEnrollments();
        courseService.findMostEnrollments();

        verify(courseRepository).findMostEnrollmentsCourses(eq(CourseStatus.PUBLISHED), any(Pageable.class));

        var instructor = TestData.user(1L, UserRole.INSTRUCTOR);
        when(userRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        courseService.create(new CourseCreateRequestDTO("Spring", null), 1L);
        courseService.findMostEnrollments();

        verify(courseRepository, times(2)).findMostEnrollmentsCourses(eq(CourseStatus.PUBLISHED), any(Pageable.class));
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
        LessonProgressRepository lessonProgressRepository() {
            return mock(LessonProgressRepository.class);
        }

        @Bean
        EnrollmentRepository enrollmentRepository() {
            return mock(EnrollmentRepository.class);
        }

        @Bean
        CertificateRepository certificateRepository() {
            return mock(CertificateRepository.class);
        }

        @Bean
        ModuleRepository moduleRepository() {
            return mock(ModuleRepository.class);
        }

        @Bean
        LessonRepository lessonRepository() {
            return mock(LessonRepository.class);
        }

        @Bean
        CourseService courseService(
                CourseRepository courseRepository,
                MediaService mediaService,
                UserRepository userRepository,
                LessonProgressRepository lessonProgressRepository,
                EnrollmentRepository enrollmentRepository,
                CertificateRepository certificateRepository,
                ModuleRepository moduleRepository,
                LessonRepository lessonRepository
        ) {
            return new CourseService(moduleRepository, lessonRepository, courseRepository, new CourseMapper(), mediaService, userRepository,
                    lessonProgressRepository, new LessonProgressMapper(), enrollmentRepository,
                    certificateRepository, new CertificateMapper());
        }
    }
}
