package ua.kpi.tzyapz.starter.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Зразок UI-тесту Selenium WebDriver. Має тег "ui" і за замовчуванням не запускається.
 * Запуск: mvn test -DexcludedGroups=none   (потрібен встановлений Chrome; драйвер завантажує Selenium Manager)
 */
@Tag("ui")
@DisplayName("Smoke: головна сторінка demo-застосунку відкривається")
class SmokeUiTest {

    private WebDriver driver;

    @BeforeEach
    void openBrowser() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--window-size=1280,900");
        driver = new ChromeDriver(options);
    }

    @AfterEach
    void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void homePageHasTitle() {
        driver.get("https://www.saucedemo.com/");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
        assertTrue(driver.getTitle().contains("Swag Labs"), "Очікувався заголовок Swag Labs, отримано: " + driver.getTitle());
    }
}
