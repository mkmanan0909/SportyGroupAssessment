package org.assessment.sporty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableKafka
@EnableRetry(proxyTargetClass = true)
public class SportyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SportyApplication.class, args);
    }
}
