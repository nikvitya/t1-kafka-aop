-- liquibase formatted sql

-- changeset e_cha:NikiforovV
CREATE SEQUENCE IF NOT EXISTS account_seq START WITH 1 INCREMENT BY 50;

-- changeset e_cha:NikiforovV
CREATE TABLE IF NOT EXISTS account
(
    id BIGINT NOT NULL PRIMARY KEY,
    client_id BIGINT,
    type VARCHAR(10),
    balance DECIMAL(19, 2),

    CONSTRAINT fk_account_client FOREIGN KEY (client_id) REFERENCES client(id)
);