package test.model.many;

import chill.db.ChillField;
import chill.db.ChillRecord;

public class ManyC extends _generated.AbstractManyC {
    public static final String DDL = "DROP TABLE IF EXISTS many_c;\n" +
            "            CREATE TABLE many_c (\n" +
            "              id INT AUTO_INCREMENT PRIMARY KEY,\n" +
            "              many_b_id INT\n" +
            "            );";

    ChillField<Long> id = pk("id");
    ChillField.FK<ManyC, ManyB> parentB = fk(ManyB.class);

}
