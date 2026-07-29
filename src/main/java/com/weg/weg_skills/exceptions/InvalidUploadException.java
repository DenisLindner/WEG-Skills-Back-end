package com.weg.weg_skills.exceptions;

import com.weg.weg_skills.enums.InvalidUpload;

public class InvalidUploadException extends RuntimeException {
    public InvalidUploadException(InvalidUpload cause) {
        super(
                switch (cause) {
                    case UNSUPPORTED_CONTENT_TYPE -> "Unsupported content type";
                    case FILE_TOO_LARGE -> "File too large";
                    case INVALID_FILE_SIZE -> "Invalid file size";
                }
        );
    }
}
