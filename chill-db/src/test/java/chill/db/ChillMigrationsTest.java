package chill.db;

import chill.db.migrations.BootstrapMigrationFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import test.model.User;

import java.sql.DriverManager;
import java.sql.SQLException;

import static chill.db.ChillMigrations.MigrationStatus.PENDING;
import static org.junit.jupiter.api.Assertions.*;

public class ChillMigrationsTest {

    public static final String TEST_DB_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;CASE_INSENSITIVE_IDENTIFIERS=true";

    @BeforeAll
    public static void beforeAll() throws Exception {
        Class.forName("org.h2.Driver");
        ChillRecord.connectionSource = () -> DriverManager.getConnection(TEST_DB_URL);
    }

    @AfterEach
    public void dropDB(){
        ChillRecord.executeUpdate("DROP ALL OBJECTS");
    }

    @Test
    public void bootstrapWorks() {
        ChillMigrations migrations = new ChillMigrations(BootstrapMigrationFile.class.getName());

        var allMigrations = migrations.all();
        assertEquals(1, allMigrations.size());
        assertEquals("migration_2022_03_08_16_02_26", allMigrations.get(0).getName());
        assertEquals(PENDING, allMigrations.get(0).getStatus());

        var pending = migrations.pending();
        assertEquals(1, pending.size());
        assertEquals("migration_2022_03_08_16_02_26", allMigrations.get(0).getName());

        var applied = migrations.applied();
        assertEquals(0, applied.size());

        migrations.up();

        pending = migrations.pending();
        assertEquals(0, pending.size());

        applied = migrations.applied();
        assertEquals(1, applied.size());

        // should be able to insert a User now
        new User().withEmail("test@test.com").saveOrThrow();

        migrations.down();

        pending = migrations.pending();
        assertEquals(1, pending.size());
        assertEquals("migration_2022_03_08_16_02_26", allMigrations.get(0).getName());

        applied = migrations.applied();
        assertEquals(0, applied.size());

        // should throw an error now
        try {
            new User().withEmail("test@test.com").saveOrThrow();
            fail("Should have thrown, table no longer exists!");
        } catch (Exception e) {
            assertTrue(SQLException.class.isAssignableFrom(e.getClass()));
        }
    }

    @Test
    public void migrateAllWorks() {
        ChillMigrations migrations = new ChillMigrations(BootstrapMigrationFile.class.getName());
        migrations.applyAll();
        new User().withEmail("test@test.com").saveOrThrow();
    }

    @Test
    public void bootstrapPrinting() {
        ChillMigrations migrations = new ChillMigrations(BootstrapMigrationFile.class.getName());
        System.out.println(migrations.getStatus());
        migrations.applyAll();
        new User().withEmail("test@test.com").saveOrThrow();
        System.out.println(migrations.getStatus());
    }

}
