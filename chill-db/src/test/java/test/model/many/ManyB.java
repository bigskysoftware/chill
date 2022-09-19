package test.model.many;

import chill.db.ChillField;
import chill.db.ChillRecord;

public class ManyB extends _generated.AbstractManyB {
    public static final String DDL = "            DROP TABLE IF EXISTS many_b;\n" +
            "            CREATE TABLE many_b (\n" +
            "              id INT AUTO_INCREMENT PRIMARY KEY,\n" +
            "              many_a_id INT\n" +
            "            );\n";

    ChillField<Long> id = pk("id");
    ChillField.FK<ManyB, ManyA> parentA = fk(ManyA.class);
    ChillField.Many<ManyC> manyCs = hasMany(ManyC.class);
}
