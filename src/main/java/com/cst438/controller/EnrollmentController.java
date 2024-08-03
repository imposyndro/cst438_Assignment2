package com.cst438.controller;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.UserRepository;
import com.cst438.dto.EnrollmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private UserRepository userRepository;

    // Instructor downloads student enrollments for a section, ordered by student name
    // User must be instructor for the section
    @GetMapping("/sections/{sectionNo}/enrollments")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
    public List<EnrollmentDTO> getEnrollments(
            @PathVariable("sectionNo") int sectionNo,
            Principal principal) {

        String instructorEmail = principal.getName();
        Section section = sectionRepository.findById(sectionNo).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));

        if (!section.getInstructorEmail().equals(instructorEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You must be logged in as the instructor for this section to view enrollments");
        }

        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsBySectionNoOrderByStudentName(sectionNo);
        List<EnrollmentDTO> enrollmentDTOs = new ArrayList<>();

        for (Enrollment enrollment : enrollments) {
            if (enrollment.getSection().getTerm() == null
                    || enrollment.getSection().getTerm().getYear() < 2000
                    || enrollment.getSection().getTerm().getYear() > 2100
                    || enrollment.getSection().getTerm().getSemester() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Enrollment has incorrect year or no set semester."
                );
            }
            enrollmentDTOs.add(new EnrollmentDTO(
                    enrollment.getEnrollmentId(),
                    enrollment.getGrade(),
                    enrollment.getUser().getId(),
                    enrollment.getUser().getName(),
                    enrollment.getUser().getEmail(),
                    enrollment.getSection().getCourse().getCourseId(),
                    enrollment.getSection().getCourse().getTitle(),
                    enrollment.getSection().getSecId(),
                    enrollment.getSection().getSectionNo(),
                    enrollment.getSection().getBuilding(),
                    enrollment.getSection().getRoom(),
                    enrollment.getSection().getTimes(),
                    enrollment.getSection().getCourse().getCredits(),
                    enrollment.getSection().getTerm().getYear(),
                    enrollment.getSection().getTerm().getSemester()
            ));
        }
        return enrollmentDTOs;
    }

    // Instructor uploads enrollments with the final grades for the section
    // User must be instructor for the section
    @PutMapping
    @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
    public void updateEnrollmentGrade(
            @RequestBody List<EnrollmentDTO> dlist,
            Principal principal) {

        String instructorEmail = principal.getName();

        for (EnrollmentDTO dto : dlist) {
            Enrollment enrollment = enrollmentRepository.findById(dto.enrollmentId()).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));

            if (!enrollment.getSection().getInstructorEmail().equals(instructorEmail)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You must be logged in as the instructor for this section to update grades");
            }

            enrollment.setGrade(dto.grade());
            enrollmentRepository.save(enrollment);
        }
    }
}
