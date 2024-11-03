package ru.skillbox.mc_account.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import ru.skillbox.common.events.CommonEvent;

import java.io.IOException;
import java.util.Map;

public class CommonEventDeserializer implements Deserializer<CommonEvent<?>> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // Конфигурация, если она нужна
    }

    @Override
    public CommonEvent<?> deserialize(String topic, byte[] data) {
        try {
            // Десериализация из JSON
            return objectMapper.readValue(data, CommonEvent.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize CommonEvent", e);
        }
    }

    @Override
    public void close() {
        // Закрытие, если нужно
    }
}