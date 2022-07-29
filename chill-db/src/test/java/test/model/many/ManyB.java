package test.model.many;

import chill.db.ChillField;
import chill.db.ChillRecord;

public class ManyB extends _generated.AbstractManyB {
    public static final String DDL = """
            DROP TABLE IF EXISTS many_b;
            CREATE TABLE many_b (
              id INT AUTO_INCREMENT PRIMARY KEY,
              many_a_id INT
            );
            """;

    ChillField<Long> id = pk("id");
    ChillField.FK<ManyB, ManyA> parentA = fk(ManyA.class);
    ChillField.Many<ManyC> manyCs = hasMany(ManyC.class);
}
