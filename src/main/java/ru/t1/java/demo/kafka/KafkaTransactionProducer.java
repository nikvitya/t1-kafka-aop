package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaTransactionProducer {

    private final KafkaTemplate template;
    private final KafkaTemplate<String, Long> templateLong;

    @Value("${t1.kafka.topic.transaction_id_registered}")
    private String transactionTopic;

    @Value("${t1.kafka.topic.transaction_errors}")
    private String transactionErrorTopic;

    public void send(Long id) {
        try {
            templateLong.setDefaultTopic(transactionTopic);
            templateLong.sendDefault(UUID.randomUUID().toString(), id).get();
            templateLong.flush();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    public void sendTransaction(Long id) {
        try {
            templateLong.setDefaultTopic(transactionErrorTopic);
            templateLong.sendDefault(UUID.randomUUID().toString(), id).get();
            templateLong.flush();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    public void sendTo(String topic, Object o) {
        try {
            template.send(topic, o).get();
            template.flush();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

}
