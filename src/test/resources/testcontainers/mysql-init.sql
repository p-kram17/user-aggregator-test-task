CREATE TABLE legacy_users (
    user_identifier VARCHAR(64) PRIMARY KEY,
    user_name VARCHAR(128) NOT NULL,
    given_name VARCHAR(128) NOT NULL,
    family_name VARCHAR(128) NOT NULL
);

INSERT INTO legacy_users (user_identifier, user_name, given_name, family_name) VALUES
    ('2', 'alice.duplicate', 'Alice', 'Smith'),
    ('3', 'bwayne', 'Bruce', 'Anderson'),
    ('4', 'td.pn', 'Todd', 'Peterson');
