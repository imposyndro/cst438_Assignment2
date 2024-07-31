package com.cst438.service;

import com.cst438.dto.*;
import com.cst438.domain.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GradebookServiceProxy {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private UserRepository userRepository;

    Queue gradebookServiceQueue = new Queue("gradebook_service", true);

    @Bean
    public Queue createQueue() {
        return new Queue("registrar_service", true);
    }

    @Autowired
    RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "registrar_service")
    public void receiveFromRegistrar(String message) {
        try {
            MessageWrapper wrapper = fromJsonString(message, MessageWrapper.class);
            String action = wrapper.getAction();
            String payload = wrapper.getPayload();
            switch (action) {
                case "addCourse":
                case "updateCourse":
                case "deleteCourse":
                    handleCourseAction(action, payload);
                    break;
                case "addSection":
                case "updateSection":
                case "deleteSection":
                    handleSectionAction(action, payload);
                    break;
                case "addUser":
                case "updateUser":
                case "deleteUser":
                    handleUserAction(action, payload);
                    break;
                case "addEnrollment":
                case "deleteEnrollment":
                    handleEnrollmentAction(action, payload);
                    break;
                case "updateFinalGrade":
                    GradeDTO gradeDTO = fromJsonString(payload, GradeDTO.class);
                    updateFinalGrade(gradeDTO);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown action: " + action);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleCourseAction(String action, String payload) {
        CourseDTO dto = fromJsonString(payload, CourseDTO.class);
        Course course;
        switch (action) {
            case "addCourse":
                course = new Course();
                course.setCourseId(dto.courseId());
                course.setTitle(dto.title());
                course.setCredits(dto.credits());
                courseRepository.save(course);
                break;
            case "updateCourse":
                course = courseRepository.findById(dto.courseId()).orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
                course.setTitle(dto.title());
                course.setCredits(dto.credits());
                courseRepository.save(course);
                break;
            case "deleteCourse":
                courseRepository.deleteById(dto.courseId());
                break;
        }
    }

    private void handleSectionAction(String action, String payload) {
        SectionDTO dto = fromJsonString(payload, SectionDTO.class);
        Section section;
        switch (action) {
            case "addSection":
                section = new Section();
                section.setSectionNo(dto.secNo());
                section.setSecId(dto.secId());
                section.setBuilding(dto.building());
                section.setRoom(dto.room());
                section.setTimes(dto.times());
                section.setInstructor_email(dto.instructorEmail());

                Course course = courseRepository.findById(dto.courseId()).orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
                section.setCourse(course);

                Term term = termRepository.findByYearAndSemester(dto.year(), dto.semester());
                section.setTerm(term);

                sectionRepository.save(section);
                break;
            case "updateSection":
                section = sectionRepository.findById(dto.secNo()).orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));
                section.setSecId(dto.secId());
                section.setBuilding(dto.building());
                section.setRoom(dto.room());
                section.setTimes(dto.times());
                section.setInstructor_email(dto.instructorEmail());
                sectionRepository.save(section);
                break;
            case "deleteSection":
                sectionRepository.deleteById(dto.secNo());
                break;
        }
    }

    private void handleUserAction(String action, String payload) {
        UserDTO dto = fromJsonString(payload, UserDTO.class);
        User user;
        switch (action) {
            case "addUser":
                user = new User();
                user.setId(dto.id());
                user.setName(dto.name());
                user.setEmail(dto.email());
                user.setType(dto.type());
                userRepository.save(user);
                break;
            case "updateUser":
                user = userRepository.findById(dto.id()).orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
                user.setName(dto.name());
                user.setEmail(dto.email());
                user.setType(dto.type());
                userRepository.save(user);
                break;
            case "deleteUser":
                userRepository.deleteById(dto.id());
                break;
        }
    }

    private void handleEnrollmentAction(String action, String payload) {
        EnrollmentDTO dto = fromJsonString(payload, EnrollmentDTO.class);
        Enrollment enrollment;
        switch (action) {
            case "addEnrollment":
                enrollment = new Enrollment();
                enrollment.setEnrollmentId(dto.enrollmentId());

                Section section = sectionRepository.findById(dto.sectionNo()).orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));
                enrollment.setSection(section);

                User user = userRepository.findById(dto.studentId()).orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
                enrollment.setUser(user);

                enrollment.setGrade(dto.grade());
                enrollmentRepository.save(enrollment);
                break;
            case "deleteEnrollment":
                enrollmentRepository.deleteById(dto.enrollmentId());
                break;
        }
    }

    private void updateFinalGrade(GradeDTO dto) {
        Enrollment enrollment = enrollmentRepository.findById(dto.gradeId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));
        enrollment.setGrade(String.valueOf(dto.score()));
        enrollmentRepository.save(enrollment);
    }

    private void sendMessage(String action, Object obj) {
        MessageWrapper wrapper = new MessageWrapper(action, asJsonString(obj));
        rabbitTemplate.convertAndSend(gradebookServiceQueue.getName(), asJsonString(wrapper));
    }

    public void sendCourseUpdate(Course course, String action) {
        sendMessage(action, course);
    }

    public void sendSectionUpdate(Section section, String action) {
        sendMessage(action, section);
    }

    public void sendUserUpdate(User user, String action) {
        sendMessage(action, user);
    }

    public void sendEnrollmentUpdate(Enrollment enrollment, String action) {
        sendMessage(action, enrollment);
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T fromJsonString(String str, Class<T> valueType) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class MessageWrapper {
        private String action;
        private String payload;

        public MessageWrapper() {}

        public MessageWrapper(String action, String payload) {
            this.action = action;
            this.payload = payload;
        }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getPayload() { return payload; }
        public void setPayload(String payload) { this.payload = payload; }
    }
}