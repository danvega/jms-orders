package dev.danvega.store;

import jakarta.jms.Queue;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableJms
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

    @Bean
    public Queue orderQueue() {
        return new ActiveMQQueue("order-queue");
    }

    @Bean
    public Queue notificationQueue() {
        return new ActiveMQQueue("notification-queue");
    }
}
