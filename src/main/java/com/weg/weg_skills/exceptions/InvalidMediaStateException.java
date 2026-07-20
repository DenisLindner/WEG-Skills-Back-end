package com.weg.weg_skills.exceptions;

import com.weg.weg_skills.enums.MediaStatus;

public class InvalidMediaStateException extends RuntimeException {
    public InvalidMediaStateException(MediaStatus status) {
        super("Invalid media state: "+status);
    }
}
