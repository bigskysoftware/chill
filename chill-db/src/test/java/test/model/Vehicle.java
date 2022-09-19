package test.model;

import chill.db.*;
import chill.db.ChillField.FK;

import java.sql.Timestamp;

public class Vehicle extends _generated.AbstractVehicle {

    public static final String DDL = "DROP TABLE IF EXISTS vehicle;\n" +
            "            CREATE TABLE vehicle (\n" +
            "              id INT AUTO_INCREMENT PRIMARY KEY,\n" +
            "              created_at TIMESTAMP,\n" +
            "              updated_at TIMESTAMP,\n" +
            "              user_id INT,\n" +
            "              make VARCHAR(250) NOT NULL,\n" +
            "              model VARCHAR(250) NOT NULL,\n" +
            "              uuid VARCHAR(250),\n" +
            "              year INTEGER\n" +
            "            );";

    ChillField<Long> id = pk("id");
    ChillField<Timestamp> createdAt = createdAt("created_at");
    ChillField<Timestamp> updatedAt = updatedAt("updated_at").optimisticLock();

    ChillField<String> make = field("make", String.class);
    ChillField<String> model = field("model", String.class);
    ChillField<String> uuid = uuid("uuid");

    ChillField<Integer> year = field("year", Integer.class);

    FK<Vehicle, User> user = fk(User.class);

}
