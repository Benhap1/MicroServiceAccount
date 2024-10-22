package ru.skillbox.mc_account.service;

import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.skillbox.common.events.account.UserEvent;

@Service
@AllArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;
    private static final String TOPIC = "user-events";

    public void sendUserEvent(UserEvent userEvent) {
        kafkaTemplate.send(TOPIC, userEvent.getId().toString(), userEvent);
    }
}
