-- liquibase formatted sql

-- changeset e_cha:1726477659739-1
ALTER TABLE client
    ADD IF NOT EXISTS first_name VARCHAR(255);
ALTER TABLE client
    ADD IF NOT EXISTS last_name VARCHAR(255);
ALTER TABLE client
    ADD IF NOT EXISTS middle_name VARCHAR(255);

