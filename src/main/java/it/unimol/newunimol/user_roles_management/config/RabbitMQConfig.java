package it.unimol.newunimol.user_roles_management.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Value("${rabbitmq.exchange.users}")
    private String usersExchange;

    @Value("${rabbitmq.queue.user.created}")
    private String userCreatedQueue;

    @Value("${rabbitmq.queue.user.updated}")
    private String userUpdatedQueue;

    @Value("${rabbitmq.queue.user.deleted}")
    private String userDeletedQueue;

    @Value("${rabbitmq.queue.role.assigned}")
    private String roleAssignedQueue;

    @Bean
    public TopicExchange usersExchange() {
        return new TopicExchange(usersExchange);
    }

    @Bean
    public Queue userCreatedQueue() {
        return QueueBuilder.durable(userCreatedQueue).build();
    }

    @Bean
    public Queue userUpdatedQueue() {
        return QueueBuilder.durable(userUpdatedQueue).build();
    }

    @Bean
    public Queue userDeletedQueue() {
        return QueueBuilder.durable(userDeletedQueue).build();
    }

    @Bean
    public Queue roleAssignedQueue() {
        return QueueBuilder.durable(roleAssignedQueue).build();
    }

    @Bean
    public Binding userCreatedBinding() {
        return BindingBuilder.bind(userCreatedQueue())
                .to(usersExchange())
                .with("user.created");
    }

    @Bean
    public Binding userUpdatedBinding() {
        return BindingBuilder.bind(userUpdatedQueue())
                .to(usersExchange())
                .with("user.updated");
    }

    @Bean
    public Binding userDeletedBinding() {
        return BindingBuilder.bind(userDeletedQueue())
                .to(usersExchange())
                .with("user.deleted");
    }

    @Bean
    public Binding roleAssignedBinding() {
        return BindingBuilder.bind(roleAssignedQueue())
                .to(usersExchange())
                .with("role.assigned");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
