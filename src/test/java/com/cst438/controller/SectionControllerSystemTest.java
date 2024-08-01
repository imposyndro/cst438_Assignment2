package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class SectionControllerSystemTest {

    // TODO edit the following to give the location and file name
    // of the Chrome driver.
    //  for WinOS the file name will be chromedriver.exe
    //  for MacOS the file name will be chromedriver
    /*
    public static final String CHROME_DRIVER_FILE_LOCATION =
            "C:/chromedriver-win64/chromedriver.exe";
    */

    public static final String EDGE_DRIVER_FILE_LOCATION =
            "A:/csumb/software eng/week3/edgedriver_win64/msedgedriver.exe";

    //public static final String CHROME_DRIVER_FILE_LOCATION =
    //        "~/chromedriver_macOS/chromedriver";
    public static final String URL = "http://localhost:3000";

    public static final int SLEEP_DURATION = 1000; // 1 second.


    // add selenium dependency to pom.xml

    // these tests assume that test data does NOT contain any
    // sections for course cst499 in 2024 Spring term.

    WebDriver driver;

    @BeforeEach
    public void setUpDriver() throws Exception {

        // set properties required by Chrome Driver
        /*
        System.setProperty(
                "webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--remote-allow-origins=*");
        */

        // set properties required by Edge Driver
        System.setProperty("webdriver.edge.driver", EDGE_DRIVER_FILE_LOCATION);
        EdgeOptions ops = new EdgeOptions();
        ops.addArguments("--remote-allow-origins=*");

        // start the driver
        driver = new EdgeDriver(ops);
        //driver = new ChromeDriver(ops);

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

    /*
    // --- ADMIN TESTS START ---

    @Test
    public void systemTestAddSection() throws Exception {
        // add a section for cst499 Spring 2024 term
        // verify section shows on the list of sections for Spring 2024
        // delete the section
        // verify the section is gone


        // click link to navigate to Sections
        WebElement we = driver.findElement(By.id("sections"));
        we.click();
        Thread.sleep(SLEEP_DURATION);

        // enter cst499, 2024, Spring and click search sections
        driver.findElement(By.id("scourseId")).sendKeys("cst499");
        driver.findElement(By.id("syear")).sendKeys("2024");
        driver.findElement(By.id("ssemester")).sendKeys("Spring");
        driver.findElement(By.id("search")).click();
        Thread.sleep(SLEEP_DURATION);

        // verify that cst499 is not in the list of sections
        // if it exists, then delete it
        // Selenium throws NoSuchElementException when the element is not found
        try {
            while (true) {
                WebElement row499 = driver.findElement(By.xpath("//tr[td='cst499']"));
                List<WebElement> buttons = row499.findElements(By.tagName("button"));
                // delete is the second button
                assertEquals(2, buttons.size());
                buttons.get(1).click();
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

        // find and click button to add a section
        driver.findElement(By.id("addSection")).click();
        Thread.sleep(SLEEP_DURATION);

        // enter data
        //  courseId: cst499,
        driver.findElement(By.id("ecourseId")).sendKeys("cst499");
        //  secId: 1,
        driver.findElement(By.id("esecId")).sendKeys("1");
        //  year:2024,
        driver.findElement(By.id("eyear")).sendKeys("2024");
        //  semester:Spring,
        driver.findElement(By.id("esemester")).sendKeys("Spring");
        //  building:052,
        driver.findElement(By.id("ebuilding")).sendKeys("052");
        //  room:104,
        driver.findElement(By.id("eroom")).sendKeys("104");
        //  times:W F 1:00-2:50 pm,
        driver.findElement(By.id("etimes")).sendKeys("W F 1:00-2:50 pm");
        //  instructorEmail jgross@csumb.edu
        driver.findElement(By.id("einstructorEmail")).sendKeys("jgross@csumb.edu");
        // click Save
        driver.findElement(By.id("save")).click();
        Thread.sleep(SLEEP_DURATION);

        String message = driver.findElement(By.id("addMessage")).getText();
        assertTrue(message.startsWith("section added"));

        // close the dialog
        driver.findElement(By.id("close")).click();

        // verify that new Section shows up on Sections list
        // find the row for cst499
        WebElement row499 = driver.findElement(By.xpath("//tr[td='cst499']"));
        List<WebElement> buttons = row499.findElements(By.tagName("button"));
        // delete is the second button
        assertEquals(2, buttons.size());
        buttons.get(1).click();
        Thread.sleep(SLEEP_DURATION);
        // find the YES to confirm button
        List<WebElement> confirmButtons = driver
                .findElement(By.className("react-confirm-alert-button-group"))
                .findElements(By.tagName("button"));
        assertEquals(2, confirmButtons.size());
        confirmButtons.get(0).click();
        Thread.sleep(SLEEP_DURATION);

        // verify that Section list is now empty
        assertThrows(NoSuchElementException.class, () ->
                driver.findElement(By.xpath("//tr[td='cst499']")));

    }

    @Test
    public void systemTestAddSectionBadCourse() throws Exception {
        // attempt to add a section to course cst599 2024, Spring
        // fails because course does not exist
        // change courseId to cst499 and try again
        // verify success
        // delete the section

        // click link to navigate to Sections
        WebElement we = driver.findElement(By.id("sections"));
        we.click();
        Thread.sleep(SLEEP_DURATION);

        // enter cst, 2024, Spring and click search sections
        driver.findElement(By.id("scourseId")).sendKeys("cst");
        driver.findElement(By.id("syear")).sendKeys("2024");
        driver.findElement(By.id("ssemester")).sendKeys("Spring");
        driver.findElement(By.id("search")).click();
        Thread.sleep(SLEEP_DURATION);

        // verify that cst499 is not in the list of sections
        // Selenium throws NoSuchElementException when the element is not found
        try {
            while (true) {
                WebElement row499 = driver.findElement(By.xpath("//tr[td='cst499']"));
                List<WebElement> buttons = row499.findElements(By.tagName("button"));
                // delete is the second button
                assertEquals(2, buttons.size());
                buttons.get(1).click();
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

        // find and click button to add a section
        driver.findElement(By.id("addSection")).click();
        Thread.sleep(SLEEP_DURATION);

        // enter data
        //  courseId: cst599
        driver.findElement(By.id("ecourseId")).sendKeys("cst599");
        //  secId: 1,
        driver.findElement(By.id("esecId")).sendKeys("1");
        //  year:2024,
        driver.findElement(By.id("eyear")).sendKeys("2024");
        //  semester:Spring,
        driver.findElement(By.id("esemester")).sendKeys("Spring");
        //  building:052,
        driver.findElement(By.id("ebuilding")).sendKeys("052");
        //  room:104,
        driver.findElement(By.id("eroom")).sendKeys("104");
        //  times:W F 1:00-2:50 pm,
        driver.findElement(By.id("etimes")).sendKeys("W F 1:00-2:50 pm");
        //  instructorEmail jgross@csumb.edu
        driver.findElement(By.id("einstructorEmail")).sendKeys("jgross@csumb.edu");
        // click Save
        driver.findElement(By.id("save")).click();
        Thread.sleep(SLEEP_DURATION);

        WebElement msg = driver.findElement(By.id("addMessage"));
        String message = msg.getText();
        assertEquals("course not found cst599", message);

        // clear the courseId field and enter cst499
        WebElement courseId = driver.findElement(By.id("ecourseId"));
        courseId.sendKeys(Keys.chord(Keys.CONTROL,"a", Keys.DELETE));
        Thread.sleep(SLEEP_DURATION);
        courseId.sendKeys("cst499");
        driver.findElement(By.id("save")).click();
        Thread.sleep(SLEEP_DURATION);

        message = driver.findElement(By.id("addMessage")).getText();
        assertTrue(message.startsWith("section added"));

        // close the dialog
        driver.findElement(By.id("close")).click();
        Thread.sleep(SLEEP_DURATION);

        WebElement row = driver.findElement(By.xpath("//tr[td='cst499']"));
        assertNotNull(row);
        // find the delete button on the row from prior statement.
        List<WebElement> deleteButtons = row.findElements(By.tagName("button"));
        // delete is the second button
        assertEquals(2, deleteButtons.size());
        deleteButtons.get(1).click();
        Thread.sleep(SLEEP_DURATION);
        // find the YES to confirm button
        List<WebElement> confirmButtons = driver
                .findElement(By.className("react-confirm-alert-button-group"))
                .findElements(By.tagName("button"));
        assertEquals(2,confirmButtons.size());
        confirmButtons.get(0).click();
        Thread.sleep(SLEEP_DURATION);

        // verify that Section list is empty
        assertThrows(NoSuchElementException.class, () ->
                driver.findElement(By.xpath("//tr[td='cst499']")));
    }
    // --- ADMIN TESTS END ---
    */

    // --- INSTRUCTOR TESTS START ---
    // instructor adds a new assignment successfully
    @Test
    public void systemTestAddAssignment() throws Exception {
        // Navigate to Sections
        WebElement we = driver.findElement(By.id("sections"));
        we.click();
        Thread.sleep(SLEEP_DURATION);

        // Enter Year and Semester
        driver.findElement(By.id("year")).sendKeys("2024");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        Thread.sleep(SLEEP_DURATION);

        // Search table for specific course and course section
        // Update index value if found else catch exception
        int idx = -1;
        // Grab Table and populate a list with it's rows
        WebElement table = driver.findElement(By.xpath("//table[.//th[text()='SecNo'] and .//th[text()='CourseId']]"));
        WebElement tbody = table.findElement(By.tagName("tbody"));
        List<WebElement> rows = tbody.findElements(By.tagName("tr"));
        // Go through table for specific course section
        try {
            // Declare boolean for found row
            boolean found = false;

            for (int i = 0; i < rows.size() && !found; i++) {
                WebElement row = rows.get(i);

                // Grab every cell via column in the current row
                List<WebElement> cells = row.findElements(By.tagName("td"));

                // Failsafe check in case row has less than 3 cells
                if (cells.size() >= 3) {
                    WebElement secondCell = cells.get(1);
                    WebElement thirdCell = cells.get(2);
                    // Convert to string and convert it to lower case
                    String secondStr = secondCell.getText();
                    String thirdStr = thirdCell.getText();
                    secondStr = secondStr.toLowerCase();
                    thirdStr = thirdStr.toLowerCase();

                    // Check cell values
                    if (secondStr.equals("cst363")
                            && thirdStr.equals("2")) {
                        idx = i;
                        found = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Navigate to Assignments of row with desired values
        WebElement row = rows.get(idx);
        WebElement assignments = row.findElement(By.id("assignments"));
        assignments.click();
        Thread.sleep(SLEEP_DURATION);

        // Select instructor dropdown menu
        WebElement instDropdown = driver.findElement(By.xpath("//label[text()='Select Instructor']/following-sibling::div"));
        instDropdown.click();

        // Select instructor
        WebElement inst = driver.findElement(By.xpath("//li[text()='dwisneski@csumb.edu']"));
        inst.click();

        // Select section dropdown menu
        WebElement sectDropdown = driver.findElement(By.xpath("//label[text()='Select Section']/following-sibling::div"));
        sectDropdown.click();

        // Select section
        WebElement sect = driver.findElement(By.xpath("//li[text()='9']"));
        sect.click();

        // Press add assignment
        WebElement addButton = driver.findElement(By.xpath("//button[text()='Add Assignment']"));
        addButton.click();
        Thread.sleep(SLEEP_DURATION);

        // Add title and due date
        driver.findElement(By.id("title")).sendKeys("Extra Credit Assignment 1");
        driver.findElement(By.id("dueDate")).clear();
        driver.findElement(By.id("dueDate")).sendKeys("04-20-2024");

        // Click save
        driver.findElement(By.xpath("//button[text()='Save']")).click();
        Thread.sleep(SLEEP_DURATION);

        // Check if the page notifies you that an assignment was added successfully
        String h4AddText = driver.findElement(By.tagName("h4")).getText();
        h4AddText = h4AddText.toLowerCase();
        try {
            assertTrue(h4AddText.contains("add"),
                    "Page doesn't update notification message after adding an assignment");
        } catch (AssertionError e) {
            System.out.println(e.getMessage());
        }

        // Verify that the assignment has been added
        driver.findElement(By.id("home")).click();
        we = driver.findElement(By.id("sections"));
        we.click();
        Thread.sleep(SLEEP_DURATION);
        driver.findElement(By.id("year")).sendKeys("2024");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        Thread.sleep(SLEEP_DURATION);
        table = driver.findElement(By.xpath("//table[.//th[text()='SecNo'] and .//th[text()='CourseId']]"));
        tbody = table.findElement(By.tagName("tbody"));
        rows = tbody.findElements(By.tagName("tr"));
        row = rows.get(idx);
        assignments = row.findElement(By.id("assignments"));
        assignments.click();
        Thread.sleep(SLEEP_DURATION);
        instDropdown = driver.findElement(By.xpath("//label[text()='Select Instructor']/following-sibling::div"));
        instDropdown.click();
        inst = driver.findElement(By.xpath("//li[text()='dwisneski@csumb.edu']"));
        inst.click();
        sectDropdown = driver.findElement(By.xpath("//label[text()='Select Section']/following-sibling::div"));
        sectDropdown.click();
        sect = driver.findElement(By.xpath("//li[text()='9']"));
        sect.click();
        Thread.sleep(SLEEP_DURATION);
        // Grab all rows from assignment table
        List<WebElement> assignRows = driver.findElements(By.xpath("//table/tbody/tr"));
        // Search the table for Extra Credit Assignment 1 and 2024-07-04
        // Set boolean in case we find what we're searching for
        // Set boolean for confirming cleanup
        boolean exFound = false;
        boolean isDeleted = false;
        for (int i = 0; !exFound && i < assignRows.size(); i++){
            WebElement assignRow = assignRows.get(i);
            // Grab 2nd and 3rd elements in the row
            String title = assignRow.findElement(By.xpath("./td[2]")).getText();
            String date = assignRow.findElement(By.xpath("./td[3]")).getText();

            // Confirm if they match with what we're looking for
            if (title.equals("Extra Credit Assignment 1") && date.equals("04-20-2024")){
                exFound = true;

                // After the boolean was confirmed true, delete the assignment made during
                // delete the assignment made during the test
                WebElement deleteButton = assignRow.findElement(By.xpath(".//button[contains(text(), 'Delete')]"));
                deleteButton.click();
                isDeleted = true;

                // Check if the page notifies you that an assignment was deleted successfully
                String h4DelText = driver.findElement(By.tagName("h4")).getText();
                h4DelText = h4DelText.toLowerCase();
                try {
                    assertTrue(h4DelText.contains("delete"),
                            "Page doesn't update notification message after deleting an assignment");
                } catch (AssertionError e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        // If found, assert true otherwise state it wasn't found
        try {
        assertTrue(exFound, "Assignment not found.");
        } catch (AssertionError e) {
            System.out.println(e.getMessage());
        }

        // Clean up portion
        try {
        assertTrue(isDeleted, "Clean up test failed, Assignment was not deleted.");
        } catch (AssertionError e) {
            System.out.println(e.getMessage());
        }

        driver.findElement(By.id("home")).click();
    }
    // --- INSTRUCTOR TESTS END ---
}
