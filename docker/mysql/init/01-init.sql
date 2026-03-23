CREATE TABLE legacy_users (
    user_identifier VARCHAR(64) PRIMARY KEY,
    user_name VARCHAR(128) NOT NULL,
    given_name VARCHAR(128) NOT NULL,
    family_name VARCHAR(128) NOT NULL
);

INSERT INTO legacy_users (user_identifier, user_name, given_name, family_name) VALUES
    ('101', 'alice.legacy', 'Alice', 'Smith'),
    ('102', 'bwayne', 'Bruce', 'Wayne');
    ('104', 'jex', 'John', 'Alex');
