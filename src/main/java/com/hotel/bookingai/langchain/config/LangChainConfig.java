package com.hotel.bookingai.langchain.config;

import com.hotel.bookingai.langchain.assistant.Assistant;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChainConfig {

    @Bean
    public ChatMemory chatMemory() {
        // Пази последните 10 съобщения от разговора в паметта
        return MessageWindowChatMemory.withMaxMessages(10);
    }

}
