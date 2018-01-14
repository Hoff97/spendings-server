# --- !Ups

CREATE TABLE scan (
id BIGSERIAL PRIMARY KEY,
result VARCHAR NOT NULL
);

ALTER TABLE spending
      ADD COLUMN scan_fk BIGINT DEFAULT NULL REFERENCES scan ON DELETE CASCADE,
      ADD CONSTRAINT spending_user_fk
          FOREIGN KEY (user_fk) REFERENCES users ON DELETE CASCADE,
      ADD CONSTRAINT spending_category_fk
          FOREIGN KEY (category_fk) REFERENCES category ON DELETE CASCADE;

ALTER TABLE category
      DROP COLUMN parent_fk;

CREATE TABLE token (
id BIGSERIAL PRIMARY KEY,
text VARCHAR NOT NULL
);

CREATE TABLE category_tokenP (
id BIGSERIAL PRIMARY KEY,
category_fk BIGSERIAL NOT NULL REFERENCES category ON DELETE CASCADE,
token_fk BIGSERIAL NOT NULL REFERENCES token ON DELETE CASCADE,
count BIGINT NOT NULL,
total BIGINT NOT NULL
);

CREATE TABLE description_tokenP (
id BIGSERIAL PRIMARY KEY,
description_fk BIGSERIAL NOT NULL REFERENCES token ON DELETE CASCADE,
token_fk BIGSERIAL NOT NULL REFERENCES token ON DELETE CASCADE,
count BIGINT NOT NULL,
total BIGINT NOT NULL
);

CREATE TABLE amount_tokenP (
id BIGSERIAL PRIMARY KEY,
token_fk BIGSERIAL NOT NULL REFERENCES token ON DELETE CASCADE,
count BIGINT NOT NULL,
total BIGINT NOT NULL
);

CREATE TABLE date_tokenP (
id BIGSERIAL PRIMARY KEY,
token_fk BIGSERIAL NOT NULL REFERENCES token ON DELETE CASCADE,
count BIGINT NOT NULL,
total BIGINT NOT NULL
);

# --- !Downs

ALTER TABLE spending DROP COLUMN scan_fk;

ALTER TABLE spending
      DROP CONSTRAINT spending_user_fk,
      DROP CONSTRAINT spending_category_fk;

DROP TABLE IF EXISTS category_tokenP;
DROP TABLE IF EXISTS description_tokenP;
DROP TABLE IF EXISTS amount_tokenP;
DROP TABLE IF EXISTS date_tokenP;

DROP TABLE IF EXISTS scan;
DROP TABLE IF EXISTS token;

ALTER TABLE category
      ADD COLUMN parent_fk BIGSERIAL;