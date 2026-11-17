package ua.kpi.tzyapz.staynow;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class CancellationFeeTest {

    private Availability availability;

    @BeforeEach
    void setUp() {
        availability = new Availability();
    }

    // Очікувано червоний на v1: дефект D5 (BL-60), Issue #13
    @ParameterizedTest
    @CsvSource({
            "8, 0.00",
            "7, 0.00",
            "6, 500.00",
            "3, 500.00",
            "2, 1000.00",
            "0, 1000.00"
    })
    @DisplayName("BL-60: граничні значення плати за скасування залежно від кількості днів до заїзду")
    void cancellationFeeBoundaries(int daysBeforeCheckIn, String expectedFeeValue) {
        // arrange
        BigDecimal total = new BigDecimal("1000.00");
        BigDecimal expected = new BigDecimal(expectedFeeValue);

        // act
        BigDecimal actual = availability.cancellationFee(total, daysBeforeCheckIn);

        // assert
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("BL-60: відхилення null та від'ємної суми total винятком IllegalArgumentException")
    void cancellationFeeRejectsNullAndNegativeTotal() {
        // arrange
        int daysBeforeCheckIn = 5;

        // act & assert
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> availability.cancellationFee(new BigDecimal("-0.01"), daysBeforeCheckIn)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> availability.cancellationFee(null, daysBeforeCheckIn))
        );
    }
}
