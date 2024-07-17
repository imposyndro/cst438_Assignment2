package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    public void systemTestEnterGrades() throws Exception {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement sectionsLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Sections')]")));
        sectionsLink.click();
        Thread.sleep(SLEEP_DURATION);

        driver.findElement(By.id("year")).sendKeys("2024");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        WebElement showSectionsLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Show Sections')]")));
        showSectionsLink.click();
        Thread.sleep(SLEEP_DURATION);

        WebElement table = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("Center")));
        assertNotNull(table, "Sections table should be present");

        WebElement sectionRow = table.findElement(By.xpath("//tr[td[text()='1']]"));
        assertNotNull(sectionRow, "Section row should be present");

        WebElement enrollmentsLink = sectionRow.findElement(By.linkText("Enrollments"));
        enrollmentsLink.click();
        Thread.sleep(SLEEP_DURATION);

        WebElement enrollmentsTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("Center")));
        assertNotNull(enrollmentsTable, "Enrollments table should be present");


        WebElement gradeField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@name='grade']")));
        gradeField.clear();
        gradeField.sendKeys("A");

        WebElement saveGradesButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("saveGradesButton")));
        saveGradesButton.click();
        Thread.sleep(SLEEP_DURATION);


        WebElement messageElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h3")));
        String message = messageElement.getText();
        assertEquals("Grades saved", message);
    }

    @Test
    public void systemTestGradeAssignment() throws Exception {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement sectionsLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Sections')]")));
        sectionsLink.click();
        Thread.sleep(SLEEP_DURATION);

        driver.findElement(By.id("year")).sendKeys("2024");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        WebElement showSectionsLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Show Sections')]")));
        showSectionsLink.click();
        Thread.sleep(SLEEP_DURATION);

        WebElement table = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("Center")));
        assertNotNull(table, "Sections table should be present");

        WebElement sectionRow = table.findElement(By.xpath("//tr[td[text()='1']]"));
        assertNotNull(sectionRow, "Section row should be present");

        WebElement assignmentsLink = sectionRow.findElement(By.linkText("Assignments"));
        assignmentsLink.click();
        Thread.sleep(SLEEP_DURATION);

        WebElement assignmentsTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("Center")));
        assertNotNull(assignmentsTable, "Assignments table should be present");

        List<WebElement> gradeButtons = assignmentsTable.findElements(By.xpath("//button[text()='Grade']"));
        for (WebElement gradeButton : gradeButtons) {
            gradeButton.click();
            Thread.sleep(SLEEP_DURATION);

            WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("MuiDialog-paper")));
            assertNotNull(modal, "Grade modal should be present");

            List<WebElement> scoreInputs = modal.findElements(By.xpath("//input[@name='score']"));
            for (WebElement scoreInput : scoreInputs) {
                scoreInput.clear();
                scoreInput.sendKeys("90");
            }

            WebElement saveButton = modal.findElement(By.id("saveButton"));
            saveButton.click();
            Thread.sleep(SLEEP_DURATION);

            WebElement messageElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h4")));
            String message = messageElement.getText();
            assertEquals("Grades saved", message);

            WebElement closeButton = modal.findElement(By.id("closeButton"));
            closeButton.click();
            Thread.sleep(SLEEP_DURATION);
        }
    }

}
