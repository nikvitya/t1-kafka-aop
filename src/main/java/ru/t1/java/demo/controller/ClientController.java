package ru.t1.java.demo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.ClientRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.t1.java.demo.aop.HandlingResult;
import ru.t1.java.demo.aop.LogException;
import ru.t1.java.demo.aop.Track;
import ru.t1.java.demo.kafka.KafkaClientProducer;
import ru.t1.java.demo.mapper.ClientMapper;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.Role;
import ru.t1.java.demo.model.RoleEnum;
import ru.t1.java.demo.model.User;
import ru.t1.java.demo.model.dto.ClientDto;
import ru.t1.java.demo.model.dto.ClientFullDto;
import ru.t1.java.demo.model.dto.security.MessageResponse;
import ru.t1.java.demo.model.dto.security.SignupRequest;
import ru.t1.java.demo.service.ClientService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ClientController {

    private final ClientService clientService;
    private final ClientMapper clientMapper;
    private final KafkaClientProducer kafkaClientProducer;
    @Value("${t1.kafka.topic.client_registration}")
    private String topic;

    @LogException
    @Track
    @GetMapping(value = "/parseClient")
    @HandlingResult
    public void parseSource() {
        List<ClientDto> clientDtos = clientService.parseJson();
        clientDtos.forEach(dto -> {
            kafkaClientProducer.sendTo(topic, dto);
        });
    }

    @PostMapping("/client/register")
    public ClientFullDto registerUser(@Valid @RequestBody ClientDto clientDto) {
        Client client = clientService.registerClient(clientMapper.toEntity(clientDto));

        return clientMapper.toFullDto(client);
    }





}
