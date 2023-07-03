package com.fiveam.orderservice.client;

import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfiguration {
    @Bean
    public Client feignClient() {
        return new Client.Default(null, null);
    }
}
