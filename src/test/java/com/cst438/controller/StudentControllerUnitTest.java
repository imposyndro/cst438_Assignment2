package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.dto.SectionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Unit tests for when a student enrolls in a section
 */

@AutoConfigureMockMvc
@SpringBootTest
public class StudentControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Test
    public void addCourseEnrollsStudentInSection() throws Exception {
        MockHttpServletResponse response;

        // issue a http POST request to SpringTestServer
        // request creation of enrollment in section with secNo 9 for student with studentId=3
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/enrollments/sections/9?studentId=3"))
                        .andReturn()
                        .getResponse();

        // check the response code for 200 meaning OK
        assertEquals(200, response.getStatus());

        // return data converted from String to DTO
        EnrollmentDTO result = fromJsonString(response.getContentAsString(), EnrollmentDTO.class);

        // primary key should have a non-zero value from the database
        assertNotEquals(0, result.enrollmentId());
        // check other fields of the DTO for expected values
        assertEquals(9, result.sectionNo());
        assertEquals(3, result.studentId());

        // check the database
        Enrollment e = enrollmentRepository.findById(result.enrollmentId()).orElse(null);
        assertNotNull(e);
        assertEquals(9, e.getSection().getSectionNo());

        // clean up after test. issue http DELETE request for enrollment
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/enrollments/"+result.enrollmentId()))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());

        // check database for delete
        e = enrollmentRepository.findById(result.enrollmentId()).orElse(null);
        assertNull(e);  // section should not be found after delete
    }

    @Test
    public void addCourseFailsWhenStudentAlreadyEnrolled( ) throws Exception {
        MockHttpServletResponse response;

        // issue a http POST request to SpringTestServer
        // request creation of enrollment in section with secNo 10 for student with studentId=3
        // this should fail as data.sql creates an enrollment for this student in this section
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/enrollments/sections/10?studentId=3"))
                .andReturn()
                .getResponse();

        // response should be 400, BAD_REQUEST
        assertEquals(400, response.getStatus());

        // check the expected error message
        String message = response.getErrorMessage();
        assertEquals("student is already enrolled in this section", message);

    }

    @Test
    public void addCourseFailsWhenSectionNoInvalid( ) throws Exception {
        MockHttpServletResponse response;

        // issue a http POST request to SpringTestServer
        // request creation of enrollment in section with secNo 20 for student with studentId=3
        // this should fail as this section should not exist
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/enrollments/sections/20?studentId=3"))
                .andReturn()
                .getResponse();

        // response should be 404, NOT_FOUND
        assertEquals(404, response.getStatus());

        // check the expected error message
        String message = response.getErrorMessage();
        assertEquals("Section not found", message);
    }

    @Test
    public void addCourseFailsWhenPastAddDeadline( ) throws Exception {
        MockHttpServletResponse response;

        // issue a http POST request to SpringTestServer
        // request creation of enrollment in section with secNo 8 for student with studentId=3
        // this should fail as the add deadline for this section has passed
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/enrollments/sections/8?studentId=3"))
                .andReturn()
                .getResponse();

        // response should be 400, BAD_REQUEST
        assertEquals(400, response.getStatus());

        // check the expected error message
        String message = response.getErrorMessage();
        assertEquals("cannot add a course before the add date or after the add deadline", message);
    }

    private static <T> T  fromJsonString(String str, Class<T> valueType ) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
