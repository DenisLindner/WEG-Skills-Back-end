package com.weg.weg_skills.service;

import com.weg.weg_skills.TestData;
import com.weg.weg_skills.dto.CreateMediaUploadRequestDTO;
import com.weg.weg_skills.dto.PasswordChangeRequestDTO;
import com.weg.weg_skills.dto.RegisterInstructorRequestDTO;
import com.weg.weg_skills.dto.UploadTicketResponseDTO;
import com.weg.weg_skills.dto.UserUpdateRequestDTO;
import com.weg.weg_skills.enums.MediaStatus;
import com.weg.weg_skills.enums.MediaType;
import com.weg.weg_skills.enums.CourseStatus;
import com.weg.weg_skills.enums.UserRole;
import com.weg.weg_skills.exceptions.DuplicateResourceException;
import com.weg.weg_skills.exceptions.ForbiddenException;
import com.weg.weg_skills.exceptions.ResourceNotFoundException;
import com.weg.weg_skills.exceptions.UnauthorizedException;
import com.weg.weg_skills.exceptions.UserHasCoursesException;
import com.weg.weg_skills.mapper.UserMapper;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Media;
import com.weg.weg_skills.model.User;
import com.weg.weg_skills.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock MediaService mediaService;
    @Mock PasswordEncoder passwordEncoder;

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(userRepository, new UserMapper(), mediaService, passwordEncoder);
    }

    @Test
    void shouldCreateInstructorWhenCurrentUserIsAdmin() {
        User admin = TestData.user(1L, UserRole.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createInstructor(
                new RegisterInstructorRequestDTO("Instructor", " INSTRUCTOR@EXAMPLE.COM "), 1L);

        assertThat(response.email()).isEqualTo("instructor@example.com");
        assertThat(response.temporaryPassword()).hasSize(8);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(UserRole.INSTRUCTOR);
        verify(passwordEncoder).encode(response.temporaryPassword());
    }

    @Test
    void shouldRejectInstructorCreationByNonAdminOrDuplicatedEmail() {
        User student = TestData.user(1L, UserRole.STUDENT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> service.createInstructor(
                new RegisterInstructorRequestDTO("Instructor", "instructor@example.com"), 1L))
                .isInstanceOf(ForbiddenException.class);

        student.setRole(UserRole.ADMIN);
        when(userRepository.existsByEmailIgnoreCase("instructor@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.createInstructor(
                new RegisterInstructorRequestDTO("Instructor", "instructor@example.com"), 1L))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void shouldReturnProfileWithReadyPicture() {
        User user = TestData.user(1L, UserRole.STUDENT);
        Media image = TestData.media(2L, user, MediaType.USER_IMAGE, MediaStatus.READY);
        user.setImage(image);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(mediaService.getPublicUrl(user.getImage())).thenReturn("picture-url");

        var response = service.getMeProfile(1L);

        assertThat(response.pictureUrl()).isEqualTo("picture-url");
    }

    @Test
    void shouldAllowOnlyAdminToListInstructors() {
        User instructor = TestData.user(2L, UserRole.INSTRUCTOR);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findAllByRole(eq(UserRole.INSTRUCTOR), any()))
                .thenReturn(new PageImpl<>(List.of(instructor), PageRequest.of(0, 10), 1));

        var response = service.findAllInstructors(0, 10, 1L, List.of("ADMIN"));

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().id()).isEqualTo(2L);

        assertThatThrownBy(() -> service.findAllInstructors(0, 10, 1L, List.of("INSTRUCTOR")))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shouldReplacePendingProfileImage() {
        User user = TestData.user(1L, UserRole.STUDENT);
        Media previous = TestData.media(2L, user, MediaType.USER_IMAGE, MediaStatus.PENDING_UPLOAD);
        Media next = TestData.media(3L, user, MediaType.USER_IMAGE, MediaStatus.PENDING_UPLOAD);
        user.setPendingImage(previous);
        UploadTicketResponseDTO ticket = new UploadTicketResponseDTO(3L, "url", "key", java.util.Map.of(), Instant.now());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(mediaService.createUserImageUpload(any(), any())).thenReturn(new CreatedMediaUpload(ticket, next));

        assertThat(service.uploadImage(1L, new CreateMediaUploadRequestDTO("file.png", "image/png", 100L)))
                .isSameAs(ticket);
        assertThat(user.getPendingImage()).isSameAs(next);
        verify(userRepository).saveAndFlush(user);
        verify(mediaService).delete(2L);
    }

    @Test
    void shouldUpdateOnlyProvidedProfileFields() {
        User user = TestData.user(1L, UserRole.STUDENT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        LocalDate birthday = LocalDate.of(2000, 1, 1);

        var response = service.update(1L, new UserUpdateRequestDTO(
                " New Name ", " NEW@EXAMPLE.COM ", birthday, " 999999999 ",
                " Joinville ", " SC ", " Brazil "));

        assertThat(response.name()).isEqualTo("New Name");
        assertThat(response.email()).isEqualTo("new@example.com");
        assertThat(user.getBirthday()).isEqualTo(birthday);
        assertThat(user.getPhone()).isEqualTo("999999999");
        assertThat(user.getCity()).isEqualTo("Joinville");
    }

    @Test
    void shouldRejectDuplicatedEmailOnUpdate() {
        User user = TestData.user(1L, UserRole.STUDENT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailIgnoreCase("other@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, new UserUpdateRequestDTO(
                null, "other@example.com", null, null, null, null, null)))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldChangePasswordWhenCurrentPasswordMatches() {
        User user = TestData.user(1L, UserRole.STUDENT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current", "encoded-password")).thenReturn(true);
        when(passwordEncoder.encode("NewStrong1!")).thenReturn("new-encoded");

        service.changePassword(1L, new PasswordChangeRequestDTO("NewStrong1!", "current"));

        assertThat(user.getPassword()).isEqualTo("new-encoded");
        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectWrongCurrentPassword() {
        User user = TestData.user(1L, UserRole.STUDENT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword(1L,
                new PasswordChangeRequestDTO("NewStrong1!", "wrong")))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void shouldDeleteUserAndItsMedia() {
        User user = TestData.user(1L, UserRole.STUDENT);
        user.setImage(TestData.media(2L, user, MediaType.USER_IMAGE, MediaStatus.READY));
        user.setPendingImage(TestData.media(3L, user, MediaType.USER_IMAGE, MediaStatus.PENDING_UPLOAD));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        service.deleteById(1L);

        verify(mediaService).delete(2L);
        verify(mediaService).delete(3L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void shouldNotDeleteUserWithCourses() {
        User user = TestData.user(1L, UserRole.INSTRUCTOR);
        user.getCourses().add(new Course("Course", "Description", user, CourseStatus.PUBLISHED));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.deleteById(1L)).isInstanceOf(UserHasCoursesException.class);
    }

    @Test
    void shouldNotDeleteInstructorMediaWhenInstructorHasCourses() {
        User instructor = TestData.user(2L, UserRole.INSTRUCTOR);
        instructor.setImage(TestData.media(3L, instructor, MediaType.USER_IMAGE, MediaStatus.READY));
        instructor.getCourses().add(new Course("Course", "Description", instructor, CourseStatus.PUBLISHED));
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(instructor));

        assertThatThrownBy(() -> service.deleteInstructorById(2L, 1L, List.of("ADMIN")))
                .isInstanceOf(UserHasCoursesException.class);

        verify(mediaService, never()).delete(any());
        verify(userRepository, never()).deleteById(2L);
    }

    @Test
    void shouldFailWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMeProfile(99L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
