package com.example.coffeemanager.config;

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

    // Tên phải khớp 100% với tên bên hr-service
    public static final String EXCHANGE_NAME = "staff-exchange";
    public static final String ROUTING_KEY_DISABLE = "account.disable";

    public static final String QUEUE_NAME_UPDATE = "auth-account-update-queue";
    public static final String ROUTING_KEY_UPDATE = "account.role.update";



    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }


    // Bean cho Queue CẬP NHẬT
    @Bean
    public Queue updateQueue() {
        return new Queue(QUEUE_NAME_UPDATE, true);
    }

    // Binding cho CẬP NHẬT
    @Bean
    public Binding updateBinding(Queue updateQueue, DirectExchange exchange) {
        return BindingBuilder.bind(updateQueue).to(exchange).with(ROUTING_KEY_UPDATE);
    }


    @Bean
    public Binding disableBinding(Queue updateQueue, DirectExchange exchange) {
        return BindingBuilder.bind(updateQueue).to(exchange).with(ROUTING_KEY_DISABLE);
    }

    /**
     * Bean này báo cho Spring Boot dùng JSON để gửi/nhận tin nhắn
     * (thay vì Java serialization).
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
