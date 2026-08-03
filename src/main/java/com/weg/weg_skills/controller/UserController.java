package com.weg.weg_skills.controller;

import com.weg.weg_skills.dto.*;
import com.weg.weg_skills.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "users")
@AllArgsConstructor
@Tag(name = "Users", description = "Endpoints for users management")
public class UserController {
    private UserService userService;

    @GetMapping(path = "/me")
    @Operation(summary = "Find current user profile")
    public ResponseEntity<UserResponseDTO> getMeProfile(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(200).body(userService.getMeProfile(jwt.getClaim("userId")));
    }

    @GetMapping(path = "/admin/instructor")
    @Operation(summary = "Find all instructors")
    public ResponseEntity<Page<UserResponseDTO>> findInstructors(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(200).body(userService.findAllInstructors(page, size, jwt.getClaim("userId"), jwt.getClaim("roles")));
    }

    @PostMapping(path = "/instructor")
    @Operation(summary = "Create instructor")
    public ResponseEntity<InstructorResponseDTO> createInstructor(@RequestBody @Valid RegisterInstructorRequestDTO dto, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(201).body(userService.createInstructor(dto, jwt.getClaim("userId")));
    }

    @PostMapping(path = "/me/images/upload")
    @Operation(summary = "Generates a ticket for uploading the user profile image")
    public ResponseEntity<UploadTicketResponseDTO> uploadImage(@RequestBody @Valid CreateMediaUploadRequestDTO dto, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(201).body(userService.uploadImage(jwt.getClaim("userId"), dto));
    }

    @PatchMapping(path = "/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<UserResponseDTO> update(@RequestBody @Valid UserUpdateRequestDTO dto, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(200).body(userService.update(jwt.getClaim("userId"), dto));
    }

    @PatchMapping(path = "/me/password")
    @Operation(summary = "Change password of the current user")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid PasswordChangeRequestDTO dto, @AuthenticationPrincipal Jwt jwt) {
        userService.changePassword(jwt.getClaim("userId"), dto);
        return ResponseEntity.status(204).build();
    }

    @DeleteMapping(path = "/me")
    @Operation(summary = "Delete current user")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt) {
        userService.deleteById(jwt.getClaim("userId"));
        return ResponseEntity.status(204).build();
    }

    @DeleteMapping(path = "/instructor/{id}")
    @Operation(summary = "Delete instructor by id")
    public ResponseEntity<Void> deleteInstructor(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        userService.deleteInstructorById(id, jwt.getClaim("userId"), jwt.getClaim("roles"));
        return ResponseEntity.status(204).build();
    }
}
