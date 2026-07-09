package org.example.firstapi.domain.validation;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainValidationTest {

    @Test
    void requireTextReturnsNonBlankValueUnchanged() {
        // given
        String value = " value ";

        // when
        String result = DomainValidation.requireText(value, "field");

        // then
        assertThat(result).isSameAs(value);
    }

    @Test
    void requireTextRejectsNullBlankAndWhitespaceValues() {
        // when + then
        assertThatThrownBy(() -> DomainValidation.requireText(null, "email"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("email cannot be blank");

        assertThatThrownBy(() -> DomainValidation.requireText("", "password"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("password cannot be blank");

        assertThatThrownBy(() -> DomainValidation.requireText("   ", "firstName"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("firstName cannot be blank");
    }

    @Test
    void requireDateTimeReturnsValueConvertedToUtc() {
        // given
        OffsetDateTime input = OffsetDateTime.of(2026, 7, 9, 12, 30, 0, 0, ZoneOffset.ofHours(2));

        // when
        OffsetDateTime result = DomainValidation.requireDateTime(input, "createdAt");

        // then
        assertThat(result).isEqualTo(OffsetDateTime.parse("2026-07-09T10:30:00Z"));
    }

    @Test
    void requireDateTimeRejectsNullValue() {
        // when + then
        assertThatThrownBy(() -> DomainValidation.requireDateTime(null, "expiresAt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("expiresAt cannot be null");
    }
}
