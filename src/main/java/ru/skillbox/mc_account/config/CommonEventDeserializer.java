package ru.skillbox.mc_account.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.skillbox.common.events.CommonEvent;

import java.io.IOException;

public class CommonEventDeserializer extends JsonDeserializer<CommonEvent<?>> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public CommonEvent<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return objectMapper.readValue(p, CommonEvent.class);
    }
}
