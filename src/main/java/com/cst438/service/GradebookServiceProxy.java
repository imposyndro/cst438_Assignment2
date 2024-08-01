package com.cst438.service;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.dto.EnrollmentDTO;
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
    private EnrollmentRepository enrollmentRepository;

    Queue gradebookServiceQueue = new Queue("gradebook_service", true);

    @Bean
    public Queue createQueue() {
        return new Queue("registrar_service", true);
    }

    @Autowired
    RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "registrar_service")
    public void receiveMessage(String message) {
        try {
            String[] parts = message.split(" ", 2);
            String action = parts[0];
            if (action.equals("updateFinalGrade")) {
                EnrollmentDTO dto = fromJsonString(parts[1], EnrollmentDTO.class);
                Enrollment enrollment = enrollmentRepository.findById(dto.enrollmentId()).orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));
                enrollment.setGrade(String.valueOf(dto.grade()));
                enrollmentRepository.save(enrollment);
            }
            else {
                throw new IllegalArgumentException("Unknown action: " + action);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

    public void sendMessage(String s) {
        rabbitTemplate.convertAndSend(gradebookServiceQueue.getName(), s);
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