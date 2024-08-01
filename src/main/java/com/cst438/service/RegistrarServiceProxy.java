package com.cst438.service;

import com.cst438.domain.*;
import com.cst438.dto.CourseDTO;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.dto.SectionDTO;
import com.cst438.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class RegistrarServiceProxy {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    Queue registrarServiceQueue = new Queue("registrar_service", true);

    @Bean
    public Queue createQueue() {
        return new Queue("gradebook_service", true);
    }

    @Autowired
    RabbitTemplate rabbitTemplate;

    public void updateFinalGrade(EnrollmentDTO e) {
        sendMessage("updateFinalGrade " + asJsonString(e));
    }

    @RabbitListener(queues = "gradebook_service")
    public void receiveFromRegistrar(String message)  {
        try {
            String[] parts = message.split(" ", 2);
            String action = parts[0];
            if (action.equals("addCourse")) {
                CourseDTO dto = fromJsonString(parts[1], CourseDTO.class);
                Course c = new Course();
                c.setCourseId(dto.courseId());
                c.setTitle(dto.title());
                c.setCredits(dto.credits());
                courseRepository.save(c);
            } else if (action.equals("deleteCourse")) {
                courseRepository.deleteById(parts[1]);
            } else if (action.equals("updateCourse")) {
                CourseDTO dto = fromJsonString(parts[1], CourseDTO.class);
                Course c = courseRepository.findById(dto.courseId()).orElse(null);
                if (c == null)
                {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found " + dto.courseId());
                }
                c.setTitle(dto.title());
                c.setCredits(dto.credits());
                courseRepository.save(c);
            } else if (action.equals("addSection")) {
                SectionDTO dto = fromJsonString(parts[1], SectionDTO.class);
                Course course = courseRepository.findById(dto.courseId()).orElse(null);
                if (course == null)
                {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found " + dto.courseId());
                }
                Section s = new Section();
                s.setCourse(course);
                Term term = termRepository.findByYearAndSemester(dto.year(), dto.semester());
                if (term == null)
                {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Term not found for year " + dto.year() + " and semester " + dto.semester());
                }
                s.setTerm(term);
                s.setSecId(dto.secId());
                s.setBuilding(dto.building());
                s.setRoom(dto.room());
                s.setTimes(dto.times());
                User instructor = null;
                if (dto.instructorEmail()==null || dto.instructorEmail().equals("")) {
                    s.setInstructor_email("");
                } else {
                    instructor = userRepository.findByEmail(dto.instructorEmail());
                    if (instructor == null || !instructor.getType().equals("INSTRUCTOR")) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "email not found or not an instructor " + dto.instructorEmail());
                    }
                    s.setInstructor_email(dto.instructorEmail());
                }
                sectionRepository.save(s);
            } else if (action.equals("deleteSection")) {
                Section s = sectionRepository.findById(Integer.parseInt(parts[1])).orElse(null);
                if (s != null) {
                    sectionRepository.delete(s);
                }
            } else if (action.equals("updateSection")) {
                SectionDTO dto = fromJsonString(parts[1], SectionDTO.class);
                Section s = sectionRepository.findById(dto.secNo()).orElse(null);
                if (s==null) {
                    throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "section not found "+dto.secNo());
                }
                s.setSecId(dto.secId());
                s.setBuilding(dto.building());
                s.setRoom(dto.room());
                s.setTimes(dto.times());

                User instructor = null;
                if (dto.instructorEmail()==null || dto.instructorEmail().equals("")) {
                    s.setInstructor_email("");
                } else {
                    instructor = userRepository.findByEmail(dto.instructorEmail());
                    if (instructor == null || !instructor.getType().equals("INSTRUCTOR")) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "email not found or not an instructor " + dto.instructorEmail());
                    }
                    s.setInstructor_email(dto.instructorEmail());
                }
                sectionRepository.save(s);
            } else if (action.equals("addUser")) {
                UserDTO dto = fromJsonString(parts[1], UserDTO.class);
                User user = new User();
                user.setName(dto.name());
                user.setEmail(dto.email());

                // create password and encrypt it
                String password = dto.name()+"2024";
                String enc_password = encoder.encode(password);
                user.setPassword(enc_password);

                user.setType(dto.type());
                if (!dto.type().equals("STUDENT") &&
                        !dto.type().equals("INSTRUCTOR") &&
                        !dto.type().equals("ADMIN")) {
                    // invalid type
                    throw  new ResponseStatusException( HttpStatus.BAD_REQUEST, "invalid user type");
                }
                userRepository.save(user);
            } else if (action.equals("deleteUser")) {
                User user = userRepository.findById(Integer.parseInt(parts[1])).orElse(null);
                if (user!=null) {
                    userRepository.delete(user);
                }
            } else if (action.equals("updateUser")) {
                UserDTO dto = fromJsonString(parts[1], UserDTO.class);
                User user = userRepository.findById(dto.id()).orElse(null);
                if (user==null) {
                    throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "user id not found");
                }
                user.setName(dto.name());
                user.setEmail(dto.email());
                user.setType(dto.type());
                if (!dto.type().equals("STUDENT") &&
                        !dto.type().equals("INSTRUCTOR") &&
                        !dto.type().equals("ADMIN")) {
                    // invalid type
                    throw  new ResponseStatusException( HttpStatus.BAD_REQUEST, "invalid user type");
                }
                userRepository.save(user);
            } else if (action.equals("addEnrollment")) {
                Enrollment enrollment = fromJsonString(parts[1], Enrollment.class);
                enrollmentRepository.save(enrollment);
            } else if (action.equals("deleteEnrollment")) {
                enrollmentRepository.deleteById(Integer.parseInt(parts[1]));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

    }


    private void sendMessage(String s) {
        rabbitTemplate.convertAndSend(registrarServiceQueue.getName(), s);
    }
    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static <T> T  fromJsonString(String str, Class<T> valueType ) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}