package com.weg.weg_skills.enums;

public enum UserRole {
    STUDENT,
    INSTRUCTOR,
    ADMIN;

    public String asAuthority() {
        return "ROLE_" + name();
    }
}
