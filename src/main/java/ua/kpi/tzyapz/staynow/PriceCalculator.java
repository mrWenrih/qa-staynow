package ua.kpi.tzyapz.staynow;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.Objects;

/**
 * Розрахунок вартості проживання (вимоги BL-30, BL-40, BL-50).
 * Усі суми — {@link BigDecimal} у гривнях, округлення HALF_UP до 2 знаків.
 */
public final class PriceCalculator {

    /** Коефіцієнт високого сезону. */
    public static final BigDecimal HIGH_SEASON = new BigDecimal("1.30");
    /** Коефіцієнт низького сезону. */
    public static final BigDecimal LOW_SEASON = new BigDecimal("0.80");
    /** Коефіцієнт звичайного періоду. */
    public static final BigDecimal REGULAR_SEASON = new BigDecimal("1.00");
    /** Надбавка за вихідну ніч (+15 %). */
    public static final BigDecimal WEEKEND_SURCHARGE = new BigDecimal("1.15");
    /** Множник знижки за тривале перебування (−10 %). */
    public static final BigDecimal LONG_STAY_FACTOR = new BigDecimal("0.90");
    /** Мінімальна кількість ночей для знижки за тривале перебування. */
    public static final int LONG_STAY_NIGHTS = 7;

    private static final int SCALE = 2;

    /** Сезонний коефіцієнт для ночі, що починається у вказану дату. */
    public BigDecimal seasonFactor(LocalDate night) {
        Objects.requireNonNull(night, "night");
        Month month = night.getMonth();
        int day = night.getDayOfMonth();
        if (month == Month.JUNE || month == Month.JULY || month == Month.AUGUST) {
            return HIGH_SEASON;
        }
        if ((month == Month.DECEMBER && day >= 20) || (month == Month.JANUARY && day <= 10)) {
            return HIGH_SEASON;
        }
        if (month == Month.FEBRUARY || month == Month.MARCH || month == Month.NOVEMBER) {
            return LOW_SEASON;
        }
        return REGULAR_SEASON;
    }

    /** Чи є ніч «вихідною» (з п'ятниці на суботу або з суботи на неділю). */
    public boolean isWeekendNight(LocalDate night) {
        DayOfWeek dow = night.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }

    /** Вартість однієї ночі з урахуванням сезону та вихідних, округлена до копійок. */
    public BigDecimal nightPrice(BigDecimal baseRate, LocalDate night) {
        requirePositive(baseRate);
        BigDecimal price = baseRate.multiply(seasonFactor(night));
        if (isWeekendNight(night)) {
            price = price.multiply(WEEKEND_SURCHARGE);
        }
        return price.setScale(SCALE, RoundingMode.DOWN);
    }

    /**
     * Загальна вартість проживання за всі ночі інтервалу [checkIn, checkOut)
     * зі знижкою 10 % за 7 і більше ночей.
     *
     * @throws IllegalArgumentException якщо тариф не додатний або виїзд не пізніший за заїзд
     */
    public BigDecimal total(BigDecimal baseRate, LocalDate checkIn, LocalDate checkOut) {
        requirePositive(baseRate);
        if (!checkIn.isBefore(checkOut)) {
            throw new IllegalArgumentException("Дата виїзду має бути пізнішою за дату заїзду");
        }
        BigDecimal sum = BigDecimal.ZERO;
        int nights = 0;
        for (LocalDate night = checkIn; night.isBefore(checkOut); night = night.plusDays(1)) {
            sum = sum.add(nightPrice(baseRate, night));
            nights++;
        }
        if (nights >= LONG_STAY_NIGHTS) {
            sum = sum.multiply(LONG_STAY_FACTOR);
        }
        return sum.setScale(SCALE, RoundingMode.HALF_UP);
    }

    private static void requirePositive(BigDecimal baseRate) {
        if (baseRate == null || baseRate.signum() <= 0) {
            throw new IllegalArgumentException("Базовий тариф має бути додатним");
        }
    }
}
