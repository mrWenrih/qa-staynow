# «СтейНау» — модуль бізнес-логіки бронювання, версія 1.0.0

Самостійний Maven-проєкт (JDK 17, JUnit 5). Реалізує правила розділу 5 «Вимоги до бізнес-логіки модуля»
специфікації `SRS_04_СтейНау` (ідентифікатори `BL-10` … `BL-70`). Пакет: `ua.kpi.tzyapz.staynow`.

## Збірка та запуск тестів

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)   # macOS; у Windows/Linux — задайте JAVA_HOME вручну
mvn -q -B test
```

Звіт Surefire: `target/surefire-reports/`. Тести з тегом `ui` за замовчуванням вимкнені (`-DexcludedGroups=none` вмикає їх).

## Що всередині

| Клас | Призначення | Правила SRS |
|---|---|---|
| `BookingValidator` | Перевірка параметрів бронювання: `validate(checkIn, checkOut, guests, capacity, today)` повертає список кодів помилок `BookingError` (порожній — бронювання допустиме); перевантаження з `email` і `phone`; `isValidEmail`, `isValidPhone` | `BL-10`, `BL-20` |
| `BookingError` | Перелік кодів помилок валідації з текстом українською | `BL-10`, `BL-20` |
| `PriceCalculator` | Вартість проживання: `seasonFactor(date)`, `isWeekendNight(date)`, `nightPrice(baseRate, date)`, `total(baseRate, checkIn, checkOut)` | `BL-30`, `BL-40`, `BL-50` |
| `Availability` | Політика скасування `cancellationFee(total, daysBeforeCheckIn)`, `refund(...)`; облік зайнятості номерів `book(room, checkIn, checkOut)`, `isAvailable(room, checkIn, checkOut)` | `BL-60`, `BL-70` |

## Реалізовані правила

- Дати — `java.time.LocalDate`; інтервал проживання — `[checkIn, checkOut)`, кількість ночей = кількість днів між датами.
- `BL-10`: дата виїзду пізніша за дату заїзду; ночей 1..30; дата заїзду не раніше `today`; гостей 1..місткість номера.
- `BL-20`: email у форматі `локальна_частина@домен.зона`; телефон — `+380` і рівно 9 цифр.
- `BL-30`: ночі з п'ятниці на суботу та з суботи на неділю — надбавка 15 %.
- `BL-40`: сезонний коефіцієнт: високий сезон (1 червня – 31 серпня, 20 грудня – 10 січня) ×1,30; низький (лютий, березень, листопад) ×0,80; інший період ×1,00.
- `BL-50`: вартість ночі = базовий тариф × сезонний коефіцієнт × (1,15 для вихідної ночі), округлення `HALF_UP` до 2 знаків; підсумок = сума ночей; за 7 і більше ночей — знижка 10 % від підсумку, округлення `HALF_UP` до 2 знаків. Усі суми — `BigDecimal`, гривні.
- `BL-60`: плата за скасування: 7 і більше днів до заїзду — 0; 3..6 днів — 50 % вартості; менше 3 днів — 100 %.
- `BL-70`: номер вільний, якщо новий інтервал не перетинається з жодним існуючим бронюванням цього номера; виїзд у день заїзду іншого гостя дозволено.

Зразок тестів: `src/test/java/ua/kpi/tzyapz/staynow/ExampleTest.java`.

Перші коміти:
- Оніщук Ілья, КТ, @mrWenrih
- Дубчак Богдан, ІТ, @neuror1ston
- Однораз Олександр, ТА, @Ostrigoth
- Свідзінський Антон, СМ, @tiktakson
end

## Середовище

Потрібен **JDK 17** (Maven 3.8+). Перевірка:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk   # Linux
export JAVA_HOME=$(/usr/libexec/java_home -v 17) # macOS
export PATH="$JAVA_HOME/bin:$PATH"
mvn -v   # Java version: 17.x
```

## Очікуваний результат

`mvn clean test` навмисно завершується **BUILD FAILURE**: 11 тестових викликів червоні, бо фіксують реальні дефекти v1. Це не регресія, а очікуваний стан до спринта 5.

| Дефект | Що фіксує | Issue |
|---|---|---|
| D1 | BL-10: бронювання рівно на 30 ночей відхиляється | #5 |
| D2 | BL-10: заїзд учора не відхиляється, якщо виїзд не раніший за сьогодні | #6 |
| D3 | BL-30: вихідними вважаються сб/нд замість пт/сб | #12 |
| D4 | BL-50: округлення вартості ночі DOWN замість HALF_UP | #7 |
| D5 | BL-60: штраф 50 % при скасуванні рівно за 7 днів | #13 |
| D6 | BL-70: стик інтервалів (виїзд = заїзд) вважається зайнятим | #15 |

Фактичні цифри останнього запуску — `docs/surefire-summary.md`.

## Структура репозиторію

| Шлях | Призначення |
|---|---|
| `src/main/java/ua/kpi/tzyapz/staynow/` | модуль v1: `BookingValidator`, `BookingError`, `PriceCalculator`, `Availability` (байт-у-байт як видано, не змінюється) |
| `src/test/java/ua/kpi/tzyapz/staynow/` | юніт-тести бізнес-логіки (9 власних класів + `ExampleTest`) |
| `ui/` | заплановані UI-тести Selenium для `src/test/java/ua/kpi/tzyapz/staynow/ui/` (готує інший учасник; у цій гілці файлів немає) |
| `selenium-ide/` | `staynow.side` та експортовані Java-класи Selenium IDE |
| `docs/` | `coverage-matrix.md` (матриця покриття і реєстр дефектів), `ui-scenarios.md`, `surefire-summary.md` (протокол запуску) |

## Хто що написав

Авторство визначено командою `git log --format='%an' -- <файл>`:

| Тест-клас | Автор | Роль |
|---|---|---|
| `BookingValidatorTest` | Оніщук Ілья (@mrWenrih) | КТ |
| `PriceCalculatorNightTest` | Оніщук Ілья (@mrWenrih) | КТ |
| `BookingValidatorGuestsTest` | Однораз Олександр (@Ostrigoth) | ТА |
| `PriceCalculatorSeasonTest` | Однораз Олександр (@Ostrigoth) | ТА |
| `CancellationFeeTest` | Однораз Олександр (@Ostrigoth) | ТА |
| `BookingValidatorContactTest` | Свідзінський Антон (@tiktakson) | СМ |
| `PriceCalculatorWeekendTest` | Свідзінський Антон (@tiktakson) | СМ |
| `PriceCalculatorTotalTest` | Дубчак Богдан (@neuror1ston) | ІТ |
| `AvailabilityTest` | Дубчак Богдан (@neuror1ston) | ІТ |
| `ExampleTest` | Оніщук Ілья (@mrWenrih) | виданий зразок, до розподілу не входить |

## Посилання

- Issues проєкту: https://github.com/mrWenrih/qa-staynow/issues
- Матриця покриття: [`docs/coverage-matrix.md`](docs/coverage-matrix.md)
- Протокол Surefire: [`docs/surefire-summary.md`](docs/surefire-summary.md)
- Досьє проєкту: `<URL>`
- Таблиця задач: `<URL>`
- Ретроспектива: `<URL>`
