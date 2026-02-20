package com.kce.AuthService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class AuthServiceApplication {

  public static void main(String[] args) {

    SpringApplication app = new SpringApplication(AuthServiceApplication.class);

    app.addListeners((ApplicationListener<ApplicationEnvironmentPreparedEvent>) event -> {
      Environment env = event.getEnvironment();
      System.out.println(">>> EARLY spring.data.mongodb.uri = " + env.getProperty("spring.data.mongodb.uri"));
      System.out.println(">>> EARLY SPRING_DATA_MONGODB_URI = " + System.getenv("SPRING_DATA_MONGODB_URI"));
      System.out.println(">>> EARLY spring.profiles.active = " + env.getProperty("spring.profiles.active"));
    });

    app.run(args);
  }
}