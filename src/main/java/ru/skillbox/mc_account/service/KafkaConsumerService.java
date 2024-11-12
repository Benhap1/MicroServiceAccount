package ru.skillbox.mc_account.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.skillbox.common.events.CommonEvent;
import ru.skillbox.common.events.UserEvent;

@Service
public class KafkaConsumerService {

    private final ObjectMapper objectMapper;

    @Autowired
    public KafkaConsumerService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "user-events", groupId = "user-event-group")
    public void listen(CommonEvent<UserEvent> commonEvent) {
        UserEvent userEvent = commonEvent.getData();
        try {
            String jsonOutput = objectMapper.writeValueAsString(userEvent);
            System.out.println("Received UserEvent: " + jsonOutput);
        } catch (Exception e) {
            System.err.println("Error while converting UserEvent to JSON: " + e.getMessage());
        }
    }
}

