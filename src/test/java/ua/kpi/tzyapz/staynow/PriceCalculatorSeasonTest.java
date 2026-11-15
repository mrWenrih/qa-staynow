package ua.kpi.tzyapz.staynow;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriceCalculatorSeasonTest {

    private PriceCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PriceCalculator();
    }

    @ParameterizedTest
    @CsvSource({
            "2026, 5, 31, 1.00",
            "2026, 6, 1, 1.30",
            "2026, 8, 31, 1.30",
            "2026, 9, 1, 1.00"
    })
    @DisplayName("BL-40: межі високого сезону влітку (01.06–31.08, коефіцієнт 1.30)")
    void summerHighSeasonBoundaries(int year, int month, int day, String expectedMultiplier) {
        // arrange
        LocalDate date = LocalDate.of(year, month, day);
        BigDecimal expected = new BigDecimal(expectedMultiplier);

        // act
        BigDecimal actual = calculator.seasonFactor(date);

        // assert
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({
            "2026, 12, 19, 1.00",
            "2026, 12, 20, 1.30",
            "2026, 12, 31, 1.30",
            "2026, 1, 1, 1.30",
            "2026, 1, 10, 1.30",
            "2026, 1, 11, 1.00"
    })
    @DisplayName("BL-40: межі високого сезону взимку (20.12–10.01, коефіцієнт 1.30)")
    void winterHighSeasonBoundaries(int year, int month, int day, String expectedMultiplier) {
        // arrange
        LocalDate date = LocalDate.of(year, month, day);
        BigDecimal expected = new BigDecimal(expectedMultiplier);

        // act
        BigDecimal actual = calculator.seasonFactor(date);

        // assert
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({
            "2026, 1, 31, 1.00",
            "2026, 2, 1, 0.80",
            "2026, 3, 31, 0.80",
            "2026, 4, 1, 1.00",
            "2026, 10, 31, 1.00",
            "2026, 11, 1, 0.80",
            "2026, 11, 30, 0.80",
            "2026, 12, 1, 1.00"
    })
    @DisplayName("BL-40: межі низького сезону (лютий, березень, листопад, коефіцієнт 0.80)")
    void lowSeasonBoundaries(int year, int month, int day, String expectedMultiplier) {
        // arrange
        LocalDate date = LocalDate.of(year, month, day);
        BigDecimal expected = new BigDecimal(expectedMultiplier);

        // act
        BigDecimal actual = calculator.seasonFactor(date);

        // assert
        assertEquals(expected, actual);
    }
}
