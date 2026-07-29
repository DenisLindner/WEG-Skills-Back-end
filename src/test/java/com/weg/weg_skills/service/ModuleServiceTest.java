package com.weg.weg_skills.service;

import com.weg.weg_skills.TestData;
import com.weg.weg_skills.dto.CreateMediaUploadRequestDTO;
import com.weg.weg_skills.dto.ModuleCreateRequestDTO;
import com.weg.weg_skills.dto.ModuleUpdateRequestDTO;
import com.weg.weg_skills.dto.RepositionRequestDTO;
import com.weg.weg_skills.dto.UploadTicketResponseDTO;
import com.weg.weg_skills.enums.CourseStatus;
import com.weg.weg_skills.enums.MediaStatus;
import com.weg.weg_skills.enums.MediaType;
import com.weg.weg_skills.enums.UserRole;
import com.weg.weg_skills.exceptions.DuplicateResourceException;
import com.weg.weg_skills.exceptions.ForbiddenException;
import com.weg.weg_skills.exceptions.ResourceNotFoundException;
import com.weg.weg_skills.mapper.ModuleMapper;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Media;
import com.weg.weg_skills.model.Module;
import com.weg.weg_skills.model.User;
import com.weg.weg_skills.repository.CourseRepository;
import com.weg.weg_skills.repository.ModuleRepository;
import com.weg.weg_skills.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModuleServiceTest {

    @Mock ModuleRepository moduleRepository;
    @Mock CourseRepository courseRepository;
    @Mock MediaService mediaService;
    @Mock UserRepository userRepository;

    private ModuleService service;

    @BeforeEach
    void setUp() {
        service = new ModuleService(moduleRepository, new ModuleMapper(), courseRepository, mediaService, userRepository);
    }

    @Test
    void shouldCreateModuleForCourseOwner() {
        User instructor = TestData.user(1L, UserRole.INSTRUCTOR);
        Course course = TestData.course(2L, instructor);
        Module previousModule = TestData.module(10L, course);
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(moduleRepository.findTopByCourseOrderByPositionDesc(course)).thenReturn(previousModule);
        when(moduleRepository.save(any(Module.class))).thenAnswer(invocation -> {
            Module module = invocation.getArgument(0);
            module.setId(3L);
            return module;
        });

        var response = service.create(new ModuleCreateRequestDTO(" Module ", " Description ", 2L),
                1L, List.of("INSTRUCTOR"));

        assertThat(response.id()).isEqualTo(3L);
        assertThat(response.title()).isEqualTo("Module");
        assertThat(response.position()).isEqualTo(2L);
        assertThat(course.getCourseStatus()).isEqualTo(CourseStatus.DRAFT);
    }

    @Test
    void shouldRejectDuplicatedModuleAndNonOwner() {
        Course course = TestData.course(2L, TestData.user(1L, UserRole.INSTRUCTOR));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> service.create(new ModuleCreateRequestDTO("Module", null, 2L),
                9L, List.of("INSTRUCTOR"))).isInstanceOf(ForbiddenException.class);

        when(moduleRepository.existsByCourseAndTitleIgnoreCase(course, "Module")).thenReturn(true);
        assertThatThrownBy(() -> service.create(new ModuleCreateRequestDTO("Module", null, 2L),
                1L, List.of("INSTRUCTOR"))).isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void shouldReplacePendingModuleImage() {
        User instructor = TestData.user(1L, UserRole.INSTRUCTOR);
        Course course = TestData.course(2L, instructor);
        Module module = TestData.module(3L, course);
        Media previous = TestData.media(4L, instructor, MediaType.MODULE_IMAGE, MediaStatus.PENDING_UPLOAD);
        Media next = TestData.media(5L, instructor, MediaType.MODULE_IMAGE, MediaStatus.PENDING_UPLOAD);
        module.setPendingImage(previous);
        UploadTicketResponseDTO ticket = new UploadTicketResponseDTO(5L, "url", "key", Map.of(), Instant.now());
        when(userRepository.existsById(1L)).thenReturn(true);
        when(moduleRepository.findById(3L)).thenReturn(Optional.of(module));
        when(mediaService.createModuleImageUpload(any(), any(), any(), any()))
                .thenReturn(new CreatedMediaUpload(ticket, next));

        assertThat(service.uploadImage(3L,
                new CreateMediaUploadRequestDTO("image.png", "image/png", 100L), 1L, List.of("INSTRUCTOR")))
                .isSameAs(ticket);
        verify(mediaService).delete(4L);
    }

    @Test
    void shouldListModulesAndResolveReadyImage() {
        Course course = TestData.course(2L, TestData.user(1L, UserRole.INSTRUCTOR));
        Module module = TestData.module(3L, course);
        module.setImage(TestData.media(4L, course.getInstructor(), MediaType.MODULE_IMAGE, MediaStatus.READY));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(moduleRepository.findAllByCourseId(any(), any())).thenReturn(new PageImpl<>(List.of(module)));
        when(mediaService.getPublicUrl(module.getImage())).thenReturn("image-url");

        assertThat(service.findAllByCourse(2L, 0, 10, 1L, List.of("INSTRUCTOR")).getContent())
                .singleElement().satisfies(item -> assertThat(item.imageUrl()).isEqualTo("image-url"));
    }

    @Test
    void shouldFindModuleById() {
        Module module = TestData.module(3L,
                TestData.course(2L, TestData.user(1L, UserRole.INSTRUCTOR)));
        when(moduleRepository.findById(3L)).thenReturn(Optional.of(module));

        assertThat(service.findById(3L, 1L, List.of("INSTRUCTOR")).id()).isEqualTo(3L);
    }

    @Test
    void shouldUpdateModuleAsAdmin() {
        Module module = TestData.module(3L,
                TestData.course(2L, TestData.user(1L, UserRole.INSTRUCTOR)));
        when(moduleRepository.findById(3L)).thenReturn(Optional.of(module));
        when(moduleRepository.save(module)).thenReturn(module);

        var response = service.update(3L, new ModuleUpdateRequestDTO(" Updated ", " Description "),
                9L, List.of("ADMIN"));

        assertThat(response.title()).isEqualTo("Updated");
    }

    @Test
    void shouldDeleteModuleMediaAndLessons() {
        User instructor = TestData.user(1L, UserRole.INSTRUCTOR);
        Module module = TestData.module(3L, TestData.course(2L, instructor));
        module.setImage(TestData.media(4L, instructor, MediaType.MODULE_IMAGE, MediaStatus.READY));
        var lesson = TestData.lesson(5L, module);
        lesson.setVideo(TestData.media(6L, instructor, MediaType.LESSON_VIDEO, MediaStatus.READY));
        module.getLessons().add(lesson);
        when(moduleRepository.findById(3L)).thenReturn(Optional.of(module));

        service.deleteById(3L, 1L, List.of("INSTRUCTOR"));

        verify(mediaService).delete(4L);
        verify(mediaService).delete(6L);
        verify(moduleRepository).delete(module);
        assertThat(module.getCourse().getCourseStatus()).isEqualTo(CourseStatus.DRAFT);
    }

    @Test
    void shouldFailWhenCourseDoesNotExistDuringList() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findAllByCourse(99L, 0, 10, 1L, List.of("INSTRUCTOR")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldRepositionModules() {
        Course course = TestData.course(2L, TestData.user(1L, UserRole.INSTRUCTOR));
        Module first = TestData.module(3L, course);
        Module second = TestData.module(4L, course);
        second.setPosition(2L);
        List<Module> modules = List.of(first, second);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.existsById(2L)).thenReturn(true);
        when(moduleRepository.findAllByCourseId(2L)).thenReturn(modules);

        service.reposition(new RepositionRequestDTO(2L, List.of(4L, 3L)),
                1L, List.of("INSTRUCTOR"));

        assertThat(second.getPosition()).isEqualTo(1L);
        assertThat(first.getPosition()).isEqualTo(2L);
        verify(moduleRepository).saveAllAndFlush(modules);
    }

    @Test
    void shouldRejectInvalidModuleOrder() {
        Course course = TestData.course(2L, TestData.user(1L, UserRole.INSTRUCTOR));
        List<Module> modules = List.of(TestData.module(3L, course), TestData.module(4L, course));
        when(userRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.existsById(2L)).thenReturn(true);
        when(moduleRepository.findAllByCourseId(2L)).thenReturn(modules);

        assertThatThrownBy(() -> service.reposition(
                new RepositionRequestDTO(2L, List.of(3L, 3L)), 1L, List.of("INSTRUCTOR")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
