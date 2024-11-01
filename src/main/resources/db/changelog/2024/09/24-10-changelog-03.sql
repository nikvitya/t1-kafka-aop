-- liquibase formatted sql

-- changeset e_cha:1727702549313-5
CREATE TABLE role
(
    id   BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(20)
);

INSERT INTO role (name) VALUES ('ROLE_USER');
INSERT INTO role (name) VALUES ('ROLE_ADMIN');
INSERT INTO role (name) VALUES ('ROLE_MODERATOR');


-- changeset e_cha:1727702549313-7
CREATE TABLE users
(
    id       BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    login    VARCHAR(20) UNIQUE,
    email    VARCHAR(50) UNIQUE,
    password VARCHAR(120)
);

-- changeset e_cha:1727702549313-6
CREATE TABLE user_roles
(
    role_id BIGINT NOT NULL REFERENCES role(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    CONSTRAINT pk_user_roles PRIMARY KEY (role_id, user_id)
);

