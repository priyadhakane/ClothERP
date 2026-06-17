package com.clotherp.backend;

import com.clotherp.backend.config.DatabaseInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BackendApplication {

    public static void main(String[] args) {
        DatabaseInitializer.initialize();
        SpringApplication.run(BackendApplication.class, args);
    }
}
