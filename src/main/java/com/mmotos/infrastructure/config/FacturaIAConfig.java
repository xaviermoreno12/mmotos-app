package com.mmotos.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmotos.infrastructure.output.ai.OpenAiExtractionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FacturaIAConfig {

    @Bean
    public OpenAiExtractionService openAiExtractionService(AppProperties props, ObjectMapper mapper) {
        return new OpenAiExtractionService(props.openai().apiKey(), mapper);
    }
}
