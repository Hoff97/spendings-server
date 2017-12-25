# --- !Ups
CREATE TABLE category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR,
    parent_fk BIGSERIAL
);

CREATE TABLE spending (
    id BIGSERIAL PRIMARY KEY,
    amount decimal(20,3),
    description VARCHAR,
    dayt Timestamp,
    category_fk BIGSERIAL,
    user_fk BIGSERIAL
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR,
    pwhash VARCHAR,
    email VARCHAR,
    pwsalt VARCHAR,
    pwhasher VARCHAR,
    provider_id VARCHAR,
    provider_key VARCHAR
);

# --- !Downs

DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS spending;
DROP TABLE IF EXISTS users;
