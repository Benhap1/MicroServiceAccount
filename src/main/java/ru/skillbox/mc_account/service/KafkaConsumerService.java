package ru.skillbox.mc_account.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.skillbox.common.events.CommonEvent;
import ru.skillbox.common.events.UserEvent;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "user-events", groupId = "user-event-group")
    public void listen(CommonEvent<UserEvent> commonEvent) {
        UserEvent userEvent = commonEvent.getData();
        System.out.println("Received UserEvent: " + userEvent);
    }
}

