package com.weg.weg_skills.service;

import com.weg.weg_skills.dto.*;
import com.weg.weg_skills.enums.CourseStatus;
import com.weg.weg_skills.enums.UserRole;
import com.weg.weg_skills.exceptions.DuplicateResourceException;
import com.weg.weg_skills.exceptions.EnrollmentNotFoundException;
import com.weg.weg_skills.exceptions.ForbiddenException;
import com.weg.weg_skills.exceptions.ResourceNotFoundException;
import com.weg.weg_skills.mapper.CertificateMapper;
import com.weg.weg_skills.mapper.CourseMapper;
import com.weg.weg_skills.mapper.LessonProgressMapper;
import com.weg.weg_skills.model.*;
import com.weg.weg_skills.projection.CourseDetailsProjection;
import com.weg.weg_skills.projection.CourseWithRatingProjection;
import com.weg.weg_skills.projection.ProgressProjection;
import com.weg.weg_skills.repository.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class CourseService {
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private CourseRepository courseRepository;
    private CourseMapper courseMapper;
    private MediaService mediaService;
    private UserRepository userRepository;
    private LessonProgressRepository lessonProgressRepository;
    private LessonProgressMapper lessonProgressMapper;
    private EnrollmentRepository enrollmentRepository;
    private CertificateRepository certificateRepository;
    private CertificateMapper certificateMapper;

    @Transactional
    @CacheEvict(cacheNames = "topCourses", allEntries = true)
    public CourseResponseDTO create(CourseCreateRequestDTO dto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (user.getRole() != UserRole.INSTRUCTOR && user.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException();
        }

        String title = normalizeString(dto.title());
        if (courseRepository.existsByTitleIgnoreCase(title)) {
            throw new DuplicateResourceException("Course", "title", title);
        }

        String description = dto.description() != null ? normalizeString(dto.description()) : null;
        Course course = courseMapper.toEntity(title, description, user);

        course = courseRepository.save(course);

        log.atInfo().addKeyValue("title", title).addKeyValue("userId", userId).log("Course created");

        return courseMapper.toResponse(course, null);
    }

    @Transactional
    public UploadTicketResponseDTO uploadImage(Long id, CreateMediaUploadRequestDTO dto, Long userId, List<String> roles) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        Course course = courseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Course", id));

        if (!course.getInstructor().getId().equals(userId)) {
            if (!roles.contains(String.valueOf(UserRole.ADMIN))){
                throw new ForbiddenException();
            }
        }

        CreatedMediaUpload createdMedia = mediaService.createCourseImageUpload(course.getId(), userId, dto);

        Media previousImage = course.getPendingImage();

        course.setPendingImage(createdMedia.media());
        courseRepository.saveAndFlush(course);

        if (previousImage != null) {
            mediaService.delete(previousImage.getId());
        }

        log.atInfo().addKeyValue("courseId", id).addKeyValue("userId", userId).log("Upload course image ticket created");

        return createdMedia.ticket();
    }

    @Transactional(readOnly = true)
    public Page<CourseResponseDTO> findAll(int page, int size, Long userId) {
        validatePagination(page, size);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by("createdAt").descending()
        );

        Page<Course> courses = courseRepository.findAllByInstructorId(userId, pageable);

        return courses.map(c ->
            courseMapper.toResponse(c, c.getImage() != null && c.getImage().isReady() ? mediaService.getPublicUrl(c.getImage()) : null)
        );
    }

    @Transactional(readOnly = true)
    public Page<CourseResponseDTO> findAllAdmin(int page, int size, Long userId) {
        validatePagination(page, size);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by("createdAt").descending()
        );

        Page<Course> courses = courseRepository.findAll(pageable);

        return courses.map(c ->
            courseMapper.toResponse(c, c.getImage() != null && c.getImage().isReady() ? mediaService.getPublicUrl(c.getImage()) : null)
        );
    }

    @Transactional(readOnly = true)
    public Page<CourseWithRatingResponseDTO> findAllPublished(int page, int size) {
        validatePagination(page, size);

        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by("createdAt").descending()
        );

        Page<CourseWithRatingProjection> courses = courseRepository.findAllByCourseStatus(CourseStatus.PUBLISHED, pageable);

        return courses.map(c ->
            courseMapper.toResponseProjection(c, c.getImage() != null && c.getImage().isReady() ? mediaService.getPublicUrl(c.getImage()) : null)
        );
    }

    @Transactional(readOnly = true)
    public Page<CourseResponseDTO> findAllByTitle(String title, int page, int size, Long userId) {
        validateTitle(title);

        validatePagination(page, size);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by("createdAt").descending()
        );

        Page<Course> courses = courseRepository.findAllByInstructorIdAndTitleContainingIgnoreCase(userId, title.trim(), pageable);

        return courses.map(c ->
            courseMapper.toResponse(c, c.getImage() != null && c.getImage().isReady() ? mediaService.getPublicUrl(c.getImage()) : null)
        );
    }

    @Transactional(readOnly = true)
    public Page<CourseWithRatingResponseDTO> findAllByTitlePublished(String title, int page, int size) {
        validateTitle(title);

        validatePagination(page, size);

        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by("createdAt").descending()
        );

        Page<CourseWithRatingProjection> courses = courseRepository.findAllByTitleContainingIgnoreCaseAndCourseStatus(title.trim(), CourseStatus.PUBLISHED, pageable);

        return courses.map(c ->
            courseMapper.toResponseProjection(c, c.getImage() != null && c.getImage().isReady() ? mediaService.getPublicUrl(c.getImage()) : null)
        );
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "topCourses")
    public List<CourseWithRatingResponseDTO> findMostEnrollments() {
        Pageable pageable = PageRequest.of(
                0, 3,
                Sort.by("createdAt").descending()
        );

        List<CourseWithRatingProjection> courses = courseRepository.findMostEnrollmentsCourses(CourseStatus.PUBLISHED, pageable);

        return courses.stream().map(c ->
            courseMapper.toResponseProjection(c, c.getImage() != null && c.getImage().isReady() ? mediaService.getPublicUrl(c.getImage()) : null)
        ).toList();
    }

    @Transactional(readOnly = true)
    public CourseWithRatingResponseDTO findById(Long id, Long userId, List<String> roles) {
        CourseDetailsProjection projection = courseRepository.findByIdWithRating(id).orElseThrow(() -> new ResourceNotFoundException("Course", id));

        if (projection.getCourseStatus() != CourseStatus.PUBLISHED) {
            if (!projection.getInstructorId().equals(userId)) {
                if (!roles.contains(String.valueOf(UserRole.ADMIN))){
                    throw new ForbiddenException();
                }
            }
        }

        return courseMapper.toResponseProjection(projection, projection.getImage() != null && projection.getImage().isReady() ? mediaService.getPublicUrl(projection.getImage()) : null);
    }

    @Transactional(readOnly = true)
    public CourseProgressResponseDTO findProgressByUser(Long courseId, Long userId, List<String> roles) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        Course course = courseRepository.findById(courseId).orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        if (course.getCourseStatus() != CourseStatus.PUBLISHED) {
            if (!course.getInstructor().getId().equals(userId)) {
                if (!roles.contains(String.valueOf(UserRole.ADMIN))){
                    throw new ForbiddenException();
                }
            }
        }

        if (!enrollmentRepository.existsByCourseIdAndUserId(courseId, userId)) {
            throw new EnrollmentNotFoundException(courseId, userId);
        }

        ProgressProjection projection = courseRepository.findProgressByUserId(userId, courseId).orElseThrow(() -> new ResourceNotFoundException("Course progress", courseId));

        List<LessonProgress> lessonProgresses = lessonProgressRepository.findAllByEnrollmentUserIdAndLessonModuleCourseId(userId, courseId);

        return courseMapper.toResponseProgress(projection, lessonProgresses.stream().map(l -> lessonProgressMapper.toResponse(l)).toList());
    }

    @Transactional
    @CacheEvict(cacheNames = "topCourses", allEntries = true)
    public CourseResponseDTO update(Long id, CourseUpdateRequestDTO dto, Long userId, List<String> roles) {
        Course course = courseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Course", id));

        if (!course.getInstructor().getId().equals(userId)) {
            if (!roles.contains(String.valueOf(UserRole.ADMIN))){
                throw new ForbiddenException();
            }
        }

        if (dto.title() != null) {
            String title = normalizeString(dto.title());
            validateTitle(title);
            if (!title.equalsIgnoreCase(course.getTitle()) && courseRepository.existsByTitleIgnoreCase(title)) {
                throw new DuplicateResourceException("Course", "title", title);
            }
            course.setTitle(title);
        }

        if (dto.description() != null) {
            String description = normalizeString(dto.description());
            course.setDescription(description);
        }

        course = courseRepository.save(course);

        log.atInfo().addKeyValue("courseId", id).addKeyValue("userId", userId).log("Course updated");

        return courseMapper.toResponse(course, course.getImage() != null && course.getImage().isReady() ? mediaService.getPublicUrl(course.getImage()) : null);
    }

    @Transactional
    @CacheEvict(cacheNames = "topCourses", allEntries = true)
    public void deleteById(Long id, Long userId, List<String> roles) {
        Course course = courseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Course", id));

        if (!course.getInstructor().getId().equals(userId)) {
            if (!roles.contains(String.valueOf(UserRole.ADMIN))){
                throw new ForbiddenException();
            }
        }

        if (course.getImage() != null) {
            mediaService.delete(course.getImage().getId());
        }

        if (course.getPendingImage() != null) {
            mediaService.delete(course.getPendingImage().getId());
        }

        course.getModules().forEach(m -> {
            m.getLessons().forEach(l -> {
                if (l.getVideo() != null && !l.getVideo().isDeleted()) mediaService.delete(l.getVideo().getId());
                if (l.getPendingVideo() != null && !l.getPendingVideo().isDeleted()) mediaService.delete(l.getPendingVideo().getId());
            });
            if (m.getImage() != null && !m.getImage().isDeleted()) mediaService.delete(m.getImage().getId());
            if (m.getPendingImage() != null && !m.getPendingImage().isDeleted()) mediaService.delete(m.getPendingImage().getId());
        });

        courseRepository.delete(course);

        log.atInfo().addKeyValue("courseId", id).addKeyValue("userId", userId).log("Course deleted");
    }

    @Transactional
    public CertificateResponseDTO createCertificate(Long courseId, Long userId, List<String> roles) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Course course = courseRepository.findById(courseId).orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        if (course.getCourseStatus() != CourseStatus.PUBLISHED) {
            if (!course.getInstructor().getId().equals(userId)) {
                if (!roles.contains(String.valueOf(UserRole.ADMIN))){
                    throw new ForbiddenException();
                }
            }
        }

        if (!enrollmentRepository.existsByCourseIdAndUserId(courseId, userId)) {
            throw new EnrollmentNotFoundException(courseId, userId);
        }

        Certificate certificate = certificateRepository.findByCourseIdAndUserId(courseId, userId);

        if (certificate != null) {
            return certificateMapper.toResponse(certificate);
        }

        ProgressProjection projection = courseRepository.findProgressByUserId(user.getId(), course.getId()).orElseThrow(() -> new ResourceNotFoundException("Course progress", courseId));

        if (projection.getTotalLessons() <= 0) {
            throw new IllegalStateException("Course doesn't have any lessons yet");
        }

        if (!projection.getTotalLessons().equals(projection.getCompletedLessons())) {
            throw new IllegalStateException("User hasn't finished the course yet");
        }

        String code = generateCertificateCode();

        certificate = certificateMapper.toEntity(code, user.getName(), course.getTitle(), projection.getTotalLessons(), course, user);

        certificate = certificateRepository.save(certificate);

        return certificateMapper.toResponse(certificate);
    }

    @Transactional
    @CacheEvict(cacheNames = "topCourses", allEntries = true)
    public CourseResponseDTO publish(Long id, Long userId, List<String> roles) {
        Course course = courseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Course", id));

        if (!course.getInstructor().getId().equals(userId)) {
            if (!roles.contains(String.valueOf(UserRole.ADMIN))){
                throw new ForbiddenException();
            }
        }

        if (course.getImage() == null) {
            throw new IllegalStateException("Course doesn't have an image yet");
        }

        Long modules = moduleRepository.countByCourseId(course.getId());
        if (modules < 1) {
            throw new IllegalStateException("Course doesn't have any modules yet");
        }

        Long lessons = lessonRepository.countByModuleCourseId(course.getId());
        Long lessonsWithVideo = lessonRepository.countByModuleCourseIdAndVideoIsNotNull(course.getId());
        if (lessonsWithVideo < 1) {
            throw new IllegalStateException("Course doesn't have any lessons yet");
        }
        if (!lessonsWithVideo.equals(lessons)) {
            throw new IllegalStateException("Some lessons doesn't have an video yet");
        }

        course.markIsPublished();

        course = courseRepository.save(course);

        return courseMapper.toResponse(course, course.getImage() != null && course.getImage().isReady() ? mediaService.getPublicUrl(course.getImage()) : null);
    }

    private String normalizeString(String value) {
        return value.trim();
    }

    private String generateCertificateCode() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private void validateTitle(String title) {
        if (title == null || title.trim().length() < 3) {
            throw new IllegalArgumentException("Title must have a non-null value and be equal to or longer than 3 characters");
        }
    }

    private void validatePagination(int page, int size) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new IllegalArgumentException("Pagination must have page >= 0, size > 0, and size <= 100");
        }
    }
}
