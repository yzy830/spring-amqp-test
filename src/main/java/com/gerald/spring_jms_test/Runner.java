package com.gerald.spring_jms_test;

import java.util.concurrent.TimeUnit;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class Runner implements CommandLineRunner {
    private final RabbitTemplate rabbitTemplate;
    
    private final Receiver receiver;
    
    private final ConfigurableApplicationContext context;
    
    @Autowired
    public Runner(RabbitTemplate rabbitTemplate, 
                  Receiver receiver, 
                  ConfigurableApplicationContext context) {
        this.rabbitTemplate = rabbitTemplate;
        this.receiver = receiver;
        this.context = context;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Sending Message...");
        rabbitTemplate.convertAndSend(App.queueName, "Hello from RabbitMQ!");
        receiver.getLatch().await(10, TimeUnit.SECONDS);
        context.close();
    }

}
