package test.model;

import chill.db.*;
import chill.db.ChillField.FK;

import java.sql.Timestamp;

public class Vehicle extends _generated.AbstractVehicle {

    public static final String DDL = """
            DROP TABLE IF EXISTS vehicle;
            CREATE TABLE vehicle (
              id INT AUTO_INCREMENT PRIMARY KEY,
              created_at TIMESTAMP,
              updated_at TIMESTAMP,
              user_id INT,
              make VARCHAR(250) NOT NULL,
              model VARCHAR(250) NOT NULL,
              uuid VARCHAR(250),
              year INTEGER
            );
            """;

    ChillField<Long> id = pk("id");
    ChillField<Timestamp> createdAt = createdAt("created_at");
    ChillField<Timestamp> updatedAt = updatedAt("updated_at").optimisticLock();

    ChillField<String> make = field("make", String.class);
    ChillField<String> model = field("model", String.class);
    ChillField<String> uuid = uuid("uuid");

    ChillField<Integer> year = field("year", Integer.class);

    FK<Vehicle, User> user = fk(User.class);

}
