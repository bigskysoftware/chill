package test.model;

import chill.db.ChillCodeGenerator;
import chill.db.ChillField;
import chill.db.ChillQuery;
import chill.db.ChillRecord;

public class User extends _generated.AbstractUser {

    public static final String DDL = """
            DROP TABLE IF EXISTS user;
            CREATE TABLE user (
              id INT AUTO_INCREMENT PRIMARY KEY,
              first_name VARCHAR(250),
              last_name VARCHAR(250),
              email VARCHAR(250) DEFAULT NULL,
              password VARCHAR(250) DEFAULT NULL,
              age INTEGER
            );
            """;

    ChillField<Long> id = pk("id");

    ChillField<String> firstName = field("first_name", String.class);

    ChillField<String> lastName = field("last_name", String.class);

    ChillField<String> email = email("email").required();

    ChillField<String> password = password("password");

    ChillField<Integer> age = field("age", Integer.class);

    ChillField<ChillQuery<Vehicle>> vehicles = many(Vehicle.class, "user_id");



}
