package test.model;

import chill.db.ChillField;
import chill.db.ChillQuery;

public class User extends _generated.AbstractUser {

    public static final String DDL = "DROP TABLE IF EXISTS user;\n" +
            "            CREATE TABLE user (\n" +
            "              id INT AUTO_INCREMENT PRIMARY KEY,\n" +
            "              first_name VARCHAR(250),\n" +
            "              last_name VARCHAR(250),\n" +
            "              email VARCHAR(250) DEFAULT NULL,\n" +
            "              password VARCHAR(250) DEFAULT NULL,\n" +
            "              age INTEGER\n" +
            "            );";

    ChillField<Long> id = pk("id");

    ChillField<String> firstName = field("first_name", String.class);

    ChillField<String> lastName = field("last_name", String.class);

    ChillField<String> email = email("email").required();

    ChillField<String> password = password("password");

    ChillField<Integer> age = field("age", Integer.class);

    ChillField.Many<Vehicle> vehicles = hasMany(Vehicle.class);



}
