--changeset 1cg:1
CREATE TABLE sample
(
    id         SERIAL PRIMARY KEY,
    first_name VARCHAR(64),
    last_name  VARCHAR(64),
    email      VARCHAR(64),
    age        INT
);
--rollback drop table sample;

--changeset 1cg:2
CREATE TABLE user
(
    id         SERIAL PRIMARY KEY,
    first_name VARCHAR(64),
    last_name  VARCHAR(64),
    email      VARCHAR(64),
    password   VARCHAR(1000),
    age        INT
);
--rollback drop table user;

--changeset 1cg:3
CREATE TABLE test
(
    name   VARCHAR(1000),
    user_id        INT
);
--rollback drop table test;