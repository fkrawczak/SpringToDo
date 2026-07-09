package org.example.firstapi.domain.validation;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class DomainValidation {

    private DomainValidation() {
    }

    public static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return value;
    }

    public static OffsetDateTime requireDateTime(OffsetDateTime value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
        return value.withOffsetSameInstant(ZoneOffset.UTC);
    }
}
