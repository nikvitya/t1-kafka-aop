package ru.t1.java.demo.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class ErrorDto {
    @JsonProperty("method_name")
    private String methodName;
    @JsonProperty("method_parameters")
    private String methodParameters;
    @JsonProperty("stack_trace")
    private String stackTrace;

}
