package com.example.hrservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Định nghĩa tên
    public static final String EXCHANGE_NAME = "staff-exchange";
    public static final String QUEUE_NAME = "auth-account-creation-queue";
    public static final String ROUTING_KEY = "staff.created";

    public static final String NOTIFICATION_EXCHANGE = "notification-exchange";
    public static final String PROBATION_ROUTING_KEY = "probation.review.due";

    public static final String ROLE_UPDATE_ROUTING_KEY = "account.role.update";

    // 👇 THÊM KEY MỚI CHO VIỆC KHÓA TÀI KHOẢN
    public static final String ACCOUNT_DISABLE_ROUTING_KEY = "account.disable";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(NOTIFICATION_EXCHANGE);
    }

    // Bean này không bắt buộc ở service gửi, nhưng tốt để có
    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME);
    }

    // Bean này không bắt buộc ở service gửi
    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}