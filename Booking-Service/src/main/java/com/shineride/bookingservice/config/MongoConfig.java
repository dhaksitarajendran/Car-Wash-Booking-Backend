package com.shineride.bookingservice.config;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class MongoConfig {

  @Bean
  MongoClientSettings mongoClientSettings() {
    return MongoClientSettings.builder()
      .applyConnectionString(new ConnectionString(
        "mongodb+srv://SruthiSrinivasan:8FhFAvPHtfeoLZRG@cluster0.wjeu6lp.mongodb.net/BookingServiceDB?retryWrites=true&w=majority&appName=Cluster0"
      ))
      .applyToSocketSettings(s -> s
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)     // <-- key for your error
      )
      .applyToClusterSettings(c -> c
        .serverSelectionTimeout(60, TimeUnit.SECONDS)
      )
      .applyToServerSettings(s -> s
        .heartbeatFrequency(10, TimeUnit.SECONDS)
      )
      .build();
  }
}