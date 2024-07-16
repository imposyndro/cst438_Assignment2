package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.AssignmentStudentDTO;
import com.cst438.dto.GradeDTO;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
 * Unit tests for AssignmentController
 */

@AutoConfigureMockMvc
@SpringBootTest
public class AssignmentControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    GradeRepository gradeRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    TermRepository termRepository;

    @BeforeEach
    public void setup() {
        // Clean up any existing data to avoid conflicts
        gradeRepository.deleteAll();
        enrollmentRepository.deleteAll();
        assignmentRepository.deleteAll();
        sectionRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();
        termRepository.deleteAll();

        // Setup test data
        User student1 = new User();
        student1.setEmail("student1@example.com");
        student1.setName("Student One");
        student1.setPassword("password");
        student1.setType("STUDENT");
        userRepository.save(student1);

        User student2 = new User();
        student2.setEmail("student2@example.com");
        student2.setName("Student Two");
        student2.setPassword("password");
        student2.setType("STUDENT");
        userRepository.save(student2);

        Course course = new Course();
        course.setCourseId("CST438");
        course.setTitle("Software Engineering");
        course.setCredits(3);
        courseRepository.save(course);

        Term term = new Term();
        term.setTermId(1);
        term.setYear(2023);
        term.setSemester("Fall");
        term.setAddDate(new java.sql.Date(System.currentTimeMillis()));
        term.setAddDeadline(new java.sql.Date(System.currentTimeMillis()));
        term.setDropDeadline(new java.sql.Date(System.currentTimeMillis()));
        term.setStartDate(new java.sql.Date(System.currentTimeMillis()));
        term.setEndDate(new java.sql.Date(System.currentTimeMillis()));
        termRepository.save(term);

        Section section = new Section();
        section.setCourse(course);
        section.setTerm(term);
        section.setSecId(1);
        section.setBuilding("Building 1");
        section.setRoom("101");
        section.setTimes("MWF 10-11am");
        section.setInstructor_email("instructor@example.com");
        sectionRepository.save(section);

        Assignment assignment = new Assignment();
        assignment.setTitle("Assignment One");
        assignment.setDueDate(new java.sql.Date(System.currentTimeMillis()));
        assignment.setSection(section);
        assignmentRepository.save(assignment);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setUser(student1);
        enrollment1.setSection(section);
        enrollmentRepository.save(enrollment1);

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setUser(student2);
        enrollment2.setSection(section);
        enrollmentRepository.save(enrollment2);

        Grade grade1 = new Grade();
        grade1.setAssignment(assignment);
        grade1.setEnrollment(enrollment1);
        grade1.setScore(95);
        gradeRepository.save(grade1);

        Grade grade2 = new Grade();
        grade2.setAssignment(assignment);
        grade2.setEnrollment(enrollment2);
        grade2.setScore(85);
        gradeRepository.save(grade2);
    }

    @Test
    public void gradeAssignmentSuccess() throws Exception {
        MockHttpServletResponse response;

        // Fetch the grades for the assignment
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .get("/assignments/1/grades")
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus(), "Expected status 200 but got " + response.getStatus());

        // Deserialize the response to a list of GradeDTO
        GradeDTO[] grades = fromJsonString(response.getContentAsString(), GradeDTO[].class);

        // Update the grades by creating new GradeDTO instances with updated scores
        GradeDTO[] updatedGrades = new GradeDTO[grades.length];
        for (int i = 0; i < grades.length; i++) {
            GradeDTO grade = grades[i];
            updatedGrades[i] = new GradeDTO(grade.gradeId(), grade.studentName(), grade.studentEmail(), grade.assignmentTitle(), grade.courseId(), grade.sectionId(), grade.score() + 5);
        }

        // Send the updated grades
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

        List<AssignmentStudentDTO> finalGrades = List.of(
                new AssignmentStudentDTO(1, "Final Grade", new java.sql.Date(System.currentTimeMillis()), "CST438", 1, 95),
                new AssignmentStudentDTO(2, "Final Grade", new java.sql.Date(System.currentTimeMillis()), "CST438", 1, 85)
        );

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .put("/final-grades")
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
