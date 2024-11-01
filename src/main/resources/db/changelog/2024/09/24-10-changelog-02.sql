-- liquibase formatted sql

-- changeset nikiforov_v:
CREATE TABLE IF NOT EXISTS account
(
    id BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY  PRIMARY KEY,
    client_id BIGINT REFERENCES client(id),
    type VARCHAR(10),
    balance DECIMAL(19, 2),
    is_blocked BOOLEAN
);

-- changeset nikiforov_v:
CREATE TABLE transaction
(
    id        BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    amount    DECIMAL(19, 2),
    client_id BIGINT REFERENCES client(id),
    account_id BIGINT REFERENCES account(id),
    is_retry   BOOLEAN,
    type VARCHAR(20)
);

