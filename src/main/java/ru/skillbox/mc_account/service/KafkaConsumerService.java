package ru.skillbox.mc_account.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.skillbox.common.events.account.UserEvent;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "user-events", groupId = "user-event-group")
    public void listen(UserEvent userEvent) {
        System.out.println("Received UserEvent: " + userEvent);
    }
}
