package ua.kpi.tzyapz.staynow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Зразок стилю юніт-тестів для модуля «СтейНау»: один клас — один предмет перевірки. */
@DisplayName("СтейНау — зразок юніт-тестів модуля бронювання")
class ExampleTest {

    private PriceCalculator calc;
    private BookingValidator validator;

    @BeforeEach
    void setUp() {
        calc = new PriceCalculator();
        validator = new BookingValidator();
    }

    @Test
    @DisplayName("BL-50: дві будні ночі у звичайний період коштують 2 × базовий тариф")
    void twoWeekdayNightsRegularSeason() {
        BigDecimal total = calc.total(new BigDecimal("100"),
                LocalDate.of(2026, 9, 15), LocalDate.of(2026, 9, 17));
        assertEquals(new BigDecimal("200.00"), total);
    }

    @Test
    @DisplayName("BL-10/BL-20: коректне бронювання не має помилок валідації")
    void validBookingHasNoErrors() {
        var errors = validator.validate(LocalDate.of(2026, 9, 15), LocalDate.of(2026, 9, 17),
                2, 2, LocalDate.of(2026, 9, 8), "guest@example.com", "+380501234567");
        assertTrue(errors.isEmpty(), "Очікувався порожній список помилок, отримано: " + errors);
    }
}
