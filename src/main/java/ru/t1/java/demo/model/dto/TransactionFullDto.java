package ru.t1.java.demo.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import ru.t1.java.demo.model.TransactionType;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class TransactionFullDto {

    private Long id;
    private BigDecimal amount;
    @JsonProperty("client_id")
    private Long clientId;
    @JsonProperty("account_id")
    private Long accountId;
    private TransactionType type;
}
