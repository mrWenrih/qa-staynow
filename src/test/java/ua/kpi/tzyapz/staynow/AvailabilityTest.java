package ua.kpi.tzyapz.staynow;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvailabilityTest {

    private static final String ROOM_101 = "101";
    private static final LocalDate BOOKED_IN = LocalDate.of(2026, 7, 10);
    private static final LocalDate BOOKED_OUT = LocalDate.of(2026, 7, 15);

    private Availability availability;

    @BeforeEach
    void setUp() {
        availability = new Availability();
    }

    // Очікувано частково червоний на v1 (для 7 днів): дефект D5 (BL-60), Issue #13
    @ParameterizedTest(name = "BL-60: total={0}, днів до заїзду={1} → повернення={2}")
    @CsvSource({
            "111.11, 4, 55.55",
            "1000.00, 7, 1000.00"
    })
    @DisplayName("BL-60: сума повернення дорівнює total мінус плата за скасування")
    void refundCalculatesDifferenceCorrectly(String totalValue, int daysBeforeCheckIn, String expectedRefundValue) {
        // arrange
        BigDecimal total = new BigDecimal(totalValue);
        BigDecimal expected = new BigDecimal(expectedRefundValue);

        // act
        BigDecimal actual = availability.refund(total, daysBeforeCheckIn);

        // assert
        assertEquals(expected, actual);
    }

    // Очікувано червоний на v1: дефект D6 (BL-70), Issue #15
    @ParameterizedTest(name = "BL-70: суміжний інтервал [{0}, {1}) має бути вільним")
    @CsvSource({
            "2026-07-05, 2026-07-10",
            "2026-07-15, 2026-07-20"
    })
    @DisplayName("BL-70: виїзд у день заїзду іншого гостя (і навпаки) дозволено")
    void adjacentBookingsAreAvailable(LocalDate checkIn, LocalDate checkOut) {
        // arrange
        availability.book(ROOM_101, BOOKED_IN, BOOKED_OUT);

        // act
        boolean actual = availability.isAvailable(ROOM_101, checkIn, checkOut);

        // assert
        assertTrue(actual, "Суміжний інтервал [" + checkIn + ", " + checkOut + ") має бути вільним");
    }

    @ParameterizedTest(name = "BL-70: номер {0} на [{1}, {2}) → вільний={3}")
    @CsvSource({
            "101, 2026-07-12, 2026-07-14, false",
            "101, 2026-07-08, 2026-07-12, false",
            "101, 2026-07-13, 2026-07-20, false",
            "101, 2026-07-08, 2026-07-20, false",
            "102, 2026-07-12, 2026-07-14, true"
    })
    @DisplayName("BL-70: перетин дат одного номера недоступний, інший номер на ті самі дати вільний")
    void overlappingBookingsNotAvailable(String room, LocalDate checkIn, LocalDate checkOut, boolean expectedAvailable) {
        // arrange
        availability.book(ROOM_101, BOOKED_IN, BOOKED_OUT);

        // act
        boolean actual = availability.isAvailable(room, checkIn, checkOut);

        // assert
        assertEquals(expectedAvailable, actual);
    }

    @Test
    @DisplayName("BL-70: бронювання зайнятого номера кидає IllegalStateException")
    void bookingOccupiedRoomThrows() {
        // arrange
        availability.book(ROOM_101, BOOKED_IN, BOOKED_OUT);
        LocalDate overlapIn = LocalDate.of(2026, 7, 12);
        LocalDate overlapOut = LocalDate.of(2026, 7, 14);

        // act & assert
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> availability.book(ROOM_101, overlapIn, overlapOut));
        assertNotNull(ex.getMessage());
    }
}
