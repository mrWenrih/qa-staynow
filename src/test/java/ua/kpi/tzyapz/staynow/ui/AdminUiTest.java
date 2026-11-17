package ua.kpi.tzyapz.staynow.ui;

import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * UI-тести адмінпанелі та навігації (TC-01, TC-16, TC-17, TC-20) для «СтейНау».
 * Запуск: mvn test -DexcludedGroups=none -Dtest=AdminUiTest
 */
@Tag("ui")
@DisplayName("СтейНау UI: навігація та авторизація адміністратора")
class AdminUiTest {

    private static final String BASE_URL = "https://automationintesting.online";
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "password";

    private static final By USERNAME_INPUT = By.id("username");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By LOGIN_BUTTON = By.xpath("//button[text()='Login' or @id='doLogin']");
    private static final By LOGOUT_BUTTON = By.xpath("//button[text()='Logout']");
    private static final By ROOM_NAME_INPUT = By.id("roomName");
    private static final By LOGIN_ERROR_ALERT = By.cssSelector(".alert.alert-danger");
    private static final By NAV_ADMIN_LINK = By.cssSelector(".navbar a[href*='admin']");

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void openBrowser() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--window-size=1280,900");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void openAdminLoginPage() {
        driver.get(BASE_URL + "/admin");
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT));
    }

    private void loginAs(String username, String password) {
        openAdminLoginPage();
        driver.findElement(USERNAME_INPUT).sendKeys(username);
        driver.findElement(PASSWORD_INPUT).sendKeys(password);
        wait.until(ExpectedConditions.elementToBeClickable(LOGIN_BUTTON)).click();
    }

    @Test
    @DisplayName("TC-01 (F-10, GUI-10): перехід на сторінку входу адміністратора через головне меню")
    void navigationToAdminViaMenu() {
        // arrange
        driver.get(BASE_URL);
        WebElement adminLink = wait.until(ExpectedConditions.elementToBeClickable(NAV_ADMIN_LINK));

        // act
        adminLink.click();
        wait.until(ExpectedConditions.urlContains("/admin"));
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT));

        // assert
        assertAll(
                () -> assertTrue(driver.getCurrentUrl().contains("/admin"),
                        "URL має містити /admin, фактично: " + driver.getCurrentUrl()),
                () -> assertTrue(usernameField.isDisplayed(),
                        "Поле Username має відображатися на сторінці входу")
        );
    }

    @Test
    @DisplayName("TC-16 (F-80, GUI-70): успішна авторизація адміністратора відкриває панель керування номерами")
    void adminSuccessfulLogin() {
        // arrange & act
        loginAs(ADMIN_USER, ADMIN_PASS);
        wait.until(ExpectedConditions.urlContains("/admin/rooms"));
        WebElement roomNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(ROOM_NAME_INPUT));
        WebElement logoutButton = wait.until(ExpectedConditions.visibilityOfElementLocated(LOGOUT_BUTTON));

        // assert
        assertAll(
                () -> assertTrue(driver.getCurrentUrl().contains("/admin/rooms"),
                        "Очікувався перехід на /admin/rooms, фактично: " + driver.getCurrentUrl()),
                () -> assertTrue(roomNameField.isDisplayed(),
                        "Після входу має відкритися форма керування номерами"),
                () -> assertTrue(logoutButton.isDisplayed(),
                        "Кнопка Logout має відображатися після входу")
        );
    }

    @Test
    @DisplayName("TC-17 (F-80, GUI-70): недійсний пароль показує повідомлення про помилку та блокує вхід")
    void invalidPasswordShowsError() {
        // arrange & act
        loginAs(ADMIN_USER, "wrong_pass123");
        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_ERROR_ALERT));

        // assert
        assertAll(
                () -> assertTrue(alert.getText().contains("Invalid credentials"),
                        "Очікувалося повідомлення Invalid credentials, отримано: " + alert.getText()),
                () -> assertFalse(driver.getCurrentUrl().contains("/admin/rooms"),
                        "При невірному паролі доступ до /admin/rooms має бути заблокований"),
                () -> assertTrue(driver.findElement(USERNAME_INPUT).isDisplayed(),
                        "Форма входу має залишатися видимою")
        );
    }

    @Test
    @DisplayName("TC-20 (F-80, F-85, F-130, GUI-70): вихід через Logout завершує сеанс і блокує повторний доступ до адмінпанелі")
    void adminLogoutReturnsToLoginForm() {
        // arrange
        loginAs(ADMIN_USER, ADMIN_PASS);
        wait.until(ExpectedConditions.visibilityOfElementLocated(ROOM_NAME_INPUT));

        // act
        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_BUTTON)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(ROOM_NAME_INPUT));
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/admin")));
        assertFalse(driver.getCurrentUrl().contains("/admin"),
                "Передумова: після Logout користувача має перенаправити з /admin");

        driver.get(BASE_URL + "/admin");
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT));

        // assert
        assertAll(
                () -> assertTrue(usernameField.isDisplayed(),
                        "При повторному відкритті /admin має відображатися форма входу"),
                () -> assertTrue(driver.findElement(LOGIN_BUTTON).isDisplayed(),
                        "Кнопка Login має відображатися без активної сесії"),
                () -> assertTrue(driver.findElements(ROOM_NAME_INPUT).isEmpty(),
                        "Розділ керування номерами не повинен бути доступним після виходу")
        );
    }
}
