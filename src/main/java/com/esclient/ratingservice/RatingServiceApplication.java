package com.esclient.ratingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@SuppressWarnings({"checkstyle:HideUtilityClassConstructor", "PMD.UseUtilityClass"})
public class RatingServiceApplication {

  public static void main(final String[] args) {
    SpringApplication.run(RatingServiceApplication.class, args);
  }
}
