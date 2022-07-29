package chill.db.many;

import chill.db.ChillRecord;
import chill.utils.TheMissingUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.model.User;
import test.model.Vehicle;
import test.model.many.ManyA;
import test.model.many.ManyB;
import test.model.many.ManyC;

import java.sql.DriverManager;

import static chill.db.ChillRecordTest.TEST_DB_URL;
import static chill.utils.TheMissingUtils.n;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ManyTest {

    @BeforeAll
    public static void beforeAll() throws Exception {
        Class.forName("org.h2.Driver");
        ChillRecord.connectionSource = () -> DriverManager.getConnection(TEST_DB_URL);
    }

    @BeforeEach
    public void beforeEach() {
        ChillRecord.executeUpdate(ManyA.DDL);
        ChillRecord.executeUpdate(ManyB.DDL);
        ChillRecord.executeUpdate(ManyC.DDL);
    }

    @Test
    public void basicManyTestWorks() {
        ManyA manyA = new ManyA();
        manyA.save();

        manyA.getManyBs().newRecord().save();
        manyA.getManyBs().newRecord().save();
        manyA.getManyBs().newRecord().save();

        assertEquals(3, manyA.getManyBs().count());
    }

    @Test
    public void basicManyThroughWorks() {
        ManyA manyA = new ManyA();
        manyA.save();

        n(3).times(() -> {
            ManyB b = manyA.getManyBs().newRecord().createOrThrow();
            n(3).times(() -> {
                b.getManyCs().newRecord().createOrThrow();
            });
        });

        assertEquals(3, manyA.getManyBs().count());
        assertEquals(9, manyA.getManyCs().count());
    }

    @Test
    public void cantCreateNewRecordWithManyThrough() {
        ManyA manyA = new ManyA();
        manyA.save();

        try {
            manyA.getManyCs().newRecord();
            fail("Shouldn't have worked");
        } catch (IllegalStateException ise) {
            // good
        }
    }
}