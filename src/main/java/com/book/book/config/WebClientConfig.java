//package com.book.book.config;
//
//import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.reactive.function.client.ExchangeStrategies;
//import org.springframework.web.reactive.function.client.WebClient;
//
//@Configuration
//public class WebClientConfig {
//
//    @Bean
//    public WebClientCustomizer webClientCustomizer() {
//        return webClientBuilder -> webClientBuilder.exchangeStrategies(
//                ExchangeStrategies.builder()
//                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(20 * 1024 * 1024)) // 20MB
//                        .build()
//        );
//    }
//}
