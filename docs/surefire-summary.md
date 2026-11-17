# Протокол запуску тестів (Surefire)

**Дата:** 2026-11-18
**Коміт:** `f12a8a0` (гілка `feature/dod-docs-fixes`)
**JDK:** OpenJDK 17.0.21 (`/usr/lib/jvm/java-17-openjdk`)
**Maven:** 3.10.0
**ОС:** Linux 7.2.11-arch1-1 (x86_64)
**Модуль:** `ua.kpi.tzyapz.staynow`, v1 (без змін у `src/main/java`)

## 1. `mvn -B clean test`

| Показник | Значення |
|---|---|
| Tests run | **76** |
| Failures | **11** |
| Errors | **0** |
| Skipped | **0** |
| Результат | **BUILD FAILURE** |

UI-тести з тегом `ui` виключені конфігурацією `excludedGroups=ui` (у наборі їх немає).

## 2. `mvn -B test -DexcludedGroups=none`

| Показник | Значення |
|---|---|
| Tests run | **76** |
| Failures | **11** |
| Errors | **0** |
| Skipped | **0** |
| Результат | **BUILD FAILURE** |

Показники збігаються з першим запуском, бо єдиний тест із тегом `ui` — `SmokeUiTest` (пакет `ua.kpi.tzyapz.starter.ui`, Sauce Demo) — видалено як такий, що не стосується «СтейНау». До видалення цей запуск давав `Tests run: 77, Failures: 11` (той тест проходив).

## 3. Розподіл за класами (перший запуск)

| Тест-клас | Tests run | Failures |
|---|---|---|
| `AvailabilityTest` | 10 | 3 |
| `BookingValidatorContactTest` | 13 | 0 |
| `BookingValidatorGuestsTest` | 4 | 0 |
| `BookingValidatorTest` | 9 | 2 |
| `CancellationFeeTest` | 7 | 1 |
| `ExampleTest` | 2 | 0 |
| `PriceCalculatorNightTest` | 7 | 3 |
| `PriceCalculatorSeasonTest` | 18 | 0 |
| `PriceCalculatorTotalTest` | 2 | 0 |
| `PriceCalculatorWeekendTest` | 4 | 2 |
| **Разом** | **76** | **11** |

`Tests run` — кількість тестових викликів після розгортання параметризованих методів; тестових методів у коді 27 (25 власних + 2 у `ExampleTest`).

## 4. Червоні тести

| № | Тест | Фактично | Дефект | Issue |
|---|---|---|---|---|
| 1 | `AvailabilityTest.adjacentBookingsAreAvailable` `[2026-07-05, 2026-07-10)` | expected `true`, was `false` | D6 | #15 |
| 2 | `AvailabilityTest.adjacentBookingsAreAvailable` `[2026-07-15, 2026-07-20)` | expected `true`, was `false` | D6 | #15 |
| 3 | `AvailabilityTest.refundCalculatesDifferenceCorrectly` (7 днів) | expected `1000.00`, was `500.00` | D5 | #13 |
| 4 | `BookingValidatorTest.checkInRelativeToTodayBoundaries` | 2 failures (заїзд учора) | D2 | #6 |
| 5 | `BookingValidatorTest.nightsBoundaries` (30 ночей) | expected `true` (порожній список), was `false` | D1 | #5 |
| 6 | `CancellationFeeTest.cancellationFeeBoundaries` (7 днів) | expected `0.00`, was `500.00` | D5 | #13 |
| 7 | `PriceCalculatorNightTest.nightPriceRoundedHalfUp` (пт, 498.33) | expected `498.33`, was `433.32` | D3, D4 | #12, #7 |
| 8 | `PriceCalculatorNightTest.nightPriceRoundedHalfUp` (пн, 129.99) | expected `129.99`, was `129.98` | D4 | #7 |
| 9 | `PriceCalculatorNightTest.nightPriceWeekdayHasNoSurcharge` (пт) | expected `149.50`, was `130.00` | D3 | #12 |
| 10 | `PriceCalculatorWeekendTest.isWeekendNightDaysCheck` (пт) | expected `true`, was `false` | D3 | #12 |
| 11 | `PriceCalculatorWeekendTest.isWeekendNightDaysCheck` (нд) | expected `false`, was `true` | D3 | #12 |

Червоні тести навмисно фіксують дефекти v1 і лишаються в наборі до спринта 5.

## 5. Як відтворити

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
mvn -B clean test
mvn -B test -DexcludedGroups=none
```

Детальні звіти: `target/surefire-reports/`.
