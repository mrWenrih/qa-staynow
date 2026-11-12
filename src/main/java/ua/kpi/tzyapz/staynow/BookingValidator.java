package ua.kpi.tzyapz.staynow;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Перевірка параметрів бронювання номера (вимоги BL-10, BL-20).
 * Інтервал проживання — [checkIn, checkOut); кількість ночей — днів між датами.
 */
public final class BookingValidator {

    /** Мінімальна кількість ночей. */
    public static final int MIN_NIGHTS = 1;
    /** Максимальна кількість ночей. */
    public static final int MAX_NIGHTS = 30;

    private static final Pattern EMAIL =
            Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)*\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^\\+380\\d{9}$");

    /**
     * Перевіряє дати заїзду/виїзду, кількість ночей і гостей.
     *
     * @param checkIn  дата заїзду
     * @param checkOut дата виїзду
     * @param guests   кількість гостей
     * @param capacity місткість номера
     * @param today    поточна дата
     * @return список помилок; порожній список означає, що бронювання допустиме
     */
    public List<BookingError> validate(LocalDate checkIn, LocalDate checkOut,
                                       int guests, int capacity, LocalDate today) {
        Objects.requireNonNull(checkIn, "checkIn");
        Objects.requireNonNull(checkOut, "checkOut");
        Objects.requireNonNull(today, "today");
        List<BookingError> errors = new ArrayList<>();

        if (!checkIn.isBefore(checkOut)) {
            errors.add(BookingError.CHECK_OUT_NOT_AFTER_CHECK_IN);
        } else {
            long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
            if (nights < MIN_NIGHTS || nights >= MAX_NIGHTS) {
                errors.add(BookingError.NIGHTS_OUT_OF_RANGE);
            }
        }
        if (checkOut.isBefore(today)) {
            errors.add(BookingError.CHECK_IN_IN_PAST);
        }
        if (guests < 1 || guests > capacity) {
            errors.add(BookingError.GUESTS_OUT_OF_RANGE);
        }
        return errors;
    }

    /**
     * Повна перевірка бронювання разом із контактними даними гостя.
     *
     * @return список помилок; порожній список означає, що бронювання допустиме
     */
    public List<BookingError> validate(LocalDate checkIn, LocalDate checkOut,
                                       int guests, int capacity, LocalDate today,
                                       String email, String phone) {
        List<BookingError> errors = validate(checkIn, checkOut, guests, capacity, today);
        if (!isValidEmail(email)) {
            errors.add(BookingError.INVALID_EMAIL);
        }
        if (!isValidPhone(phone)) {
            errors.add(BookingError.INVALID_PHONE);
        }
        return errors;
    }

    /** Чи відповідає email формату «локальна_частина@домен.зона». */
    public boolean isValidEmail(String email) {
        return email != null && EMAIL.matcher(email).matches();
    }

    /** Чи відповідає телефон формату «+380» і 9 цифр. */
    public boolean isValidPhone(String phone) {
        return phone != null && PHONE.matcher(phone).matches();
    }
}
