package com.weg.weg_skills.exceptions;

public class MediaNotReadyException extends RuntimeException {
    public MediaNotReadyException() {
        super("Media not ready");
    }
}
