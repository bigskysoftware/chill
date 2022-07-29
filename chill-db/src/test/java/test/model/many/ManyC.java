package test.model.many;

import chill.db.ChillField;
import chill.db.ChillRecord;

public class ManyC extends _generated.AbstractManyC {
    public static final String DDL = """
            DROP TABLE IF EXISTS many_c;
            CREATE TABLE many_c (
              id INT AUTO_INCREMENT PRIMARY KEY,
              many_b_id INT
            );
            """;

    ChillField<Long> id = pk("id");
    ChillField.FK<ManyC, ManyB> parentB = fk(ManyB.class);

}
