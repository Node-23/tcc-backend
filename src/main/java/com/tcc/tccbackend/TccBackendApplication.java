package com.tcc.tccbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import static io.github.cdimascio.dotenv.Dotenv.*;
import java.util.Objects;

@SpringBootApplication
public class TccBackendApplication {

    public static void main(String[] args) {
        boolean isJenkins = System.getenv("CI") != null && System.getenv("CI").equalsIgnoreCase("true");
        if (!isJenkins) {
            var dotenv = load();
            System.setProperty("POSTGRES_DB", Objects.requireNonNull(dotenv.get("POSTGRES_DB")));
            System.setProperty("POSTGRES_USER", Objects.requireNonNull(dotenv.get("POSTGRES_USER")));
            System.setProperty("POSTGRES_PASSWORD", Objects.requireNonNull(dotenv.get("POSTGRES_PASSWORD")));
            System.setProperty("SPRING_DATASOURCE_URL", Objects.requireNonNull(dotenv.get("SPRING_DATASOURCE_URL")));
        }

        SpringApplication.run(TccBackendApplication.class, args);
    }

}
