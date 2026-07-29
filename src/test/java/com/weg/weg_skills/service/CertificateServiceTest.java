package com.weg.weg_skills.service;

import com.weg.weg_skills.TestData;
import com.weg.weg_skills.enums.UserRole;
import com.weg.weg_skills.exceptions.CertificateNotFoundException;
import com.weg.weg_skills.mapper.CertificateMapper;
import com.weg.weg_skills.model.Certificate;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.User;
import com.weg.weg_skills.repository.CertificateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificateServiceTest {

    @Mock CertificateRepository certificateRepository;

    private CertificateService service;

    @BeforeEach
    void setUp() {
        service = new CertificateService(certificateRepository, new CertificateMapper());
    }

    @Test
    void shouldValidateCertificate() {
        User student = TestData.user(1L, UserRole.STUDENT);
        Course course = TestData.course(2L, TestData.user(3L, UserRole.INSTRUCTOR));
        Certificate certificate = new Certificate("valid-code", student.getName(), course.getTitle(), 2L, student, course);
        certificate.setCompletedAt(Instant.now());
        when(certificateRepository.findByCode("valid-code")).thenReturn(Optional.of(certificate));

        var response = service.validateCertificate("valid-code");

        assertThat(response.code()).isEqualTo("valid-code");
        assertThat(response.studentName()).isEqualTo("Test User");
        assertThat(response.courseTitle()).isEqualTo("Java Basics");
        assertThat(response.totalLessons()).isEqualTo(2L);
    }

    @Test
    void shouldRejectInvalidCertificate() {
        when(certificateRepository.findByCode("invalid-code")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validateCertificate("invalid-code"))
                .isInstanceOf(CertificateNotFoundException.class);
    }
}
