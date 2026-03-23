CREATE TABLE app_users (
    external_id VARCHAR(64) PRIMARY KEY,
    login VARCHAR(128) NOT NULL,
    first_name VARCHAR(128) NOT NULL,
    last_name VARCHAR(128) NOT NULL
);

INSERT INTO app_users (external_id, login, first_name, last_name) VALUES
    ('100', 'jdoe', 'John', 'Doe'),
    ('101', 'asmith', 'Alice', 'Smith');
    ('103', 'td.pn', 'Todd', 'Peterson');
