package ua.kpi.tzyapz.staynow;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriceCalculatorWeekendTest {

    private PriceCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PriceCalculator();
    }

    // Очікувано червоний на v1: дефект D3 (BL-30), Issue #___
    @ParameterizedTest
    @CsvSource({
            "2026, 6, 4, false",
            "2026, 6, 5, true",
            "2026, 6, 6, true",
            "2026, 6, 7, false"
    })
    @DisplayName("BL-30: вихідні ночі — з п'ятниці на суботу та з суботи на неділю")
    void isWeekendNightDaysCheck(int year, int month, int day, boolean expected) {
        // arrange
        LocalDate night = LocalDate.of(year, month, day);

        // act
        boolean actual = calculator.isWeekendNight(night);

        // assert
        assertEquals(expected, actual);
    }
}
