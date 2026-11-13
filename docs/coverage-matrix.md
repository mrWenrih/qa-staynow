# Матриця покриття бізнес-логіки модуля StayNow (BL10–BL70)

**Проєкт:** `ua.kpi.tzyapz.staynow` (версія v1)  
**Склад команди:** КТ (Керівник), ТА (Аналітик), ІТ (Інженер / SDET), СМ (Скрам-майстер)  
**Укладач:** ТА  
**Дата:** 2026-11-13  

> [!NOTE]
> Джерело очікуваних результатів — виправлена SRS продукту (розділ BL зі спринта 2). Усі розрахунки сум виконані у гривнях (`BigDecimal`) з округленням `HALF_UP` до 2 знаків. Вимогу **BL80** вилучено під час аудиту (дефект № 12, дублювала BL60).

---

### 1. BookingValidator (`ua.kpi.tzyapz.staynow.BookingValidator`)

| ID | Правило / Суть перевірки | Тип | Вхідні дані | Очікувано за SRS | Назва тест-методу | Авт./Рев. | v1 |
|:---|:---|:---:|:---|:---|:---|:---:|:---:|
| **BL10** | Валідне бронювання | Поз. | today=01.06, in=02.06, out=05.06 (3 ночі), guests=2, cap=3 | Порожній список `[]` | `validBookingReturnsNoErrors` | КТ / СМ | Pass |
| **BL10** | Виїзд не пізніший за заїзд | Нег. | in=05.06, out=05.06 (0 ночей) або out=04.06 | Помилка дат у списку | `checkOutNotAfterCheckInFails` | КТ / СМ | Pass |
| **BL10** | Ночі: межі 1..30 включно | BVA | 0, 1, 30, 31 ночей | 0, 31 → помилка; 1, 30 → OK | `nightsBoundaries` | КТ / ТА | — |
| **BL10** | Заїзд відносно today | BVA | in: вчора, сьогодні, завтра | вчора → помилка; сьогодні, завтра → OK | `checkInRelativeToTodayBoundaries` | КТ / ТА | — |
| **BL10** | Гості: 1 ≤ guests ≤ cap | BVA | cap=3; guests: 0, 1, 3, 4 | 0 → INVALID_GUESTS; 1, 3 → OK; 4 → CAPACITY_EXCEEDED | `guestsCapacityBoundaries` | ТА / СМ | — |
| **BL20** | Валідні email | Поз. | `user@example.com`, `name@sub.domain.ua` | `true` | `validEmailsAccepted` | СМ / ТА | Pass |
| **BL20** | Невалідні email | Нег. | `@domain.com`, `user@`, `user@domain`, `null` | `false` для кожного | `invalidEmailFormatsRejected` | СМ / ТА | — |
| **BL20** | Телефон: +380 + 9 цифр | BVA | `+38050123456` (8 цифр), `+380501234567` (9), `+3805012345678` (10) | 8, 10 цифр → `false`; 9 цифр → `true` | `phoneDigitsCountBoundaries` | СМ / ТА | — |
| **BL20** | Телефон: без + або літери | Нег. | `380501234567`, `+38050123456a`, `null` | `false` | `invalidPhonePrefixAndChars` | СМ / ТА | Pass |
| **BL20** | Перевантаження validate | Поз. | Невалідний email + невалідний phone | Містить `INVALID_EMAIL` і `INVALID_PHONE` | `validateOverloadCollectsEmailPhoneErrors` | СМ / ТА | — |

---

### 2. PriceCalculator (`ua.kpi.tzyapz.staynow.PriceCalculator`)

| ID | Правило / Суть перевірки | Тип | Вхідні дані | Очікувано за SRS | Назва тест-методу | Авт./Рев. | v1 |
|:---|:---|:---:|:---|:---|:---|:---:|:---:|
| **BL30** | Вихідні ночі (Пт, Сб = +15%) | BVA | FRIDAY, SATURDAY, SUNDAY, THURSDAY | Пт, Сб → `true`; Нд, Чт → `false` | `isWeekendNightDaysCheck` | СМ / КТ | — |
| **BL40** | Високий сезон: літо | BVA | 31.05, 01.06, 31.08, 01.09 | 01.06, 31.08 → `1.30`; 31.05, 01.09 → `1.00` | `summerHighSeasonBoundaries` | ТА / КТ | — |
| **BL40** | Високий сезон: зима | BVA | 19.12, 20.12, 31.12, 01.01, 10.01, 11.01 | 20.12..10.01 → `1.30`; 19.12, 11.01 → `1.00` | `winterHighSeasonBoundaries` | ТА / КТ | — |
| **BL40** | Низький сезон (лют, бер, лист) | BVA | 31.01, 01.02, 31.03, 01.04, 31.10, 01.11, 30.11, 01.12 | лют, бер, лист → `0.80`; решта дат → `1.00` | `lowSeasonBoundaries` | ТА / КТ | — |
| **BL50** | Розрахунок nightPrice | Розрахунок | base=333.33, season=1.30, вихідний (1.15) | `498.33` ($333.33 \times 1.30 \times 1.15 = 498.32835$) | `nightPriceCalculatedAndRoundedHalfUp` | КТ / ТА | — |
| **BL50** | Знижка 10% (ночей ≥ 7) | BVA | base=1000.00, звичайний сезон, будні; ночей: 6, 7, 8 | 6 ночей → `6000.00`; 7 → `6300.00`; 8 → `7200.00` | `longStayDiscountBoundaries` | КТ / ТА | — |
| **BL50** | Недодатний тариф / дати | Виняток | baseRate ≤ 0 або checkOut ≤ checkIn | `IllegalArgumentException` | `invalidTotalParametersThrow` | ІТ / ТА | Pass |

