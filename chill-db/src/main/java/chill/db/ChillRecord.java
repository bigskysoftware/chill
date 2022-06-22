package chill.db;

import chill.utils.ChillLogs;
import chill.utils.NiceList;
import chill.utils.TheMissingUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static chill.utils.TheMissingUtils.*;

@SuppressWarnings("rawtypes")
public class ChillRecord {

    public static ConnectionSource connectionSource = null;

    private static boolean SHOULD_LOG = true;
    private ChillLogs.LogCategory log = ChillLogs.get(this.getClass());

    String tableName = TheMissingUtils.snake(this.getClass().getSimpleName());
    LinkedList<ChillField> fields = new LinkedList<>();
    private ChillValidation.Errors errors;
    /* package */ boolean persisted;
    private Timestamp currentTimestamp;

    public static void executeUpdate(String sql) {
        safely(() -> {
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
            }
        });
    }

    public static LinkedList<LinkedHashMap<String, Object>> executeQuery(String sql) {
        return safely(() -> {
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                ResultSet resultSet = stmt.executeQuery(sql);
                LinkedList<LinkedHashMap<String, Object>> results = new LinkedList<>();
                ResultSetMetaData metaData = resultSet.getMetaData();
                while (resultSet.next()) {
                    LinkedHashMap<String, Object> result = new LinkedHashMap<>();
                    for (int i = 0; i < metaData.getColumnCount(); i++) {
                        String columnName = metaData.getColumnName(i + 1);
                        result.put(columnName, resultSet.getObject(columnName));
                    }
                    results.add(result);
                }
                return results;
            }
        });
    }

    private static Connection getConnection() throws SQLException {
        Connection currentTransactionConnection = ChillTransaction.currentTransactionConnection();
        if (currentTransactionConnection != null) {
            return currentTransactionConnection;
        } else {
            return getRawConnection();
        }
    }

    private static Connection getRawConnection() throws SQLException {
        return connectionSource.getConnection();
    }

    public static void inTransaction(Runnable run){
        ChillTransaction currentTransaction = ChillTransaction.getCurrentTransaction();
        if (currentTransaction == null) {
            ChillTransaction transaction = ChillTransaction.start(safely(() -> getRawConnection()));
            try {
                run.run();
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw forceThrow(e);
            } finally {
                transaction.close();
            }
        } else {
            currentTransaction.join();
            run.run();
        }
    }


    public String getTableName() {
        return tableName;
    }

    protected void tableName(String tableName) {
        this.tableName = tableName;
    }

    public NiceList<ChillField> getFields() {
        return nice(fields);
    }

    /*package*/ NiceList<ChillField> getDBFields() {
        return new NiceList<>(fields).filter(ChillField::isInDatabase);
    }

    public static String makeQueryLog(String type, String sql, List args) {
        return type + " -- \n" + Arrays.stream(sql.split("\n")).map(s -> "  " + s).collect(Collectors.joining("\n")) + (args.size() > 0 ? "\n  Args: " + args : "");
    }

    //==========================================================
    //  Validation
    //==========================================================

    public boolean isValid() {
        this.errors = new ChillValidation.Errors(this);
        for (ChillField field : fields) {
            field.validate(errors);
        }
        return !errors.hasErrors();
    }

    //==========================================================
    //  Events
    //==========================================================

    private void beforeCreateImpl() {
        beforeCreate();
        fields.forEach(ChillField::beforeCreate);
    }

    protected void beforeCreate() { /* to be overridden */}

    private void beforeUpdateImpl() {
        beforeUpdate();
        fields.forEach(ChillField::beforeUpdate);
    }

    protected void beforeUpdate() { /* to be overridden */}

    //==========================================================
    //  C[R]UD - For Read, see the Finder API
    //==========================================================

    public boolean save() {
        if (persisted) {
            return update() == 1;
        } else {
            return create();
        }
    }

    protected ChillRecord firstOrCreateImpl() {
        ChillQuery chillQuery = new ChillQuery(this);
        ChillRecord first = chillQuery.first();
        if (first != null) {
            return first;
        } else {
            boolean created = create();
            if (created) {
                return this;
            } else {
                throw new ChillValidation.ValidationException(getErrors());
            }
        }
    }

    public boolean create() {
        if (!isValid()) {
            return  false;
        }
        return safely(() -> {
            try (var connection = getConnection();
                 var ignored = new TimestampCloseable()) {

                beforeCreateImpl();

                var fields = getDBFields();
                var nonNullFields = fields.filter(ChillField::hasValue);

                String fieldNames = nonNullFields.map(ChillField::getColumnName).join(",");
                String queryVars = nonNullFields.map(field -> "?").join(",");

                var sql = "INSERT INTO " + getTableName() + "(" + fieldNames + ")\n" +
                        "VALUES (" + queryVars + ")";

                var values = nonNullFields.map(field -> field.get());

                if (shouldLog()) {
                    info(makeQueryLog("INSERT", sql, values));
                }

                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                int col = 1;
                for (ChillField field : nonNullFields) {
                    preparedStatement.setObject(col++, field.rawValue());
                }

                int inserted = preparedStatement.executeUpdate();

                // Extract any autogenerated values
                var generatedKeys = preparedStatement.getGeneratedKeys();
                var next = generatedKeys.next();
                var generatedFields = fields.filter(ChillField::isGenerated);
                col = 1;
                for (ChillField generatedField : generatedFields) {
                    generatedField.set(generatedKeys.getObject(col++, generatedField.getType()));
                }
                this.persisted = inserted == 1;
                if (this.persisted) {
                    fields.forEach(ChillField::updateLastSaved);
                }
                return this.persisted;
            }
        });
    }

    public <T extends ChillRecord> Finder<T> read() {
        return (Finder) finder(this.getClass());
    }

    public int update() {
        return safely(() -> {
            try (var connection = getConnection();
                 var ignored = new TimestampCloseable()) {

                beforeUpdateImpl();

                NiceList<ChillField> fields = getDBFields();

                var nonGeneratedFields = fields.filter(field -> !field.isGenerated());
                String updateClauses = nonGeneratedFields.map(field -> field.getColumnName() + "=?").join(", ");
                var sql = "UPDATE " + getTableName() + " SET " + updateClauses;

                var primaryKeys = fields.filter(ChillField::isPrimaryKey);
                var where = primaryKeys.map(field -> field.getColumnName() + "=?").join(" AND ");

                var optimisticLocks = fields.filter(ChillField::isOptimisticLock);
                if (primaryKeys.size() > 0 && optimisticLocks.size() > 0) {
                    where += " AND ";
                }
                where += optimisticLocks.map(field -> field.getColumnName() + "=?").join(" AND ");

                sql = sql + "\nWHERE " + where;

                var values = nonGeneratedFields.concat(primaryKeys).map(ChillField::get).concat(optimisticLocks.map(ChillField::lastSavedValue));
                if (shouldLog()) {
                    info(makeQueryLog("UPDATE", sql, values));
                }

                var preparedStatement = connection.prepareStatement(sql);
                var col = 1;
                for (var field : nonGeneratedFields) {
                    preparedStatement.setObject(col++, field.rawValue());
                }
                for (var field : primaryKeys) {
                    preparedStatement.setObject(col++, field.rawValue());
                }
                for (var field : optimisticLocks) {
                    preparedStatement.setObject(col++, field.rawLastSavedValue());
                }

                var updated = preparedStatement.executeUpdate();

                // TODO we should not throw here
                if (optimisticLocks.size() > 0 && updated == 0) {
                    throw new OptimisticConcurrencyFailure();
                } else if (updated >= 1) {
                    fields.forEach(ChillField::updateLastSaved);
                }

                return updated;
            }
        });
    }

    public int delete() {
        return safely(() -> {
            try (Connection connection = getConnection()) {
                var sql = "DELETE FROM  " + getTableName();

                var primaryKeys = getFields().filter(ChillField::isPrimaryKey);
                var where = primaryKeys.map(field -> field.getColumnName() + "=?").join(" AND ");
                sql = sql + "\nWHERE " + where;

                if (shouldLog()) {
                    info(sql);
                }

                var preparedStatement = connection.prepareStatement(sql);
                var col = 1;
                for (var field : primaryKeys) {
                    preparedStatement.setObject(col++, field.get());
                }

                var updated = preparedStatement.executeUpdate();
                return updated;
            }
        });
    }

    public static <T extends ChillRecord> void populateFromResultSet(T record, ResultSet resultSet) {
        record.persisted = true;
        var fields = record.getDBFields();
        for (ChillField field : fields) {
            field.fromResultSet(resultSet);
        }
    }

    public static <T extends ChillRecord> void populateFromWebParams(T record, Map<String, String> parameters, String... fieldsToTake) {
        var fields = record.getDBFields().toMap(ChillField::getColumnName);
        for (String fieldName : fieldsToTake) {
            ChillField chillField = fields.get(fieldName);
            if (chillField != null) {
                String stringValue = parameters.get(fieldName);
                chillField.setFromString(stringValue);
            } else {
                throw new IllegalStateException("No such field with column name " + fieldName);
            }
        }
    }

    public static <T extends ChillRecord> void populateFromHashMap(T record, Map<String, Object> hashMap) {
        var fields = record.getDBFields();
        for (ChillField field : fields) {
            field.fromHashMap(hashMap);
        }
    }

    //==========================================================
    //  Finder API
    //==========================================================

    protected static <T extends ChillRecord> Finder<T> finder(Class<T> clazz) {
        return new Finder<T>(clazz);
    }

    //==========================================================
    //  Field Building API
    //==========================================================

    protected <T> ChillField<T> field(String columnName, Class<T> type) {
        return new ChillField<T>(this, columnName, type);
    }

    protected ChillField<Long> pk(String column) {
        return field(column, Long.class).autoGenerated().primaryKey();
    }

    protected <S extends ChillRecord, T> ChillField.FK<S, T> fk(String columnName, Class<T> type) {
        return new ChillField.FK<>(this.getClass(), this, columnName, type);
    }

    protected <S extends ChillRecord, T extends ChillRecord> ChillField.FK<S, T> fk(String columnName, Class<T> type, String otherColumn) {
        return new ChillField.FK<S, T>(this.getClass(), this, columnName, type).withColumn(otherColumn);
    }

    protected <T extends ChillRecord> ChillField.Many<T, ChillQuery<T>> many(Class<T> componentType, String otherColumn) {
        return new ChillField.Many<>(this.getClass(), this, componentType, otherColumn);
    }

    protected ChillField<String> email(String columnName) {
        return field(columnName, String.class).email();
    }

    protected ChillField<String> password(String columnName) {
        return field(columnName, String.class).password();
    }

    protected ChillField<String> uuid(String columnName) {
        return field(columnName, String.class).uuid();
    }

    protected ChillField<Timestamp> createdAt(String columnName) {
        return field(columnName, Timestamp.class).beforeCreate(dateField -> dateField.set(dateField.getRecord().currentTimestamp())).readOnly();
    }

    private Timestamp currentTimestamp() {
        if (currentTimestamp != null) {
            return currentTimestamp;
        } else {
            throw new IllegalStateException("No current timestamp!");
        }
    }

    protected ChillField<Timestamp> updatedAt(String columnName) {
        return field(columnName, Timestamp.class)
                .beforeCreate(dateField -> dateField.set(dateField.getRecord().currentTimestamp()))
                .beforeUpdate(dateField -> dateField.set(dateField.getRecord().currentTimestamp())).readOnly();
    }

    public ChillValidation.Errors getErrors() {
        return errors;
    }

    public interface ConnectionSource {
        Connection getConnection() throws SQLException;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + TheMissingUtils.join(fields, ", ") + ")";
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Generated {
    }

    public static class Finder<T extends ChillRecord> {
        private final Class<T> clazz;

        public Finder(Class<T> clazz) {
            this.clazz = clazz;
        }

        public T first() {
            return where().first();
        }
        public T firstWhere(Object... conditions) {
            return new ChillQuery<T>(clazz).firstWhere(conditions);
        }

        public ChillQuery<T> all() {
            return where();
        }

        public T byPrimaryKey(Object... keys) {
            return new ChillQuery<T>(clazz).findByPrimaryKey(keys);
        }

        public ChillQuery<T> where(Object... conditions) {
            return new ChillQuery<T>(clazz).where(conditions);
        }

        public T one(Object... conditions) {
            return new ChillQuery<T>(clazz).where(conditions).first();
        }

        public T by(String column, Object val) {
            return new ChillQuery<T>(clazz).where(column, val).first();
        }
    }

    public static void codeGen() {

        StackTraceElement trace[] = Thread.currentThread().getStackTrace();
        Class templateClass;
        Object instance;
        if (trace.length > 0) {
            templateClass = safely(() -> Class.forName(trace[trace.length - 1].getClassName()));
            instance = TheMissingUtils.newInstance(templateClass);
        } else {
            throw new IllegalStateException("Could not determine class to code gen for!");
        }

        String newLine = "\n    ";
        StringBuilder sb = new StringBuilder()
                .append(newLine)
                .append("//region chill.Record GENERATED CODE").append(newLine)
                .append(newLine);

        // Class logger
        String className = templateClass.getSimpleName().replace("$", ".");

        // createOrThrow()
        sb.append("public ").append(className).append(" createOrThrow(){").append(newLine)
                .append("  if(!create()){").append(newLine)
                .append("    throw new chill.db.ChillValidation.ValidationException(getErrors());")
                .append(newLine).append("  }").append(newLine)
                .append("  return this;").append(newLine)
                .append("}").append(newLine)
                .append(newLine);

        // saveOrThrow()
        sb.append("public ").append(className).append(" saveOrThrow(){").append(newLine)
                .append("  if(!save()){").append(newLine)
                .append("    throw new chill.db.ChillValidation.ValidationException(getErrors());")
                .append(newLine).append("  }").append(newLine)
                .append("  return this;").append(newLine)
                .append("}").append(newLine)
                .append(newLine);

        // firstOrCreateOrThrow()
        sb.append("public ").append(className).append(" firstOrCreateOrThrow(){").append(newLine)
                .append("  return (").append(className).append(") firstOrCreateImpl();").append(newLine)
                .append("}").append(newLine)
                .append(newLine);

        // fromWebParams()
        sb.append("@chill.db.ChillRecord.Generated ")
                .append("public ")
                .append(className).append(" fromWebParams(java.util.Map<String, String> values, String... params) {").append(newLine)
                .append("  ChillRecord.populateFromWebParams(this, values, params);").append(newLine)
                .append("  return this;").append(newLine)
                .append("}").append(newLine)
                .append(newLine);

        java.lang.reflect.Field[] classFields = templateClass.getDeclaredFields();
        for (java.lang.reflect.Field javaField : classFields) {
            if (ChillField.class.isAssignableFrom(javaField.getType())) {
                javaField.setAccessible(true);
                ChillField chillField = (ChillField) safely(() -> javaField.get(instance));

                String propName = TheMissingUtils.capitalize(javaField.getName());
                Method existingGetter = safelyOr(() -> templateClass.getMethod("get" + propName), null);
                boolean overriddenGetter = existingGetter != null && existingGetter.getAnnotation(Generated.class) == null;
                sb.append("@chill.db.ChillRecord.Generated ")
                        .append(overriddenGetter ? "protected " : "public ")
                        .append(chillField.getTypeName()).append(" get")
                        .append(propName)
                        .append(overriddenGetter ? "Internal" : "").append("() {").append(newLine)
                        .append("  return ").append(javaField.getName()).append(".get();").append(newLine)
                        .append("}").append(newLine)
                        .append(newLine);

                if (!chillField.isReadOnly()) {
                    Method existingSetter = safelyOr(() -> templateClass.getMethod("set" + propName, chillField.getType()), null);
                    boolean overriddenSetter = existingSetter != null && existingSetter.getAnnotation(Generated.class) == null;
                    sb.append("@chill.db.ChillRecord.Generated ")
                            .append(overriddenSetter ? "protected" : "public").append(" void set")
                            .append(propName)
                            .append(overriddenSetter ? "Internal" : "")
                            .append("(").append(chillField.getType().getSimpleName()).append(" ")
                            .append(javaField.getName()).append(") {").append(newLine)
                            .append("  this.").append(javaField.getName()).append(".set(").append(javaField.getName()).append(");").append(newLine)
                            .append("}").append(newLine)
                            .append(newLine);

                    Method existingWith = safelyOr(() -> templateClass.getMethod("with" + propName, chillField.getType()), null);
                    boolean overriddenWith = existingWith != null && existingWith.getAnnotation(Generated.class) == null;
                    sb.append("@chill.db.ChillRecord.Generated ")
                            .append(overriddenWith ? "protected " : "public ")
                            .append(className).append(" with")
                            .append(propName)
                            .append(overriddenWith ? "Internal" : "")
                            .append("(").append(chillField.getType().getSimpleName()).append(" ")
                            .append(javaField.getName()).append(") {").append(newLine)
                            .append("  set").append(propName).append("(").append(javaField.getName()).append(");").append("").append(newLine)
                            .append("  return this;").append(newLine)
                            .append("}").append(newLine)
                            .append(newLine);
                }
                if (chillField instanceof ChillField.FK) {
                    Method forFK = safelyOr(() -> templateClass.getMethod("for" + chillField.getType().getSimpleName(), chillField.getType()), null);
                    boolean overridenFK = forFK != null && forFK.getAnnotation(Generated.class) == null;
                    sb.append("@chill.db.ChillRecord.Generated ")
                            .append(overridenFK ? "protected " : "public static chill.db.ChillQuery<")
                            .append(className).append("> for")
                            .append(chillField.getType().getSimpleName())
                            .append(overridenFK ? "Internal" : "")
                            .append("(").append(chillField.getType().getSimpleName()).append(" ")
                            .append(javaField.getName()).append(") {").append(newLine)
                            .append("  return new ").append(className).append("().").append(javaField.getName()).append(".reverse(").append(javaField.getName()).append(");").append(newLine)
                            .append("}").append(newLine)
                            .append(newLine);
                }
                if (chillField.isPassword()) {
                    Method forFK = safelyOr(() -> templateClass.getMethod("for" + chillField.getType().getSimpleName(), chillField.getType()), null);
                    boolean overridenFK = forFK != null && forFK.getAnnotation(Generated.class) == null;
                    sb.append("@chill.db.ChillRecord.Generated ")
                            .append(overridenFK ? "protected " : "public boolean ")
                            .append(javaField.getName())
                            .append("Matches")
                            .append(overridenFK ? "Internal" : "")
                            .append("(String passwd) {").append(newLine)
                            .append("  return ").append(javaField.getName()).append(".passwordMatches(passwd);").append(newLine)
                            .append("}").append(newLine)
                            .append(newLine);
                }
            }
        }
        sb.append("public static final chill.db.ChillRecord.Finder<").append(className).append("> find = finder(").append(className).append(".class);").append(newLine)
                .append(newLine);

        sb.append("//endregion").append(newLine);

        System.out.println(sb.toString());
    }

    // manages timestamps during update/insert
    private class TimestampCloseable implements AutoCloseable{
        public TimestampCloseable() {
            currentTimestamp = new Timestamp(Instant.now().toEpochMilli());
        }

        @Override
        public void close() {
            currentTimestamp = null;
        }
    }


    public static boolean shouldLog() {
        return SHOULD_LOG;
    }

    public static SafeAutoCloseable quietly() {
        SHOULD_LOG = false;
        return () -> SHOULD_LOG = true;
    }

    //=====================================
    // Log delegation
    //=====================================
    protected void trace(ToString msg) {
        log.trace(msg);
    }
    protected void trace(Object msg) {
        log.trace(msg);
    }
    protected void trace(String format, Object... argArray) {
        log.trace(format, argArray);
    }

    protected void debug(ToString msg) {
        log.debug(msg);
    }
    protected void debug(Object msg) {
        log.debug(msg);
    }
    protected void debug(String format, Object... argArray) {
        log.debug(format, argArray);
    }

    protected void info(ToString msg) {
        log.info(msg);
    }
    protected void info(Object msg) {
        log.info(msg);
    }
    protected void info(String format, Object... argArray) {
        log.info(format, argArray);
    }

    protected void warn(ToString msg) {
        log.warn(msg);
    }
    protected void warn(Object msg) {
        log.warn(msg);
    }
    protected void warn(String format, Object... argArray) {
        log.warn(format, argArray);
    }

    protected void error(Throwable throwable) {
        log.error(throwable);
    }
    protected void error(ToString msg) {
        log.error(msg);
    }
    protected void error(Object msg) {
        log.error(msg);
    }
    protected void error(String format, Object... argArray) {
        log.error(format, argArray);
    }

    public static class OptimisticConcurrencyFailure extends RuntimeException {}
}