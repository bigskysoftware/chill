package chill.db;

import chill.utils.ChillLogs;
import chill.utils.NiceList;
import chill.utils.TheMissingUtils;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

import static chill.utils.TheMissingUtils.safely;

public class ChillMigrations {

    public static final String MIGRATIONS_FILE = "model.Migrations";
    static ChillLogs.LogCategory LOG = ChillLogs.get(ChillMigrations.class);
    private ChillMigrations migrationsFile = null;
    private NiceList<ChillMigration> allMigrations = null;
    private NiceList<ChillMigrations> subMigrationFiles = new NiceList<>();

    public static void checkPendingMigrations(boolean dropToConsole) {
        try(var ignore = ChillRecord.quietly()) {
            Class.forName(MIGRATIONS_FILE);
            ChillMigrations migrations = new ChillMigrations(MIGRATIONS_FILE);
            if (migrations.hasPending()) {
                if (dropToConsole) {
                    LOG.info("Pending migrations found, dropping to migration console:");
                    System.out.println(migrations.getPendingMigrations());
                    migrations.console(true);
                } else {
                    LOG.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    LOG.warn("!! Pending migrations found!  Please migrate the DB!             !!");
                    LOG.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    System.out.println(migrations.getPendingMigrations());
                }
            } else {
                LOG.info("No pending migrations found...");
            }
        } catch (ClassNotFoundException e) {
            LOG.info("No class named " + MIGRATIONS_FILE + " found, nothing to migrate");
        }
    }

    private boolean hasPending() {
        return pending().size() > 0;
    }

    protected void include(ChillMigrations secondaryMigrationFile) {
        subMigrationFiles.add(secondaryMigrationFile);
    }

    protected ChillMigrations() { /* for subclasses */ }

    public ChillMigrations(String migrationsClassName) {
        try {
            migrationsFile = TheMissingUtils.newInstance(migrationsClassName);
            allMigrations = getMigrations();
            bootstrap();
        } catch (Exception e) {
            // TODO better error state checking here
            allMigrations = new NiceList<>();
            LOG.error("Unable to load migrations file", e);
        }
    }

