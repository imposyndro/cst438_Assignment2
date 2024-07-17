package com.cst438.controller;

import com.cst438.dto.EnrollmentDTO;
import com.cst438.dto.GradeDTO;
import com.cst438.dto.UserDTO;
import com.cst438.domain.Course;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Section;
import com.cst438.domain.Term;
import com.cst438.domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class AssignmentControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    EnrollmentRepository enrollmentRepository;

    private static boolean setUpIsDone = false;

    @BeforeEach
    public void setup() throws Exception {
        if (!setUpIsDone) {
            mvc.perform(MockMvcRequestBuilders.delete("/clear"))
                    .andReturn()
                    .getResponse();

            UserDTO student1 = new UserDTO(0, "Student One", "student1@example.com", "STUDENT");
            mvc.perform(
                            MockMvcRequestBuilders
                                    .post("/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(asJsonString(student1)))
                    .andReturn()
                    .getResponse();

            UserDTO student2 = new UserDTO(0, "Student Two", "student2@example.com", "STUDENT");
            mvc.perform(
                            MockMvcRequestBuilders
                                    .post("/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(asJsonString(student2)))
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
            updatedGrades[i] = new GradeDTO(grade.gradeId(), grade.studentName(), grade.studentEmail(), grade.assignmentTitle(), grade.courseId(), grade.sectionId(), grade.score() + 5);
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


        Term term = new Term();
        term.setYear(2024);
        term.setSemester("Fall");

        Course course = new Course();
        course.setCourseId("CST438");
        course.setTitle("Course Title");
        course.setCredits(3);

        Section section = new Section();
        section.setSectionNo(1);
        section.setTerm(term);
        section.setCourse(course);

        User studentOne = new User();
        studentOne.setId(1);
        studentOne.setName("Student One");
        studentOne.setEmail("student1@example.com");

        User studentTwo = new User();
        studentTwo.setId(2);
        studentTwo.setName("Student Two");
        studentTwo.setEmail("student2@example.com");

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setEnrollmentId(1);
        enrollment1.setSection(section);
        enrollment1.setUser(studentOne);

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setEnrollmentId(2);
        enrollment2.setSection(section);
        enrollment2.setUser(studentTwo);

        List<Enrollment> enrollments = List.of(enrollment1, enrollment2);

        when(enrollmentRepository.findEnrollmentsBySectionNoOrderByStudentName(1))
                .thenReturn(enrollments);

        when(enrollmentRepository.findById(1)).thenReturn(Optional.of(enrollment1));
        when(enrollmentRepository.findById(2)).thenReturn(Optional.of(enrollment2));


        response = mvc.perform(
                        get("/sections/1/enrollments")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        EnrollmentDTO[] returnedEnrollments = fromJsonString(response.getContentAsString(), EnrollmentDTO[].class);
        assertEquals(2, returnedEnrollments.length);
        assertEquals("Student One", returnedEnrollments[0].name());
        assertEquals("Student Two", returnedEnrollments[1].name());


        returnedEnrollments[0] = new EnrollmentDTO(
                returnedEnrollments[0].enrollmentId(),
                "A",
                returnedEnrollments[0].studentId(),
                returnedEnrollments[0].name(),
                returnedEnrollments[0].email(),
                returnedEnrollments[0].courseId(),
                returnedEnrollments[0].title(),
                returnedEnrollments[0].sectionId(),
                returnedEnrollments[0].sectionNo(),
                returnedEnrollments[0].building(),
                returnedEnrollments[0].room(),
                returnedEnrollments[0].times(),
                returnedEnrollments[0].credits(),
                returnedEnrollments[0].year(),
                returnedEnrollments[0].semester()
        );

        returnedEnrollments[1] = new EnrollmentDTO(
                returnedEnrollments[1].enrollmentId(),
                "B",
                returnedEnrollments[1].studentId(),
                returnedEnrollments[1].name(),
                returnedEnrollments[1].email(),
                returnedEnrollments[1].courseId(),
                returnedEnrollments[1].title(),
                returnedEnrollments[1].sectionId(),
                returnedEnrollments[1].sectionNo(),
                returnedEnrollments[1].building(),
                returnedEnrollments[1].room(),
                returnedEnrollments[1].times(),
                returnedEnrollments[1].credits(),
                returnedEnrollments[1].year(),
                returnedEnrollments[1].semester()
        );


        response = mvc.perform(
                        put("/enrollments")
                                .content(asJsonString(List.of(returnedEnrollments)))
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
