package ua.kpi.tzyapz.staynow;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class PriceCalculatorNightTest {

    private PriceCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PriceCalculator();
    }

    // Очікувано червоний на v1: дефекти D3 (BL-30), Issue #12 і D4 (BL-50), Issue #7
    @ParameterizedTest
    @CsvSource({
            "333.33, 2026, 6, 5, 498.33",
            "99.99, 2026, 6, 1, 129.99"
    })
    @DisplayName("BL-50: округлення вартості ночі за правилом HALF_UP до 2 знаків")
    void nightPriceRoundedHalfUp(String baseRateValue, int year, int month, int day, String expectedValue) {
        // arrange
        BigDecimal baseRate = new BigDecimal(baseRateValue);
        LocalDate night = LocalDate.of(year, month, day);
        BigDecimal expected = new BigDecimal(expectedValue);

        // act
        BigDecimal actual = calculator.nightPrice(baseRate, night);

        // assert
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({
            "2026, 9, 12, 6150.00",
            "2026, 9, 13, 6570.00"
    })
    @DisplayName("BL-50: знижка 10 % при бронюванні від 7 ночей (межі 6 і 7 ночей)")
    void longStayDiscountBoundaries(int checkOutYear, int checkOutMonth, int checkOutDay, String expectedValue) {
        // arrange
        BigDecimal baseRate = new BigDecimal("1000.00");
        LocalDate checkIn = LocalDate.of(2026, 9, 6);
        LocalDate checkOut = LocalDate.of(checkOutYear, checkOutMonth, checkOutDay);
        BigDecimal expected = new BigDecimal(expectedValue);

        // act
        BigDecimal actual = calculator.total(baseRate, checkIn, checkOut);

        // assert
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({
            "100.00, 2026, 6, 1, 130.00",
            "1000.00, 2026, 9, 7, 1000.00",
            "100.00, 2026, 6, 5, 149.50"
    })
    @DisplayName("BL-50: розрахунок вартості будніх і вихідних ночей без зайвої чи пропущеної надбавки")
    void nightPriceWeekdayHasNoSurcharge(String baseRateValue, int year, int month, int day, String expectedValue) {
        // arrange
        BigDecimal baseRate = new BigDecimal(baseRateValue);
        LocalDate night = LocalDate.of(year, month, day);
        BigDecimal expected = new BigDecimal(expectedValue);

        // act
        BigDecimal actual = calculator.nightPrice(baseRate, night);

        // assert
        assertEquals(expected, actual);
    }
}