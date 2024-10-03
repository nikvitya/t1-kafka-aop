package ru.t1.java.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "account")
@AllArgsConstructor
@NoArgsConstructor
public class Account extends AbstractPersistable<Long> {

    @Column(name = "client_id")
    private Long clientId;

    private AccountType type;

    @Column(precision = 19, scale = 2)
    private BigDecimal balance;

}
