package com.weg.weg_skills.controller;

import com.weg.weg_skills.dto.CertificateResponseDTO;
import com.weg.weg_skills.service.CertificateService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/certificates")
@AllArgsConstructor
public class CertificateController {
    private CertificateService certificateService;

    @GetMapping("/validate/{code}")
    public ResponseEntity<CertificateResponseDTO> validate(@PathVariable String code) {
        return ResponseEntity.status(200).body(certificateService.validateCertificate(code));
    }
}
