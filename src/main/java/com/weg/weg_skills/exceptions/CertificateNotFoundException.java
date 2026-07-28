package com.weg.weg_skills.exceptions;

public class CertificateNotFoundException extends RuntimeException {
    public CertificateNotFoundException(String code) {
        super("Certificate with code: "+code+", not found");
    }
}
