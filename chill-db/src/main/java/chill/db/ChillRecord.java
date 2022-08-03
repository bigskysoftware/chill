package chill.db;

import chill.utils.ChillLogs;
import chill.utils.NiceList;
import chill.utils.NiceMap;
import chill.utils.TheMissingUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
    private boolean prototype = false;

    LinkedList<ChillField.Many> manys = new LinkedList<>();

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

    private static final Map<Class, ChillRecord> PROTOTYPE = new ConcurrentHashMap<>();
    public static <T extends ChillRecord> T getPrototype(Class<T> clazz) {
        ChillRecord o = PROTOTYPE.computeIfAbsent(clazz, aClass -> {
            ChillRecord record = (ChillRecord) newInstance(aClass);
            record.markAsPrototype();
            return record;
        });
        return (T) o;
    }

    private void markAsPrototype() {
        this.prototype = true;
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

    public NiceMap<String, ChillField> getFieldsAsMap() {
        return getFields().toMap(chillField -> chillField.getColumnName());
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

    public boolean isRecordValid() {
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
        beforeSaveImpl();
        beforeCreate();
        fields.forEach(ChillField::beforeCreate);
    }

    protected void beforeCreate() { /* to be overridden */}

    private void beforeUpdateImpl() {
        beforeSaveImpl();
        beforeUpdate();
        fields.forEach(ChillField::beforeUpdate);
    }

    protected void beforeUpdate() { /* to be overridden */}

    private void beforeSaveImpl() {
        beforeSave();
        fields.forEach(ChillField::beforeSave);
    }
    protected void beforeSave() { /* to be overridden */ }

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
        if (!isRecordValid()) {
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

    protected <S extends ChillRecord, T extends ChillRecord> ChillField.FK<S, T> fk( Class<T> type) {
        T prototype = getPrototype(type);
        return fk(prototype.getTableName() + "_id", type);
    }

    protected <S extends ChillRecord, T extends ChillRecord> ChillField.FK<S, T> fk(String columnName, Class<T> type) {
        return new ChillField.FK<>(this.getClass(), this, columnName, type);
    }

    protected <S extends ChillRecord, T extends ChillRecord> ChillField.FK<S, T> fk(String columnName, Class<T> type, String otherColumn) {
        return new ChillField.FK<S, T>(this.getClass(), this, columnName, type).withColumn(otherColumn);
    }

    protected <T extends ChillRecord> ChillField.Many<T> hasMany(Class<T> componentType) {
        return hasMany(componentType, defaultFkName());
    }

    protected <T extends ChillRecord> ChillField.Many<T> hasMany(Class<T> componentType, String otherColumn) {
        return new ChillField.Many<>(this, componentType, otherColumn);
    }

    private String defaultFkName() {
        return tableName + "_id";
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

    public void reload() {
        var query = new ChillQuery<>(this.getClass());
        query.reload(this);
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

        public ChillQuery<T> join(ChillField.FK foriegnKey) {
            return new ChillQuery<T>(clazz).join(foriegnKey);
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