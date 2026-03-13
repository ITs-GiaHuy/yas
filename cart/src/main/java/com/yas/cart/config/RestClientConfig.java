package com.yas.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

// hahah
    @Bean
    public RestClient restClient() {
        return RestClient.builder().build();
    }

}
