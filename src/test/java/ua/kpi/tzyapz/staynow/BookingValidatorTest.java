package ua.kpi.tzyapz.staynow;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class BookingValidatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 1);
    private static final int GUESTS = 2;
    private static final int CAPACITY = 3;

    private BookingValidator validator;

    @BeforeEach
    void setUp() {
        validator = new BookingValidator();
    }

    @Test
    @DisplayName("BL-10: коректне бронювання не повертає помилок")
    void validBookingReturnsNoErrors() {
        // arrange
        LocalDate checkIn = LocalDate.of(2026, 6, 2);
        LocalDate checkOut = LocalDate.of(2026, 6, 5);

        // act
        List<BookingError> errors = validator.validate(checkIn, checkOut, GUESTS, CAPACITY, TODAY);

        // assert
        assertTrue(errors.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            "2026, 6, 5, 2026, 6, 5",
            "2026, 6, 5, 2026, 6, 4"
    })
    @DisplayName("BL-10: виїзд не пізніше заїзду повертає помилку CHECK_OUT_NOT_AFTER_CHECK_IN")
    void checkOutNotAfterCheckInFails(int checkInYear, int checkInMonth, int checkInDay,
                                      int checkOutYear, int checkOutMonth, int checkOutDay) {
        // arrange
        LocalDate checkIn = LocalDate.of(checkInYear, checkInMonth, checkInDay);
        LocalDate checkOut = LocalDate.of(checkOutYear, checkOutMonth, checkOutDay);

        // act
        List<BookingError> errors = validator.validate(checkIn, checkOut, GUESTS, CAPACITY, TODAY);

        // assert
        assertAll(
                () -> assertFalse(errors.isEmpty()),
                () -> assertTrue(errors.contains(BookingError.CHECK_OUT_NOT_AFTER_CHECK_IN))
        );
    }

    // Очікувано червоний на v1: дефект D1 (BL-10), Issue #___
    @ParameterizedTest
    @CsvSource({
            "2026, 6, 3, false",
            "2026, 7, 2, false",
            "2026, 7, 3, true"
    })
    @DisplayName("BL-10: граничні значення кількості ночей (1, 30, 31)")
    void nightsBoundaries(int checkOutYear, int checkOutMonth, int checkOutDay, boolean expectError) {
        // arrange
        LocalDate checkIn = LocalDate.of(2026, 6, 2);
        LocalDate checkOut = LocalDate.of(checkOutYear, checkOutMonth, checkOutDay);

        // act
        List<BookingError> errors = validator.validate(checkIn, checkOut, GUESTS, CAPACITY, TODAY);

        // assert
        if (expectError) {
            assertAll(
                    () -> assertFalse(errors.isEmpty()),
                    () -> assertTrue(errors.contains(BookingError.NIGHTS_OUT_OF_RANGE))
            );
        } else {
            assertTrue(errors.isEmpty());
        }
    }

    // Очікувано червоний на v1: дефект D2 (BL-10), Issue #___
    @ParameterizedTest
    @CsvSource({
            "2026, 5, 31, true",
            "2026, 6, 1, false",
            "2026, 6, 2, false"
    })
    @DisplayName("BL-10: граничні значення дати заїзду відносно поточної дати")
    void checkInRelativeToTodayBoundaries(int checkInYear, int checkInMonth, int checkInDay, boolean expectError) {
        // arrange
        LocalDate checkIn = LocalDate.of(checkInYear, checkInMonth, checkInDay);
        LocalDate checkOut = checkIn.plusDays(2);

        // act
        List<BookingError> errors = validator.validate(checkIn, checkOut, GUESTS, CAPACITY, TODAY);

        // assert
        if (expectError) {
            assertAll(
                    () -> assertFalse(errors.isEmpty()),
                    () -> assertTrue(errors.contains(BookingError.CHECK_IN_IN_PAST))
            );
        } else {
            assertTrue(errors.isEmpty());
        }
    }
}