    public static void generateNewMigration() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Please enter a brief description of the migration: ");
        String description = scanner.nextLine();
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String datePrefix = formatter.format(date);
        System.out.println("  public final ChillMigration migration_" + datePrefix + " = new ChillMigration(\"" +
                description
                + "\"){\n" +
                "        protected void up() {\n" +
                "        }\n" +
                "        protected void down() {\n" +
                "        }\n" +
                "    };");
    }

    public static void execute(String migrationCommand) {
        try {
            Class.forName(MIGRATIONS_FILE);
            ChillMigrations migrations = new ChillMigrations(MIGRATIONS_FILE);
            if (migrationCommand.isEmpty() || migrationCommand.equals("console")) {
                migrations.console();
            } else {
                migrations.execCommand(migrationCommand);
            }
        } catch (ClassNotFoundException e) {
            LOG.info("No class named " + MIGRATIONS_FILE + " found, nothing to migrate");
        }
    }

    public boolean execCommand(String migrationCommand) {
        try (var ignored = ChillRecord.quietly()) {
            String command = migrationCommand.strip();
            try {
                if (command.equals("")) {
                    // ignore
                } else if (command.equals("exit")) {
                    return true;
                } else if (command.equals("?") || command.equals("help")) {
                    System.out.println("help or ? - print this help message\n" +
                            "exit      - exit the tool\n" +
                            "status    - show current migration status\n" +
                            "pending   - show pending migration status\n" +
                            "up        - migrate up one step\n" +
                            "up:<n>    - migrate up n steps\n" +
                            "up:*      - migrate up all pending steps\n" +
                            "down      - migrate down one step\n" +
                            "down:<n>  - migrate down n steps\n" +
                            "skip      - migrate skip one step\n" +
                            "new       - generate code for a new migration\n");
                } else if (command.equals("new")) {
                    generateNewMigration();
                } else if (command.equals("status")) {
                    System.out.println(getStatus());
                } else if (command.equals("pending")) {
                    System.out.println(getPendingMigrations());
                } else if (command.equals("up")) {
                    up();
                } else if (command.equals("down")) {
                    down();
                } else if (command.equals("up:*")) {
                    applyAll();
                } else {
                    System.out.println("Unknown command : " + command);
                }
            } catch (Exception e) {
                System.out.println("An exception occurred: " + e.getMessage());
                e.printStackTrace();
                System.out.println("\n");
            }
            return false;
        }
    }

    public void console() {
        console(false);
    }

    public void console(boolean exitOnNoPending) {
        System.out.println("Welcome to Chill Migrations.  Please enter a command or '?' for help");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.flush();
            System.out.print("chill-migrations $> ");
            String line = scanner.nextLine();
            if (execCommand(line)) {
                return;
            }
            if (exitOnNoPending && !hasPending()) {
                System.out.println("Schema up to date... exiting migrations console");
                return;
            }
        }
    }

    public void bootstrap() {
        MigrationRecord.bootstrap();
    }

    public NiceList<ChillMigration> all() {
        return allMigrations;
    }

    public NiceList<ChillMigration> pending() {
        try(var ignore = ChillRecord.quietly()) {
            return allMigrations.filter(chillMigration -> chillMigration.getStatus() == MigrationStatus.PENDING);
        }
    }

    public NiceList<ChillMigration> applied() {
        try (var ignore = ChillRecord.quietly()) {
            return allMigrations.filter(chillMigration -> chillMigration.getStatus() == MigrationStatus.APPLIED);
        }
    }

    public NiceList<ChillMigration> skipped() {
        try (var ignore = ChillRecord.quietly()) {
            return allMigrations.filter(chillMigration -> chillMigration.getStatus() == MigrationStatus.SKIPPED);
        }
    }

    public String getStatus() {
        StringBuilder sb = new StringBuilder("All Migrations:\n");
        String formatString = "%-30s | %-30s | %-10s | %-25s | %-45s\n";
        sb.append(String.format(formatString, "Class", "Migration Name", "Status", "Date Applied", "Description"));
        sb.append("------------------------------------------------------------------------------------------------------------------------------------------\n");
        for (ChillMigration migration : allMigrations) {
            sb.append(String.format(formatString, migration.getOwningClassName(), migration.getName(), migration.getStatus(), migration.getApplicationDate(), migration.getDescription()));
        }
        return sb.toString();
    }

    public String getPendingMigrations() {
        StringBuilder sb = new StringBuilder("Pending Migrations:\n");
        String formatString = "%-30s | %-30s | %-10s | %-25s | %-45s\n";
        sb.append(String.format(formatString, "Class", "Migration Name", "Status", "Date Applied", "Description"));
        sb.append("------------------------------------------------------------------------------------------------------------------------------------------\n");
        for (ChillMigration migration : pending()) {
            sb.append(String.format(formatString, migration.getOwningClassName(), migration.getName(), migration.getStatus(), migration.getApplicationDate(), migration.getDescription()));
        }
        return sb.toString();
    }

    private NiceList<ChillMigration> getMigrations() {
        ChillMigrations migrationsFile = this.migrationsFile;
        NiceList<ChillMigration> migrations = new NiceList<>();
        migrationsFile.addMigrations(migrations);
        return migrations.sortBy(ChillMigration::getName);
    }

    private void addMigrations(NiceList<ChillMigration> migrations) {
        Field[] declaredFields = this.getClass().getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (ChillMigration.class.isAssignableFrom(declaredField.getType())) {
                declaredField.setAccessible(true);
                ChillMigration value = (ChillMigration) safely(() -> declaredField.get(this));
                value.setName(declaredField.getName());
                value.setOwnerClass(this.getClass());
                migrations.add(value);
            }
        }
        for (ChillMigrations subMigrationFile : this.subMigrationFiles) {
            subMigrationFile.addMigrations(migrations);
        }
    }

    public void up() {
        ChillMigration first = pending().first();
        if (first != null) {
            first.migrateUp();
        }
    }

    public void down() {
        ChillMigration first = applied().last();
        if (first != null) {
            first.migrateDown();
        }
    }

    public void applyAll() {
        ChillRecord.inTransaction(() -> {
            for (ChillMigration chillMigration : pending()) {
                chillMigration.migrateUp();
            }
        });
    }

    protected abstract static class ChillMigration {
        private final String description;
        private String name;
        private Class ownerClass;

        public String getName() {
            return name;
        }

        protected void setName(String name) {
            this.name = name;
        }

        protected void setOwnerClass(Class clazz) {
            ownerClass = clazz;
        }

        public ChillMigration(String description) {
            this.description = description;            
        }

        protected void exec(String sql) {
            System.out.println("Executing SQL : \n\n" + sql);
            ChillRecord.executeUpdate(sql);
        }

        protected void file(String filePath) {
            InputStream resourceAsStream = getClass().getResourceAsStream(filePath);
            if (resourceAsStream == null) {
                throw new IllegalStateException("Could not find file " + filePath);
            }
            String text = new String(safely(resourceAsStream::readAllBytes), StandardCharsets.UTF_8);
            exec(text);
        }

        public final void migrateUp() {
            System.out.println("Migrating " + name  + " up...");
            try (var ignore = ChillRecord.quietly()) {
                ChillRecord.inTransaction(() -> {
                    up();
                    new MigrationRecord().withDescription(description).withName(name).withStatus(MigrationStatus.APPLIED).create();
                });
            }
            System.out.println("Done");
        }

        protected abstract void up();

        public final void migrateDown() {
            System.out.println("Migrating " + name + " down...");
            try (var ignore = ChillRecord.quietly()) {
                ChillRecord.inTransaction(() -> {
                    down();
                    MigrationRecord record = getMigrationRecord();
                    record.delete();
                });
            }
            System.out.println("Done");
        }

        protected abstract void down();

        public MigrationStatus getStatus() {
            MigrationRecord record = getMigrationRecord();
            if (record == null) {
                return MigrationStatus.PENDING;
            } else {
                return record.getStatus();
            }
        }

        private MigrationRecord getMigrationRecord() {
            try(var ignore = ChillRecord.quietly()) {
                return MigrationRecord.find.where("name", this.name).first();
            }
        }

        public String getOwningClassName() {
            return ownerClass.getSimpleName();
        }

        public String getDescription() {
            return description;
        }

        public String getApplicationDate() {
            MigrationRecord migrationRecord = getMigrationRecord();
            if (migrationRecord != null) {
                return migrationRecord.getCreatedAt().toString();
            } else {
                return null;
            }
        }
    }
    
    public static class MigrationRecord extends ChillRecord {

        public static final String BOOTSTRAP_DDL = "CREATE TABLE IF NOT EXISTS migrations (\n" +
                "              id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "              name VARCHAR(250),\n" +
                "              created_at VARCHAR(250),\n" +
                "              description VARCHAR(250),\n" +
                "              status VARCHAR(250)\n" +
                "            );\n" +
                "            ";


        public static void bootstrap() {
            executeUpdate(BOOTSTRAP_DDL);
        }

        {tableName("migrations");}

        ChillField<Long> id = pk("id");
        ChillField<String> name = field("name", String.class);
        ChillField<Timestamp> createdAt = createdAt("created_at");
        ChillField<String> description = field("description", String.class);
        ChillField<MigrationStatus> status = field("status", MigrationStatus.class);

        //region chill.Record GENERATED CODE

        public MigrationRecord createOrThrow(){
            if(!create()){
                throw new chill.db.ChillValidation.ValidationException(getErrors());
            }
            return this;
        }

        public MigrationRecord saveOrThrow(){
            if(!save()){
                throw new chill.db.ChillValidation.ValidationException(getErrors());
            }
            return this;
        }

        public MigrationRecord firstOrCreateOrThrow(){
            return (MigrationRecord) firstOrCreateImpl();
        }

        @chill.db.ChillRecord.Generated public Long getId() {
            return id.get();
        }

        @chill.db.ChillRecord.Generated public String getName() {
            return name.get();
        }

        @chill.db.ChillRecord.Generated public void setName(String name) {
            this.name.set(name);
        }

        @chill.db.ChillRecord.Generated public MigrationRecord withName(String name) {
            setName(name);
            return this;
        }

        @chill.db.ChillRecord.Generated public Timestamp getCreatedAt() {
            return createdAt.get();
        }

        @chill.db.ChillRecord.Generated public String getDescription() {
            return description.get();
        }

        @chill.db.ChillRecord.Generated public void setDescription(String description) {
            this.description.set(description);
        }

        @chill.db.ChillRecord.Generated public MigrationRecord withDescription(String description) {
            setDescription(description);
            return this;
        }

        @chill.db.ChillRecord.Generated public MigrationStatus getStatus() {
            return status.get();
        }

        @chill.db.ChillRecord.Generated public void setStatus(MigrationStatus status) {
            this.status.set(status);
        }

        @chill.db.ChillRecord.Generated public MigrationRecord withStatus(MigrationStatus status) {
            setStatus(status);
            return this;
        }

        public static final chill.db.ChillRecord.Finder<MigrationRecord> find = finder(MigrationRecord.class);

        //endregion

    }

    public enum MigrationStatus {
        PENDING,
        APPLIED,
        SKIPPED
    }

}
