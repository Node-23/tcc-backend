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
            System.setProperty("AWS_ACCESS_KEY_ID", Objects.requireNonNull(dotenv.get("AWS_ACCESS_KEY_ID")));
            System.setProperty("AWS_ACCESS_KEY_SECRET", Objects.requireNonNull(dotenv.get("AWS_ACCESS_KEY_SECRET")));
            System.setProperty("AWS_REGION", Objects.requireNonNull(dotenv.get("AWS_REGION")));
            System.setProperty("S3_BUCKET_NAME", Objects.requireNonNull(dotenv.get("S3_BUCKET_NAME")));
        }

        SpringApplication.run(TccBackendApplication.class, args);
    }

}
