package ru.t1.java.demo.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.dto.ClientDto;
import ru.t1.java.demo.repository.ClientRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceImplTest {
    @InjectMocks
    @Spy
    ClientServiceImpl clientService;

    @Mock
    ClientServiceImpl clientServiceMock;

    @Mock
    ClientRepository clientRepository;

    @Test
    void parseJsonTest() {

        List<ClientDto> expectedClients = List.of(
                ClientDto.builder()
                        .firstName("Arlena")
                        .build()
        );
        when(clientService.parseJson()).thenReturn(expectedClients);
        List<ClientDto> actualClients = clientService.parseJson();

        assertEquals(expectedClients.get(0).getFirstName(), actualClients.get(0).getFirstName());
        verify(clientService, times(1)).parseJson();
    }

    @Test
    void registerClientTest() {
        Client client = new Client();
        client.setId(1L);
        client.setFirstName("John");
        client.setLastName("Doe");

        when(clientRepository.save(client)).thenReturn(client);
        Client savedClient = clientService.registerClient(client);

        assertEquals(client, savedClient);
    }

    @Test
    void parseJsonSpy() {
        when(clientService.parseJson()).thenReturn(List.of(ClientDto.builder()
                .build()));

        assertEquals(List.of(ClientDto.builder().build()), clientService.parseJson());
    }

    @Test
    void parseJsonMock() {
        when(clientServiceMock.parseJson()).thenReturn(List.of(ClientDto.builder().build()));
        assertEquals(List.of(ClientDto.builder().build()), clientServiceMock.parseJson());
    }
}