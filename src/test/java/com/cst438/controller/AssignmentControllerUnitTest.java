package com.cst438.controller;

import com.cst438.dto.AssignmentStudentDTO;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.dto.GradeDTO;
import com.cst438.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureMockMvc
@SpringBootTest
public class AssignmentControllerUnitTest {

    @Autowired
    MockMvc mvc;

    private static boolean setUpIsDone = false;

    @BeforeEach
    public void setup() throws Exception {
        if (!setUpIsDone) {
            mvc.perform(MockMvcRequestBuilders.delete("/clear"))
                    .andReturn()
                    .getResponse();

            // Create user for gradeAssignmentSuccess
            UserDTO student1 = new UserDTO(0, "Student One", "student1@example.com", "STUDENT");
            mvc.perform(
                            MockMvcRequestBuilders
                                    .post("/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(asJsonString(student1)))
                    .andReturn()
                    .getResponse();

            // Create user for gradeAssignmentInvalidAssignmentId
            UserDTO student2 = new UserDTO(0, "Student Two", "student2@example.com", "STUDENT");
            mvc.perform(
                            MockMvcRequestBuilders
                                    .post("/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(asJsonString(student2)))
                    .andReturn()
                    .getResponse();

            // Create user for enterFinalGradesSuccess
            UserDTO student3 = new UserDTO(0, "Student Three", "student3@example.com", "STUDENT");
            mvc.perform(
                            MockMvcRequestBuilders
                                    .post("/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(asJsonString(student3)))
                    .andReturn()
                    .getResponse();

            setUpIsDone = true;
        }
    }

    @Test
    public void gradeAssignmentSuccess() throws Exception {
        MockHttpServletResponse response = mvc.perform(
                        MockMvcRequestBuilders
                                .get("/assignments/1/grades")
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus(), "Expected status 200 but got " + response.getStatus());

        GradeDTO[] grades = fromJsonString(response.getContentAsString(), GradeDTO[].class);
        GradeDTO[] updatedGrades = new GradeDTO[grades.length];
        for (int i = 0; i < grades.length; i++) {
            GradeDTO grade = grades[i];
            updatedGrades[i] = new GradeDTO(grade.gradeId(), grade.studentName(), grade.studentEmail(), grade.assignmentTitle(), grade.courseId(), grade.sectionId(), grade.score() + 5); // Example modification
        }

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .put("/grades")
                                .content(asJsonString(updatedGrades))
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus(), "Expected status 200 but got " + response.getStatus());
    }

    @Test
    public void gradeAssignmentInvalidAssignmentId() throws Exception {
        MockHttpServletResponse response;

        List<GradeDTO> grades = List.of(
                new GradeDTO(9999, "Student One", "student1@example.com", "Assignment One", "CST438", 1, 95),
                new GradeDTO(9999, "Student Two", "student2@example.com", "Assignment One", "CST438", 1, 85)
        );

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .put("/grades")
                                .content(asJsonString(grades))
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        assertEquals(404, response.getStatus());
    }

    @Test
    public void enterFinalGradesSuccess() throws Exception {
        MockHttpServletResponse response;

        List<EnrollmentDTO> finalGrades = List.of(
                new EnrollmentDTO(1, "A", 1, "Student One", "student1@example.com", "CST438", "Course Title", 1, 1, "Building", "Room", "Times", 3, 2024, "Fall"),
                new EnrollmentDTO(2, "B", 2, "Student Two", "student2@example.com", "CST438", "Course Title", 1, 1, "Building", "Room", "Times", 3, 2024, "Fall")
        );

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .put("/enrollments")
                                .content(asJsonString(finalGrades))
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());
    }

    private static <T> T fromJsonString(String str, Class<T> valueType) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
