CREATE TABLE IF NOT EXISTS users (
    name text not null primary key,
    password text not null,
    admin bool default false
);

INSERT INTO users (name, password, admin) VALUES
    ('Ilia', 'qwerty123', true),
    ('player1', '123', false),
    ('player2', '123', false),
    ('player3', '123', false)