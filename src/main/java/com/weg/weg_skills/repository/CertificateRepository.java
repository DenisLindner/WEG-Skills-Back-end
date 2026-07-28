package com.weg.weg_skills.repository;

import com.weg.weg_skills.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, String> {
    Optional<Certificate> findByCode(String code);
    Certificate findByCourseIdAndUserId(Long courseId, Long userId);
}
