package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.dto.ClientDto;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaClientProducer {
    private final KafkaTemplate template;
    private final KafkaTemplate<String,Long> templateLong;

    @Value("${t1.kafka.topic.client_id_registered}")
    private String topic;

    public void send(Long id) {
        try {
            templateLong.setDefaultTopic(topic);
            templateLong.sendDefault(UUID.randomUUID().toString(), id).get();
            templateLong.flush();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    public void sendTo(String topic, ClientDto o) {
        try {
            template.send(topic, o).get();
            template.flush();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

}
