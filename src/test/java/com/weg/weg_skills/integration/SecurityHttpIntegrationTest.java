package com.weg.weg_skills.integration;

import com.weg.weg_skills.config.security.SecurityConfig;
import com.weg.weg_skills.controller.AuthController;
import com.weg.weg_skills.controller.CourseController;
import com.weg.weg_skills.controller.LessonController;
import com.weg.weg_skills.controller.UserController;
import com.weg.weg_skills.dto.AuthResponseDTO;
import com.weg.weg_skills.dto.CourseResponseDTO;
import com.weg.weg_skills.dto.LessonProgressDetailsResponseDTO;
import com.weg.weg_skills.enums.CourseStatus;
import com.weg.weg_skills.service.AuthService;
import com.weg.weg_skills.service.CourseService;
import com.weg.weg_skills.service.LessonService;
import com.weg.weg_skills.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, CourseController.class, LessonController.class, UserController.class})
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "security.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "security.jwt.expiration=1h",
        "security.jwt.issuer=weg-skills-api",
        "cors.config.origins=http://localhost:3000"
})
class SecurityHttpIntegrationTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean AuthService authService;
    @MockitoBean CourseService courseService;
    @MockitoBean LessonService lessonService;
    @MockitoBean UserService userService;
    @MockitoBean UserDetailsService userDetailsService;

    @Test
    void shouldAllowPublicAuthenticationEndpoint() throws Exception {
        when(authService.login(any())).thenReturn(new AuthResponseDTO("token", "Bearer", Instant.now()));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"password"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRequireAuthenticationForProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/courses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowPublicTopCoursesEndpoint() throws Exception {
        when(courseService.findMostEnrollments()).thenReturn(List.of());

        mockMvc.perform(get("/courses/top-courses"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowOnlyInstructorOrAdminToCreateCourse() throws Exception {
        String body = """
                {"title":"Java Course","description":"Description"}
                """;

        mockMvc.perform(post("/courses")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        when(courseService.create(any(), eq(1L)))
                .thenReturn(new CourseResponseDTO(1L, "Java Course", "Description", CourseStatus.DRAFT, null));
        mockMvc.perform(post("/courses")
                        .with(jwt()
                                .jwt(token -> token.claim("userId", 1L))
                                .authorities(new SimpleGrantedAuthority("ROLE_INSTRUCTOR")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldAllowStudentToCompleteLesson() throws Exception {
        when(lessonService.completeLesson(4L, 1L, List.of("STUDENT")))
                .thenReturn(new LessonProgressDetailsResponseDTO(4L, 5L, Instant.now()));

        mockMvc.perform(put("/lessons/4/completion")
                        .with(jwt()
                                .jwt(token -> token.claim("userId", 1L).claim("roles", List.of("STUDENT")))
                                .authorities(new SimpleGrantedAuthority("ROLE_STUDENT"))))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowOnlyInstructorOrAdminToPublishCourse() throws Exception {
        mockMvc.perform(patch("/courses/2/publish")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT"))))
                .andExpect(status().isForbidden());

        when(courseService.publish(2L, 1L, List.of("INSTRUCTOR")))
                .thenReturn(new CourseResponseDTO(2L, "Course", "Description", CourseStatus.PUBLISHED, null));
        mockMvc.perform(patch("/courses/2/publish")
                        .with(jwt()
                                .jwt(token -> token.claim("userId", 1L).claim("roles", List.of("INSTRUCTOR")))
                                .authorities(new SimpleGrantedAuthority("ROLE_INSTRUCTOR"))))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowOnlyAdminToCreateInstructor() throws Exception {
        String body = """
                {"name":"Instructor","email":"instructor@example.com"}
                """;

        mockMvc.perform(post("/users/instructor")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_INSTRUCTOR")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/users/instructor")
                        .with(jwt()
                                .jwt(token -> token.claim("userId", 1L))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }
}
