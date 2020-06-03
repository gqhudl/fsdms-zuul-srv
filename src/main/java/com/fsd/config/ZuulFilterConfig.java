package com.fsd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZuulFilterConfig {

  @Bean
  public PreRequestFilter preRequestFilter() {
    return new PreRequestFilter();
  }

}
