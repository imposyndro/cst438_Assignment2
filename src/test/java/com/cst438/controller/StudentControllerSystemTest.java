package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StudentControllerSystemTest {

    public static final String CHROME_DRIVER_FILE_LOCATION =
            "C:/Users/Aaron/OneDrive/Documents/School/CSUMB/Summer2024/CST438 Software Engineering/Week4/chromedriver.exe";

    public static final String URL = "http://localhost:3000";

    public static final int SLEEP_DURATION = 1000; // 1 second.


    // add selenium dependency to pom.xml

    // these tests assume that the studentId = 3, and that valid enrollments
    // for Fall 2024 CST363 and Fall 2024 CST438 are available to enroll into

    WebDriver driver;

    @BeforeEach
    public void setUpDriver() throws Exception {

        // set properties required by Chrome Driver
        System.setProperty(
                "webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--remote-allow-origins=*");

        // start the driver
        driver = new ChromeDriver(ops);

        driver.get(URL);
        // must have a short wait to allow time for the page to download
        Thread.sleep(SLEEP_DURATION);

    }

    @AfterEach
    public void terminateDriver() {
        if (driver != null) {
            // quit driver
            driver.close();
            driver.quit();
            driver = null;
        }
    }

    @Test
    public void systemTestAddEnrollment() throws Exception {
        // Click link to navigate to student schedule
        WebElement scheduleLink = driver.findElement(By.id("scheduleLink"));
        scheduleLink.click();
        Thread.sleep(SLEEP_DURATION);

        // Enter 2024 Fall and click search enrollments
        driver.findElement(By.id("syear")).sendKeys("2024");
        driver.findElement(By.id("ssemester")).sendKeys("Fall");
        driver.findElement(By.id("search")).click();
        Thread.sleep(SLEEP_DURATION);

        // Verify that there is no enrollment for Fall 2024 cst363
        // If there is, drop the enrollment
        try {
            while (true) {
                WebElement row363 = driver.findElement(By.xpath("//tr[td='cst363' and td='Fall']"));
                List<WebElement> buttons = row363.findElements(By.tagName("button"));
                assertEquals(1, buttons.size());
                buttons.get(0).click();
                Thread.sleep(SLEEP_DURATION);
                // find the YES to confirm button
                List<WebElement> confirmButtons = driver
                        .findElement(By.className("react-confirm-alert-button-group"))
                        .findElements(By.tagName("button"));
                assertEquals(2, confirmButtons.size());
                confirmButtons.get(0).click();
                Thread.sleep(SLEEP_DURATION);
            }
        } catch (NoSuchElementException e) {
            // do nothing, continue with test
        }

        // Click link to navigate to add course enrollment
        WebElement enrollLink = driver.findElement(By.id("enrollLink"));
        enrollLink.click();
        Thread.sleep(SLEEP_DURATION);

        // Enroll in Fall 2024 cst363
        WebElement row363 = driver.findElement(By.xpath("//tr[td='cst363' and td='Fall']"));
        List<WebElement> buttons = row363.findElements(By.tagName("button"));
        assertEquals(1, buttons.size());
        buttons.get(0).click();
        Thread.sleep(SLEEP_DURATION);

        // Find the YES to confirm button
        List<WebElement> confirmButtons = driver
                .findElement(By.className("react-confirm-alert-button-group"))
                .findElements(By.tagName("button"));
        assertEquals(2, confirmButtons.size());
        confirmButtons.get(0).click();
        Thread.sleep(SLEEP_DURATION);

        // Assert message displayed indicates successful enrollment
        String message = driver.findElement(By.id("message")).getText();
        assertEquals("Enrolled in course", message);

        // Navigate to transcripts view to confirm the new course has been added
        scheduleLink.click();
        Thread.sleep(SLEEP_DURATION);

        // Enter 2024 Fall and click search enrollments
        driver.findElement(By.id("syear")).sendKeys("2024");
        driver.findElement(By.id("ssemester")).sendKeys("Fall");
        driver.findElement(By.id("search")).click();
        Thread.sleep(SLEEP_DURATION);

        // Verify that enrollment shows up in the schedule view
        assertDoesNotThrow(() ->
                driver.findElement(By.xpath("//tr[td='cst363' and td='Fall']")));
    }

   @Test
    public void systemTestAddEnrollmentFailsWhenAlreadyEnrolled() throws Exception {
       // Initialize variables for later use
       WebElement row438;
       List<WebElement> buttons;
       List<WebElement> confirmButtons;
       String message;
       // attempt to enroll in CST438 for Fall 2024 when an enrollment is already present
       // click link to navigate to student schedule


       WebElement enrollLink = driver.findElement(By.id("enrollLink"));
       WebElement scheduleLink = driver.findElement(By.id("scheduleLink"));
       scheduleLink.click();
       Thread.sleep(SLEEP_DURATION);

       // Enter 2024 Fall and click search enrollments
       driver.findElement(By.id("syear")).sendKeys("2024");
       driver.findElement(By.id("ssemester")).sendKeys("Fall");
       driver.findElement(By.id("search")).click();
       Thread.sleep(SLEEP_DURATION);

       // Verify that there is an enrollment for Fall 2024 cst363
       // If there is not one, add it
       try {
           row438 = driver.findElement(By.xpath("//tr[td='cst438']"));
       } catch (NoSuchElementException e) {
           // If exception is thrown, the enrollment in cst438 is missing and we need to add it
           enrollLink.click();
           Thread.sleep(SLEEP_DURATION);
           // Enroll in Fall 2024 cst438
           row438 = driver.findElement(By.xpath("//tr[td='cst438']"));
           buttons = row438.findElements(By.tagName("button"));
           assertEquals(1, buttons.size());
           buttons.get(0).click();
           Thread.sleep(SLEEP_DURATION);

           // find the YES to confirm button
           confirmButtons = driver
                   .findElement(By.className("react-confirm-alert-button-group"))
                   .findElements(By.tagName("button"));
           assertEquals(2, confirmButtons.size());
           confirmButtons.get(0).click();
           Thread.sleep(SLEEP_DURATION);

           // Assert message displayed indicates successful enrollment
           message = driver.findElement(By.id("message")).getText();
           assertEquals("Enrolled in course", message);
       }

       // Click link to navigate to enrollment view
       enrollLink.click();
       Thread.sleep(SLEEP_DURATION);

       // Attempt to enroll in Fall 2024 cst363
       row438 = driver.findElement(By.xpath("//tr[td='cst438']"));
       buttons = row438.findElements(By.tagName("button"));
       assertEquals(1, buttons.size());
       buttons.get(0).click();
       Thread.sleep(SLEEP_DURATION);

       // find the YES to confirm button
       confirmButtons = driver
               .findElement(By.className("react-confirm-alert-button-group"))
               .findElements(By.tagName("button"));
       assertEquals(2, confirmButtons.size());
       confirmButtons.get(0).click();
       Thread.sleep(SLEEP_DURATION);

       // Assert message displayed indicates student is already enrolled in the section
       message = driver.findElement(By.id("message")).getText();
       assertEquals("student is already enrolled in this section", message);
    }
}
