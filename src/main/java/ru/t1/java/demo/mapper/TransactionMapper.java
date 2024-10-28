package ru.t1.java.demo.mapper;

import org.mapstruct.*;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.TransactionType;
import ru.t1.java.demo.model.dto.TransactionDto;
import ru.t1.java.demo.model.dto.TransactionFullDto;

import java.math.BigDecimal;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = TransactionMapper.class)
public interface TransactionMapper {

    @Mapping(target = "type", expression = "java(determineTransactionType(transactionDto.getAmount()))")
    Transaction toEntity(TransactionDto transactionDto);

    default TransactionType determineTransactionType(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) >= 0) {
            return TransactionType.DEPOSIT;
        } else {
            return TransactionType.WITHDRAW;
        }
    }

    TransactionDto toDto(Transaction transaction);

    TransactionFullDto toFullDto(Transaction transaction);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Transaction partialUpdate(TransactionDto transactionDto, @MappingTarget Transaction transaction);

}

