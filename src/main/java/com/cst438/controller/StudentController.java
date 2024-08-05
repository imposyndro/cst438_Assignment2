package com.cst438.controller;

import com.cst438.domain.*;
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
public class StudentController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private UserRepository userRepository;

    // Student gets transcript showing list of all enrollments
    @GetMapping("/transcripts")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public List<EnrollmentDTO> getTranscript(Principal principal) {
        String studentEmail = principal.getName();
        User student = userRepository.findByEmail(studentEmail);
        if (student == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
        }
        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsByStudentIdOrderByTermId(student.getId());
        List<EnrollmentDTO> enrollmentDTOs = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
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

    // Student gets a list of their enrollments for the given year, semester
    @GetMapping("/enrollments")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public List<EnrollmentDTO> getSchedule(
            @RequestParam("year") int year,
            @RequestParam("semester") String semester,
            Principal principal) {
        String studentEmail = principal.getName();
        User student = userRepository.findByEmail(studentEmail);
        if (student == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
        }
        List<Enrollment> enrollments = enrollmentRepository.findByYearAndSemesterOrderByCourseId(year, semester, student.getId());
        List<EnrollmentDTO> enrollmentDTOs = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
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

    // Student adds enrollment into a section
    @PostMapping("/enrollments/sections/{sectionNo}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public EnrollmentDTO addCourse(
            @PathVariable int sectionNo,
            Principal principal) {
        String studentEmail = principal.getName();
        User student = userRepository.findByEmail(studentEmail);
        if (student == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
        }

        Section section = sectionRepository.findById(sectionNo).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));

        var today = new java.util.Date();
        var addDate = new java.util.Date(section.getTerm().getAddDate().getTime());
        var addDeadline = new java.util.Date(section.getTerm().getAddDeadline().getTime());
        var outsideDateRange = today.before(addDate) || today.after(addDeadline);
        if (outsideDateRange) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot add a course before the add date or after the add deadline");
        }

        // Check that student is not already enrolled in this section
        var enrollment = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, student.getId());
        if (enrollment != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student is already enrolled in this section");
        }

        enrollment = new Enrollment();
        enrollment.setUser(student);
        enrollment.setSection(section);
        enrollmentRepository.save(enrollment);

        return new EnrollmentDTO(
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
        );
    }

    // Student drops a course
    @DeleteMapping("/enrollments/{enrollmentId}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public void dropCourse(@PathVariable("enrollmentId") int enrollmentId, Principal principal) {
        String studentEmail = principal.getName();
        User student = userRepository.findByEmail(studentEmail);
        if (student == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
        }

        Enrollment enrollment = enrollmentRepository.findEnrollmentByEnrollmentId(enrollmentId);
        if (enrollment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found");
        }

        if (enrollment.getUser().getId() != student.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only drop your own enrollments");
        }

        // Check that today is not after the drop deadline for the section
        var today = new java.util.Date();
        var deadline = new java.util.Date(enrollment.getSection().getTerm().getDropDeadline().getTime());
        var afterDropDeadline = today.after(deadline);
        if (afterDropDeadline) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The deadline to drop this enrollment has passed");
        }

        enrollmentRepository.delete(enrollment);
    }
}
