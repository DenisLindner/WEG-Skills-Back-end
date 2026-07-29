package com.weg.weg_skills.controller;

import com.weg.weg_skills.dto.AuthResponseDTO;
import com.weg.weg_skills.dto.LoginRequestDTO;
import com.weg.weg_skills.dto.RegisterRequestDTO;
import com.weg.weg_skills.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "auth")
@AllArgsConstructor
@Tag(name = "Auth", description = "Endpoints for auth management")
public class AuthController {
    private AuthService authService;

    @PostMapping(path = "/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody @Valid RegisterRequestDTO dto) {
        return ResponseEntity.status(201).body(authService.register(dto));
    }

    @PostMapping(path = "/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto) {
        return ResponseEntity.status(200).body(authService.login(dto));
    }
}
