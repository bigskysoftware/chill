package model;

import chill.db.ChillCodeGenerator;
import chill.db.ChillField;
import chill.db.ChillRecord;

import java.sql.Timestamp;

public class User extends _generated.AbstractUser {

    ChillField<Long> id = pk("id");
    ChillField<Timestamp> createdAt = createdAt("created_at");
    ChillField<Timestamp> updatedAt = updatedAt("updated_at").optimisticLock();
    ChillField<String> email = email("email" );
    ChillField<String> password = password("password");
    ChillField<String> uuid = uuid("uuid");

}
