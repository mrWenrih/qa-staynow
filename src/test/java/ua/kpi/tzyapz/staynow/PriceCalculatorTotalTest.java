package ua.kpi.tzyapz.staynow;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PriceCalculatorTotalTest {

    private PriceCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PriceCalculator();
    }

    @Test
    @DisplayName("BL-50: загальна вартість підсумовує ночі при зміні сезону (високий → звичайний)")
    void totalSumsNightsWithMixedSeasons() {
        // arrange
        BigDecimal baseRate = new BigDecimal("1000.00");
        LocalDate checkIn = LocalDate.of(2026, 8, 31);
        LocalDate checkOut = LocalDate.of(2026, 9, 2);
        BigDecimal expected = new BigDecimal("2300.00");

        // act
        BigDecimal actual = calculator.total(baseRate, checkIn, checkOut);

        // assert
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("BL-50: недодатний тариф або виїзд не пізніший за заїзд кидає IllegalArgumentException")
    void invalidTotalParametersThrow() {
        // arrange
        LocalDate validIn = LocalDate.of(2026, 9, 10);
        LocalDate validOut = LocalDate.of(2026, 9, 12);

        // act & assert
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> calculator.total(new BigDecimal("0.00"), validIn, validOut)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> calculator.total(new BigDecimal("-1.00"), validIn, validOut)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> calculator.total(new BigDecimal("1000.00"), validIn, validIn)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> calculator.total(new BigDecimal("1000.00"), validIn, LocalDate.of(2026, 9, 8)))
        );
    }
}
