package ua.kpi.tzyapz.staynow;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Політика скасування та перевірка доступності номера (вимоги BL-60, BL-70).
 */
public final class Availability {

    /** Бронювання номера на інтервал [checkIn, checkOut). */
    public record Stay(String room, LocalDate checkIn, LocalDate checkOut) {
        public Stay {
            Objects.requireNonNull(room, "room");
            Objects.requireNonNull(checkIn, "checkIn");
            Objects.requireNonNull(checkOut, "checkOut");
            if (!checkIn.isBefore(checkOut)) {
                throw new IllegalArgumentException("Дата виїзду має бути пізнішою за дату заїзду");
            }
        }
    }

    /** Кількість днів до заїзду, від якої скасування безкоштовне. */
    public static final int FREE_CANCELLATION_DAYS = 7;
    /** Кількість днів до заїзду, від якої утримується половина вартості. */
    public static final int HALF_FEE_DAYS = 3;

    private static final BigDecimal HALF = new BigDecimal("0.50");
    private static final int SCALE = 2;

    private final List<Stay> stays = new ArrayList<>();

    /**
     * Реєструє бронювання номера.
     *
     * @throws IllegalStateException якщо номер зайнятий на частину інтервалу
     */
    public Stay book(String room, LocalDate checkIn, LocalDate checkOut) {
        Stay stay = new Stay(room, checkIn, checkOut);
        if (!isAvailable(room, checkIn, checkOut)) {
            throw new IllegalStateException("Номер " + room + " зайнятий на вибрані дати");
        }
        stays.add(stay);
        return stay;
    }

    /**
     * Чи вільний номер на інтервал [checkIn, checkOut).
     * Виїзд у день заїзду іншого гостя (і навпаки) дозволено.
     */
    public boolean isAvailable(String room, LocalDate checkIn, LocalDate checkOut) {
        Objects.requireNonNull(room, "room");
        if (!checkIn.isBefore(checkOut)) {
            throw new IllegalArgumentException("Дата виїзду має бути пізнішою за дату заїзду");
        }
        for (Stay s : stays) {
            if (!s.room().equals(room)) {
                continue;
            }
            boolean overlaps = !(checkOut.isBefore(s.checkIn()) || checkIn.isAfter(s.checkOut()));
            if (overlaps) {
                return false;
            }
        }
        return true;
    }

    /** Плата за скасування залежно від кількості днів до заїзду. */
    public BigDecimal cancellationFee(BigDecimal total, int daysBeforeCheckIn) {
        if (total == null || total.signum() < 0) {
            throw new IllegalArgumentException("Вартість не може бути від'ємною");
        }
        if (daysBeforeCheckIn > FREE_CANCELLATION_DAYS) {
            return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        }
        if (daysBeforeCheckIn >= HALF_FEE_DAYS) {
            return total.multiply(HALF).setScale(SCALE, RoundingMode.HALF_UP);
        }
        return total.setScale(SCALE, RoundingMode.HALF_UP);
    }

    /** Сума, що повертається гостю після утримання плати за скасування. */
    public BigDecimal refund(BigDecimal total, int daysBeforeCheckIn) {
        return total.subtract(cancellationFee(total, daysBeforeCheckIn))
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    /** Незмінний перелік зареєстрованих бронювань. */
    public List<Stay> stays() {
        return Collections.unmodifiableList(stays);
    }
}
