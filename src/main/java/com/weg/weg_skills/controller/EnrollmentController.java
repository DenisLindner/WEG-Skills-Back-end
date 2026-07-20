package com.weg.weg_skills.controller;

import com.weg.weg_skills.dto.EnrollmentRequestDTO;
import com.weg.weg_skills.dto.EnrollmentResponseDTO;
import com.weg.weg_skills.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/enrollments")
@AllArgsConstructor
@Tag(name = "Matrículas", description = "Endpoints para gerenciamento de matrículas nos cursos")
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    @PostMapping
    @Operation(summary = "Realiza a matrícula de um usuário em um curso")
    public ResponseEntity<EnrollmentResponseDTO> enroll(@Valid @RequestBody EnrollmentRequestDTO requestDTO) {
        return ResponseEntity.status(201).body(enrollmentService.enrollUser(requestDTO));
    }
}