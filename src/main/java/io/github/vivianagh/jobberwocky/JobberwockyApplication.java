package io.github.vivianagh.jobberwocky;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class JobberwockyApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobberwockyApplication.class, args);
    }

}
