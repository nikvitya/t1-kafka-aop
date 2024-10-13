package ru.t1.java.demo.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.kafka.KafkaMetricProducer;
import ru.t1.java.demo.model.dto.MetricTraceDto;

import java.util.concurrent.atomic.AtomicLong;

@Async
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class MetricAspect {

    private static final AtomicLong START_TIME = new AtomicLong();
    private final KafkaMetricProducer kafkaMetricProducer;
    @Value("${t1.kafka.topic.metric_trace}")
    private String topic;
    @Value("${t1.aop.max-execution-time}")
    private Long maxMethodExecutionTime;

    @Before("@annotation(ru.t1.java.demo.aop.Metric)")
    public void logExecTime(JoinPoint joinPoint) throws Throwable {
        log.info("Старт метода: {}", joinPoint.getSignature().toShortString());
        START_TIME.addAndGet(System.currentTimeMillis());
    }

    @After("@annotation(ru.t1.java.demo.aop.Metric)")
    public void calculateTime(JoinPoint joinPoint) {
        long afterTime = System.currentTimeMillis();
        long durationTime = afterTime - START_TIME.get();
        log.info("Время исполнения: {} ms", durationTime);

//      Если время работы метода превышает заданное значение, аспект должен отправлять сообщение в топик Kafka
//        (t1_demo_metric_trace) c информацией о времени работы, имени метода и параметрах метода, если таковые имеются.

        if (durationTime > maxMethodExecutionTime) {
            kafkaMetricProducer.sendTo(topic,
                    MetricTraceDto.builder()
                            .methodName(joinPoint.getSignature().getName())
                            .duration(durationTime)
                            .methodParameters(joinPoint.getArgs())
                            .build());
        }

        START_TIME.set(0L);
    }

//    @Around("@annotation(ru.t1.java.demo.aop.Metric)")
//    public Object logExecTime(ProceedingJoinPoint pJoinPoint) {
//        log.info("Вызов метода: {}", pJoinPoint.getSignature().toShortString());
//        long beforeTime = System.currentTimeMillis();
//        Object result = null;
//        try {
//            result = pJoinPoint.proceed();//Important
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//        }
//        long afterTime = System.currentTimeMillis();
//        log.info("Время исполнения: {} ms", (afterTime - beforeTime));
//        return result;
//    }
}
