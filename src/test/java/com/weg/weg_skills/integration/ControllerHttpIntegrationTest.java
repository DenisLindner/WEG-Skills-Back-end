package com.weg.weg_skills.integration;

import com.weg.weg_skills.controller.AuthController;
import com.weg.weg_skills.controller.CourseController;
import com.weg.weg_skills.controller.LessonController;
import com.weg.weg_skills.controller.ModuleController;
import com.weg.weg_skills.controller.ReviewController;
import com.weg.weg_skills.dto.AuthResponseDTO;
import com.weg.weg_skills.dto.CourseResponseDTO;
import com.weg.weg_skills.dto.CourseWithRatingResponseDTO;
import com.weg.weg_skills.enums.CourseStatus;
import com.weg.weg_skills.exceptions.GlobalExceptionHandler;
import com.weg.weg_skills.exceptions.ResourceNotFoundException;
import com.weg.weg_skills.service.AuthService;
import com.weg.weg_skills.service.CourseService;
import com.weg.weg_skills.service.LessonService;
import com.weg.weg_skills.service.ModuleService;
import com.weg.weg_skills.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ControllerHttpIntegrationTest {

    @Mock AuthService authService;
    @Mock CourseService courseService;
    @Mock ModuleService moduleService;
    @Mock LessonService lessonService;
    @Mock ReviewService reviewService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new AuthController(authService),
                        new CourseController(courseService),
                        new ModuleController(moduleService),
                        new LessonController(lessonService),
                        new ReviewController(reviewService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldRegisterUserThroughHttp() throws Exception {
        when(authService.register(any())).thenReturn(new AuthResponseDTO(
                "access-token", "Bearer", Instant.parse("2030-01-01T00:00:00Z")));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "John Doe",
                                  "email": "john@example.com",
                                  "password": "Strong1!"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void shouldReturnBadRequestWhenServiceRejectsRequest() throws Exception {
        when(authService.register(any())).thenThrow(new IllegalArgumentException("invalid request"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"John Doe",
                                  "email":"john@example.com",
                                  "password":"Strong1!"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.type").value("urn:problem:bad-request"));
    }

    @Test
    void shouldReturnInvalidFields() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Jo",
                                  "email":"invalid-email",
                                  "password":"Strong1!"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").value("Name must contain between 3 and 128 characters"))
                .andExpect(jsonPath("$.errors.email").value("Email must be a valid email"));
    }

    @Test
    void shouldListCatalogResourcesThroughHttp() throws Exception {
        when(courseService.findAllPublished(0, 10)).thenReturn(new PageImpl<>(
                List.of(new CourseResponseDTO(1L, "Course", "Description", CourseStatus.PUBLISHED, null)), PageRequest.of(0, 10), 1));
        when(courseService.findAllByTitlePublished("Course", 0, 10)).thenReturn(new PageImpl<>(
                List.of(new CourseResponseDTO(1L, "Course", "Description", CourseStatus.PUBLISHED, null)), PageRequest.of(0, 10), 1));
        when(courseService.findMostEnrollments()).thenReturn(List.of(
                new CourseWithRatingResponseDTO(1L, "Course", "Description", CourseStatus.PUBLISHED, 9.0, null)));

        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Course"));
        mockMvc.perform(get("/courses/title").param("title", "Course"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Course"));
        mockMvc.perform(get("/courses/top-courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rating").value(9.0));
    }

    @Test
    void shouldReturnProblemDetailsWhenResourceDoesNotExist() throws Exception {
        when(courseService.findAllByTitlePublished("Missing", 0, 10))
                .thenThrow(new ResourceNotFoundException("Course", 99L));

        mockMvc.perform(get("/courses/title").param("title", "Missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.detail").value("Course with id: 99, not found"))
                .andExpect(jsonPath("$.instance").value("/courses/title"));
    }
}
