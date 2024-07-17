package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class AssignmentControllerSystemTest {

    public static final String CHROME_DRIVER_FILE_LOCATION = "C:/chromedriver-win64/chromedriver.exe";
    public static final String URL = "http://localhost:3000/";
    public static final int SLEEP_DURATION = 1000;

    WebDriver driver;

    @BeforeEach
    public void setUpDriver() throws Exception {
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(ops);
        driver.get(URL);
        Thread.sleep(SLEEP_DURATION);
    }

    @AfterEach
    public void terminateDriver() {
        if (driver != null) {
            driver.close();
            driver.quit();
            driver = null;
        }
    }

    @Test
    public void systemTestEnterFinalGrades() throws Exception {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Navigate to the sections page
        WebElement sectionsLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Sections')]")));
        sectionsLink.click();
        Thread.sleep(SLEEP_DURATION);

        // Perform actions to enter final grades
        driver.findElement(By.id("scourseId")).sendKeys("cst");
        driver.findElement(By.id("syear")).sendKeys("2024");
        driver.findElement(By.id("ssemester")).sendKeys("Spring");

        driver.findElement(By.id("search")).click();
        Thread.sleep(SLEEP_DURATION);

        WebElement table = driver.findElement(By.className("Center"));
        assertNotNull(table, "Sections table should be present");

        WebElement sectionRow = table.findElement(By.xpath("//tr[td[text()='1']]"));
        assertNotNull(sectionRow, "Section row should be present");

        // Example: Enter grades for students in the section
        // This part depends on how the UI for entering grades is structured

        // Assert the expected outcome, for example:
        // String message = driver.findElement(By.id("message")).getText();
        // assertEquals("Grades entered successfully", message);
    }
}
