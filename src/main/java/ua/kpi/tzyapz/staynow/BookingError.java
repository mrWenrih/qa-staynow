package ua.kpi.tzyapz.staynow;

/**
 * Коди помилок валідації бронювання (вимоги BL-10, BL-20).
 * Кожен код має пояснення українською для повідомлень користувачеві.
 */
public enum BookingError {
    CHECK_OUT_NOT_AFTER_CHECK_IN("дата виїзду має бути пізнішою за дату заїзду"),
    NIGHTS_OUT_OF_RANGE("кількість ночей має бути в межах 1..30"),
    CHECK_IN_IN_PAST("дата заїзду не може бути раніше сьогоднішньої"),
    GUESTS_OUT_OF_RANGE("кількість гостей має бути в межах від 1 до місткості номера"),
    INVALID_EMAIL("email має формат локальна_частина@домен.зона"),
    INVALID_PHONE("телефон має формат +380 і 9 цифр");

    private final String message;

    BookingError(String message) {
        this.message = message;
    }

    /** Пояснення помилки українською. */
    public String message() {
        return message;
    }
}
