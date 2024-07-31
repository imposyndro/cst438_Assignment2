package com.cst438.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class GradebookServiceProxy {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend("gradebookQueue", message);
    }

    @RabbitListener(queues = "gradebookQueue")
    public void receiveMessage(String message) {
        try {
            // Process the message and update the Enrollment entity
            // Example: Update enrollment with the received grade
            System.out.println("Received message: " + message);
        } catch (Exception e) {
            // Handle the exception to avoid an infinite loop
            System.err.println("Failed to process message: " + e.getMessage());
        }
    }
}