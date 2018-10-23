CREATE TABLE IF NOT EXISTS Tags (
  keyword     VARCHAR(30) PRIMARY KEY,
  description TEXT    NOT NULL,
  value       TEXT    NOT NULL,
  creator     INTEGER NOT NULL
);