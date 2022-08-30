CREATE TABLE user5 (
                      id INT AUTO_INCREMENT PRIMARY KEY,
                      first_name VARCHAR(250),
                      last_name VARCHAR(250),
                      email VARCHAR(250) DEFAULT NULL,
                      password VARCHAR(250) DEFAULT NULL,
                      age INTEGER
);