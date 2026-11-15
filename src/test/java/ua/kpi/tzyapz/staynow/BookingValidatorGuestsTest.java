package ua.kpi.tzyapz.staynow;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class BookingValidatorGuestsTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 1);
    private static final int CAPACITY = 3;

    private BookingValidator validator;

    @BeforeEach
    void setUp() {
        validator = new BookingValidator();
    }

    @ParameterizedTest
    @CsvSource({
            "0, true",
            "1, false",
            "3, false",
            "4, true"
    })
    @DisplayName("BL-10: граничні значення кількості гостей відносно місткості номера (0, 1, 3, 4)")
    void guestsCapacityBoundaries(int guests, boolean expectError) {
        // arrange
        LocalDate checkIn = LocalDate.of(2026, 6, 2);
        LocalDate checkOut = LocalDate.of(2026, 6, 5);

        // act
        List<BookingError> errors = validator.validate(checkIn, checkOut, guests, CAPACITY, TODAY);

        // assert
        if (expectError) {
            assertAll(
                    () -> assertFalse(errors.isEmpty()),
                    () -> assertTrue(errors.contains(BookingError.GUESTS_OUT_OF_RANGE))
            );
        } else {
            assertTrue(errors.isEmpty());
        }
    }
}
