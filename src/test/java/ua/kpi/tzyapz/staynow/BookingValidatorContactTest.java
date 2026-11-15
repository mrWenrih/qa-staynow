package ua.kpi.tzyapz.staynow;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class BookingValidatorContactTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 1);
    private static final LocalDate CHECK_IN = LocalDate.of(2026, 6, 2);
    private static final LocalDate CHECK_OUT = LocalDate.of(2026, 6, 5);
    private static final int GUESTS = 2;
    private static final int CAPACITY = 3;

    private BookingValidator validator;

    @BeforeEach
    void setUp() {
        validator = new BookingValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "name@sub.domain.ua"
    })
    @DisplayName("BL-20: коректні email приймаються")
    void validEmailsAccepted(String email) {
        // arrange

        // act
        boolean actual = validator.isValidEmail(email);

        // assert
        assertTrue(actual);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            "@domain.com",
            "user@",
            "user@domain"
    })
    @DisplayName("BL-20: некоректні email відхиляються")
    void invalidEmailFormatsRejected(String email) {
        // arrange

        // act
        boolean actual = validator.isValidEmail(email);

        // assert
        assertFalse(actual);
    }

    @ParameterizedTest
    @CsvSource({
            "+38050123456, false",
            "+380501234567, true",
            "+3805012345678, false"
    })
    @DisplayName("BL-20: телефон містить рівно 9 цифр після префікса +380")
    void phoneDigitsCountBoundaries(String phone, boolean expected) {
        // arrange

        // act
        boolean actual = validator.isValidPhone(phone);

        // assert
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            "380501234567",
            "+38050123456a"
    })
    @DisplayName("BL-20: телефон без префікса +380 або з нецифровими символами відхиляється")
    void invalidPhonePrefixAndChars(String phone) {
        // arrange

        // act
        boolean actual = validator.isValidPhone(phone);

        // assert
        assertFalse(actual);
    }

    @Test
    @DisplayName("BL-20: перевантаження validate додає помилки INVALID_EMAIL та INVALID_PHONE")
    void validateOverloadCollectsEmailPhoneErrors() {
        // arrange
        String email = "bad";
        String phone = "123";

        // act
        List<BookingError> errors = validator.validate(CHECK_IN, CHECK_OUT, GUESTS, CAPACITY,
                TODAY, email, phone);

        // assert
        assertAll(
                () -> assertTrue(errors.contains(BookingError.INVALID_EMAIL),
                        "Очікувалась помилка INVALID_EMAIL, отримано: " + errors),
                () -> assertTrue(errors.contains(BookingError.INVALID_PHONE),
                        "Очікувалась помилка INVALID_PHONE, отримано: " + errors)
        );
    }
}
