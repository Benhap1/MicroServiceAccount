package ru.skillbox.mc_account.service;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.skillbox.common.events.CommonEvent;
import ru.skillbox.common.events.UserEvent;

@Service
@AllArgsConstructor
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class); // Создаем логгер

    private final KafkaTemplate<String, CommonEvent<UserEvent>> kafkaTemplate;
    private static final String TOPIC = "user-events";

    public void sendUserEvent(CommonEvent<UserEvent> commonEvent) {
        log.info("Sending user event with id: {}", commonEvent.getEventId());
        kafkaTemplate.send(TOPIC, commonEvent.getEventId().toString(), commonEvent);
    }
}
