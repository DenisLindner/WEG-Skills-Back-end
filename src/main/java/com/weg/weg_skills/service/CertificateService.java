package com.weg.weg_skills.service;

import com.weg.weg_skills.dto.CertificateResponseDTO;
import com.weg.weg_skills.exceptions.CertificateNotFoundException;
import com.weg.weg_skills.mapper.CertificateMapper;
import com.weg.weg_skills.model.Certificate;
import com.weg.weg_skills.repository.CertificateRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CertificateService {
    private CertificateRepository certificateRepository;
    private CertificateMapper certificateMapper;

    @Transactional(readOnly = true)
    public CertificateResponseDTO validateCertificate(String code) {
        Certificate certificate = certificateRepository.findByCode(code).orElseThrow(() -> new CertificateNotFoundException(code));

        return certificateMapper.toResponse(certificate);
    }
}
