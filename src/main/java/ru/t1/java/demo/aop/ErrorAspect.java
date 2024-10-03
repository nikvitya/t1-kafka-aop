package ru.t1.java.demo.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.kafka.KafkaErrorProducer;
import ru.t1.java.demo.model.dto.ErrorDto;

import java.util.Arrays;

@Async
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ErrorAspect {

    private final KafkaErrorProducer kafkaErrorProducer;

    @Value("${t1.kafka.topic.error_trace}")
    private String topic;

    @Pointcut("within(ru.t1.java.demo..*)")
    public void allMethods() {

    }

    @AfterThrowing(value = "allMethods()", throwing = "ex")
    public void logExceptionAnnotation(JoinPoint joinPoint, Throwable ex) {
        log.error("Старт метода: {}", joinPoint.getSignature().toShortString());

        kafkaErrorProducer.sendTo(topic,
                ErrorDto.builder()
                        .methodName(joinPoint.getSignature().toShortString())
                        .methodParameters(Arrays.toString(joinPoint.getArgs()))
                        .stackTrace(Arrays.toString(ex.getStackTrace()))
                        .build());
    }
}

