CREATE TABLE IF NOT EXISTS customer (
                          id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                          identifier VARCHAR(255) unique,
                          x INT NOT NULL,
                          y INT NOT NULL,
                          dest_identifier VARCHAR(255),
                          state VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS taxi (
                      id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                      identifier VARCHAR(255) unique,
                      available BOOLEAN NOT NULL,
                      x INT NOT NULL,
                      y INT NOT NULL,
                      dest_identifier VARCHAR(255),
                      state VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS location (
                          id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                          identifier VARCHAR(255) unique,
                          x INT NOT NULL,
                          y INT NOT NULL
);

CREATE TABLE IF NOT EXISTS customer_taxi_assignment (
                                          customer_id BIGINT NOT NULL unique ,
                                          taxi_id BIGINT NOT NULL unique ,
                                          PRIMARY KEY (customer_id, taxi_id),
                                          CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
                                          CONSTRAINT fk_taxi FOREIGN KEY (taxi_id) REFERENCES taxi(id)
);

INSERT INTO Taxi (identifier, available, x, y)
VALUES ('1', true, 1, 1),
       ('2', true, 1, 1),
       ('3', true, 1, 1),
       ('4', true, 1, 1);
-- INSERT INTO Taxi (identifier, available, x, y) VALUES
--                                                    ('1', true, 1, 1),
--                                                    ('2', true, 1, 1),
--                                                    ('3', true, 1, 1),
--                                                    ('4', true, 1, 1);


INSERT INTO Customer (identifier, x, y)
VALUES ('a', 5, 10),
       ('b', 15, 5),
       ('c', 10, 18),
       ('d', 3, 7);

