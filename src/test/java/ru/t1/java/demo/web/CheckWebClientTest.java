package ru.t1.java.demo.web;

import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import ru.t1.java.demo.model.dto.CheckResponse;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
//@ExtendWith(MockitoExtension.class)
//@TestPropertySource("classpath:application-test.yml")
public class CheckWebClientTest {
    @Autowired
    CheckWebClient checkWebClient;

//    @Autowired
//    WebClient webClient;

//    CheckClientConfig config = new CheckClientConfig();
//    CheckClientConfig.ClientHttp clientHttp = config.new ClientHttp();
//    ClientHttpConnector connector = clientHttp.getClientHttp(CheckWebClient.class.getName());
//    CheckWebClient checkWebClient = new CheckWebClient(WebClient.builder()
//            .baseUrl("http://localhost:8088")
//            .clientConnector(connector).build());

    @Test
    void check() {

        assertThat(checkWebClient.check(1L)).get().isEqualTo(CheckResponse.builder()
                .blocked(false)
                .build());

    }

    @Test
    void check2() {

        assertThat(checkWebClient.checkTransaction(1L)).get().isEqualTo(CheckResponse.builder()
                .blocked(false)
                .build());

    }
}
