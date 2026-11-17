package ua.kpi.tzyapz.staynow.ui;

import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * UI-тести контактної форми «Send Us a Message» (TC-13, TC-14) для «СтейНау».
 * Запуск: mvn test -DexcludedGroups=none -Dtest=ContactUiTest
 */
@Tag("ui")
@DisplayName("СтейНау UI: контактна форма")
class ContactUiTest {

    private static final String BASE_URL = "https://automationintesting.online";

    private static final By NAME_INPUT = By.id("name");
    private static final By EMAIL_INPUT = By.id("email");
    private static final By PHONE_INPUT = By.id("phone");
    private static final By SUBJECT_INPUT = By.id("subject");
    private static final By DESCRIPTION_INPUT = By.id("description");
    private static final By SUBMIT_BUTTON = By.cssSelector("#contact button");
    private static final By CONTACT_SECTION = By.id("contact");
    private static final By THANKS_HEADING = By.xpath("//h3[contains(text(),'Thanks for getting in touch')]");
    private static final By ERROR_ALERT = By.cssSelector("#contact .alert-danger");

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

    private void submitContactForm(String name, String email, String phone, String subject, String message) {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(NAME_INPUT)).sendKeys(name);
        driver.findElement(EMAIL_INPUT).sendKeys(email);
        driver.findElement(PHONE_INPUT).sendKeys(phone);
        driver.findElement(SUBJECT_INPUT).sendKeys(subject);
        driver.findElement(DESCRIPTION_INPUT).sendKeys(message);

        WebElement submit = driver.findElement(SUBMIT_BUTTON);
        // Миттєвий скрол до кнопки прибирає конфлікт із smooth-scrolling у Bootstrap 5
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({behavior:'instant', block:'center'});", submit);
        wait.until(ExpectedConditions.elementToBeClickable(submit)).click();
    }

    @Test
    @DisplayName("TC-13 (F-70, GUI-90): успішне надсилання контактної форми показує подяку з іменем і темою")
    void contactFormSubmissionSuccess() {
        // arrange
        String guestName = "T4-Maxym";
        String subject = "T4-Reservation";

        // act
        submitContactForm(
                guestName,
                "test@example.com",
                "09876543210",
                subject,
                "Hello, I would like to check early check-in possibility."
        );
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(THANKS_HEADING));
        String contactText = driver.findElement(CONTACT_SECTION).getText();

        // assert
        assertAll(
                () -> assertTrue(heading.isDisplayed(),
                        "Має з'явитися заголовок подяки"),
                () -> assertTrue(contactText.contains("Thanks for getting in touch " + guestName + "!"),
                        "У блоці подяки очікувалось ім'я гостя, фактично: " + contactText),
                () -> assertTrue(contactText.contains(subject),
                        "У блоці подяки очікувалась тема звернення, фактично: " + contactText)
        );
    }

    @Test
    @DisplayName("TC-14 (F-70, GUI-90): тема коротша за 5 символів показує помилку валідації")
    void contactFormSubjectTooShortShowsError() {
        // arrange & act
        submitContactForm(
                "T4-Maxym",
                "test@example.com",
                "09876543210",
                "Help",
                "Valid message text with more than 20 chars."
        );
        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_ALERT));
        String errorText = alert.getText();

        // assert
        assertAll(
                () -> assertTrue(alert.isDisplayed(),
                        "Має відображатися блок помилок валідації"),
                () -> assertTrue(errorText.contains("Subject must be between 5 and 100 characters"),
                        "Очікувалась помилка довжини теми, отримано: " + errorText),
                () -> assertTrue(driver.findElement(SUBJECT_INPUT).isDisplayed(),
                        "Форма не повинна зникати при помилці валідації")
        );
    }
}
