package test.model.many;

import chill.db.ChillField;
import chill.db.ChillRecord;

public class ManyA extends _generated.AbstractManyA {
    public static final String DDL = "            DROP TABLE IF EXISTS many_a;\n" +
            "            CREATE TABLE many_a (\n" +
            "              id INT AUTO_INCREMENT PRIMARY KEY\n" +
            "            );\n";

    ChillField<Long> id = pk("id");
    ChillField.Many<ManyB> manyBs = hasMany(ManyB.class);

    ChillField.Many<ManyC> manyCs = hasMany(ManyC.class).through(manyBs);

}
