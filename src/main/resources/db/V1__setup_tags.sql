CREATE TABLE IF NOT EXISTS Tags
(
    keyword     VARCHAR(30) PRIMARY KEY,
    description TEXT   NOT NULL,
    value       TEXT   NOT NULL,
    creator     BIGINT NOT NULL -- BIGINT is needed for JOOQ, even if SQLITE doesn't care
);

CREATE TABLE IF NOT EXISTS TagAliases
(
    keyword VARCHAR(30) PRIMARY KEY,
    target  VARCHAR(30) REFERENCES Tags (keyword)
        ON DELETE CASCADE
        ON UPDATE CASCADE
        NOT NULL
);