---

### 3. Availability (`ua.kpi.tzyapz.staynow.Availability`)

| ID | Правило / Суть перевірки | Тип | Вхідні дані | Очікувано за SRS | Назва тест-методу | Авт./Рев. | v1 |
|:---|:---|:---:|:---|:---|:---|:---:|:---:|
| **BL60** | Штраф скасування: дні | BVA | total=1000.00; дні: 8, 7, 6, 3, 2, 0 | 8, 7 дн → `0.00`; 6, 3 дн → `500.00`; 2, 0 дн → `1000.00` | `cancellationFeeBoundaries` | ТА / ІТ | — |
| **BL60** | Розрахунок повернення refund | Розрахунок | total=111.11, 4 дні (штраф 50% = 55.56) | refund = `55.55` ($111.11 - 55.56$) | `refundCalculatesDifferenceCorrectly` | ІТ / ТА | — |
| **BL70** | Вільність: стик у стик [in, out) | Поз. | Зайнято: 10..15.07. Запит: 05..10.07 або 15..20.07 | `true` (виїзд/заїзд в один день дозволено) | `adjacentBookingsAreAvailable` | ІТ / КТ | Pass |
| **BL70** | Вільність: перетин дат | Нег. | Зайнято: 10..15.07. Запит: 12..14.07 або 08..12.07 | `false` (перетин інтервалів) | `overlappingBookingsNotAvailable` | ІТ / КТ | — |
| **BL70** | Бронювання зайнятого номера | Виняток | Спроба `book()` на зайняті дати | `IllegalStateException` | `bookingOccupiedRoomThrows` | ІТ / СМ | Pass |

---

## 4. Розподіл тестів (6 – 5 – 6 – 5, усього 22 тести)

- **КТ (Керівник тестування) — 6 тестів:**
  - `BookingValidatorTest.validBookingReturnsNoErrors` (BL10)
  - `BookingValidatorTest.checkOutNotAfterCheckInFails` (BL10)
  - `BookingValidatorTest.nightsBoundaries` (BL10)
  - `BookingValidatorTest.checkInRelativeToTodayBoundaries` (BL10)
  - `PriceCalculatorTest.nightPriceCalculatedAndRoundedHalfUp` (BL50)
  - `PriceCalculatorTest.longStayDiscountBoundaries` (BL50)
  - *Загальні задачі ролі:* README, протокол Surefire, підготовка та проведення Демо.

- **ТА (Тест-аналітик) — 5 тестів:**
  - `BookingValidatorTest.guestsCapacityBoundaries` (BL10)
  - `PriceCalculatorTest.summerHighSeasonBoundaries` (BL40)
  - `PriceCalculatorTest.winterHighSeasonBoundaries` (BL40)
  - `PriceCalculatorTest.lowSeasonBoundaries` (BL40)
  - `AvailabilityTest.cancellationFeeBoundaries` (BL60)
  - *Загальні задачі ролі:* Матриця покриття BL (`coverage-matrix.md`), Специфікація UI-сценаріїв (`ui-scenarios.md`).

- **СМ (Скрам-майстер) — 6 тестів:**
  - `BookingValidatorTest.validEmailsAccepted` (BL20)
  - `BookingValidatorTest.invalidEmailFormatsRejected` (BL20)
  - `BookingValidatorTest.phoneDigitsCountBoundaries` (BL20)
  - `BookingValidatorTest.invalidPhonePrefixAndChars` (BL20)
  - `BookingValidatorTest.validateOverloadCollectsEmailPhoneErrors` (BL20)
  - `PriceCalculatorTest.isWeekendNightDaysCheck` (BL30)
  - *Загальні задачі ролі:* Code Review за чек-листом 5.3, протокол ретроспективи, стендапи.

- **ІТ (Інженер з тестування / SDET) — 5 юніт-тестів + UI-автоматизація:**
  - `PriceCalculatorTest.invalidTotalParametersThrow` (BL50)
  - `AvailabilityTest.refundCalculatesDifferenceCorrectly` (BL60)
  - `AvailabilityTest.adjacentBookingsAreAvailable` (BL70)
  - `AvailabilityTest.overlappingBookingsNotAvailable` (BL70)
  - `AvailabilityTest.bookingOccupiedRoomThrows` (BL70)
  - *Загальні задачі ролі:* Maven/CI інфраструктура, проєкт Selenium IDE, написання WebDriver UI-тестів (`@Tag("ui")`).

---

## 5. Реєстр знайдених дефектів модуля v1 (заповнюється під час виконання `mvn test`)

> [!WARNING]
> Усі розбіжності коду v1 з очікуваними значеннями SRS фіксуються в GitHub Issues з міткою `module`. Тести залишаються в коді «червоними».

| Issue ID | Вимога | Тест-метод | Фактичний результат у v1 | Очікувано за SRS | Severity | Автор |
|:---|:---:|:---|:---|:---|:---:|:---:|
| `#1` | BL40 | `winterHighSeasonBoundaries` | *[заповнити після mvn test]* | 10 січня коефіцієнт має бути 1.30 | Major | ТА |
| `#2` | BL60 | `cancellationFeeBoundaries` | *[заповнити після mvn test]* | За 7 днів штраф має бути 0.00 | Major | ТА |
| `#3` | BL10 | `nightsBoundaries` | *[заповнити після mvn test]* | 30 ночей має бути дозволено | Major | КТ |
| `#4` | BL20 | *[заповнити після mvn test]* | *[заповнити після mvn test]* | *[заповнити після mvn test]* | Minor | СМ |
