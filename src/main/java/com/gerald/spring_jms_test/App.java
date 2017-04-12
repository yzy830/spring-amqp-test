package com.gerald.spring_jms_test;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory.CacheMode;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

@SpringBootApplication
public class App {
    final static String queueName = "spring-boot";
    
    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        
        connectionFactory.setAddresses("localhost:5672");
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        connectionFactory.setCacheMode(CacheMode.CHANNEL);  
        connectionFactory.setConnectionNameStrategy(cf -> "SHOP_APP");
        
        return connectionFactory;
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate();
        
        //TODO: RetryTemplate和RecoverCallback，还需要测试一下
        RetryTemplate retry = new RetryTemplate();
        ExponentialBackOffPolicy policy = new ExponentialBackOffPolicy();
        policy.setInitialInterval(500);
        policy.setMaxInterval(10000);
        policy.setMultiplier(10);
        retry.setBackOffPolicy(policy);
        
        template.setRetryTemplate(retry);
        
        return template;
    }
    
    @Bean
    public Queue queue() {
        return new Queue(queueName);
    }
    
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange("spring-boot-exchange");
    }
    
    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(queueName);
    }
    
    /**
     * 使用一个自定义的Receiver对象，创建了MessageListenerAdapter，并指定"receiveMessage"
     * 方法来处理消息。需要Adapter是因为Message Listener Container对Message Listener有要求
     * 
     * @param receiver
     * @return
     */
    @Bean
    public MessageListenerAdapter listenerAdapter(Receiver receiver) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(receiver, "receiveMessage");
        
        return adapter;
    }
    
    @Bean
    public SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
                                                    MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName);
        container.setMessageListener(listenerAdapter);
        
        return container;
    }
    
    public static void main( String[] args ) {
        new SpringApplicationBuilder(App.class).web(true).run(args);
    }
}
