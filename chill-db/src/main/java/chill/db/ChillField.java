package chill.db;

import chill.utils.NiceList;
import chill.utils.TheMissingUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class ChillField<T> {

    private final ChillRecord record;
    private final String columnName;
    private final Class<T> type;
    private final boolean synthetic;
    private boolean password;
    private boolean required;
    protected boolean readOnly;
    private boolean primaryKey;
    private boolean generated;
    private boolean optimisticLock;

    private T value;
    private T lastSavedValue;
    private boolean dirty = false;
    private Callable lazyValue;

    // events
    protected final List<Transformer<T>> beforeSets;
    protected final List<Transformer<T>> beforeReturns;
    protected final List<ChillValidation<T>> validations;
    protected final NiceList<Consumer<ChillField<T>>> beforeCreates;
    protected final NiceList<Consumer<ChillField<T>>> afterCreates;
    protected final NiceList<Consumer<ChillField<T>>> beforeUpdates;
    protected final NiceList<Consumer<ChillField<T>>> afterUpdates;
    protected final NiceList<Consumer<ChillField<T>>> beforeSaves;
    protected final NiceList<Consumer<ChillField<T>>> afterSaves;
    private boolean uuid;

    public ChillField(ChillRecord record, String columnName, Class<T> type) {
        this.record = record;
        this.columnName = columnName;
        this.synthetic = columnName == null;
        this.type = type;
        record.fields.add(this);
        this.lastSavedValue = null;
        this.value = null;
        beforeSets = new LinkedList<>();
        beforeReturns = new LinkedList<>();
        beforeCreates = new NiceList<>();
        afterCreates = new NiceList<>();
        beforeUpdates = new NiceList<>();
        afterUpdates = new NiceList<>();
        beforeSaves = new NiceList<>();
        afterSaves = new NiceList<>();
        this.validations = new LinkedList<>();
        if (type.isEnum()) {
            beforeReturn(val -> enumValueFor(String.valueOf(val)));
            beforeSet(val -> (T) ((Enum) val).name());
        }
    }

    private T enumValueFor(String code) {
        for (T enumConstant : type.getEnumConstants()) {
            Enum e = (Enum) enumConstant;
            if (e.name().equals(code)) {
                return (T) e;
            }
        }
        return null;
    }

    public Object rawLastSavedValue() {
        return lastSavedValue();
    }

    public T lastSavedValue() {
        return lastSavedValue;
    }

    public ChillRecord getRecord() {
        return record;
    }

    public T get() {
        T valueToReturn = rawValue();
        for (var beforeReturn : beforeReturns) {
            valueToReturn = beforeReturn.transform(valueToReturn);
        }
        return valueToReturn;
    }

    public T rawValue() {
        if (!dirty && lazyValue != null && this.value == null) {
            this.value = (T) TheMissingUtils.safely(lazyValue::call);
        }
        return this.value;
    }

    protected void setRaw(Object value) {
        this.dirty = true;
        this.value = (T) value;
    }

    public void set(T value) {
        for (var beforeSet : beforeSets) {
            value = beforeSet.transform(value);
        }
        setRaw(value);
    }

    public boolean isDirty() {
        return dirty;
    }

    public String getColumnName() {
        return this.columnName;
    }

    public boolean hasValue() {
        return get() != null;
    }

    public boolean isSynthetic() {
        return synthetic;
    }

    public boolean isInDatabase() {
        return !isSynthetic();
    }

    public boolean isGenerated() {
        return generated;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public boolean isOptimisticLock() {
        return optimisticLock;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public Class<T> getType() {
        return type;
    }

    public void fromResultSet(ResultSet resultSet) {
        TheMissingUtils.safely(() -> {
            if (ChillRecord.class.isAssignableFrom(type)) {
                long valueFromDB = resultSet.getLong(getColumnName());
                lastSavedValue = (T) (Object) valueFromDB;
                value = (T) (Object) valueFromDB;
            } else if (type.isEnum()) {
                String valueFromDB = resultSet.getString(getColumnName());
                lastSavedValue = (T) (Object) valueFromDB;
                value = (T) (Object) valueFromDB;
            } else {
                T valueFromDB = resultSet.getObject(getColumnName(), type);
                lastSavedValue = valueFromDB;
                value = valueFromDB;
            }
        });
    }

    public void fromHashMap(Map<String, Object> map) {
        if (map.containsKey(getColumnName())) {
            set((T) map.get(getColumnName()));
        }
    }

    public ChillField<T> lazy(Callable c) {
        lazyValue = c;
        return this;
    }

    public ChillField<T> beforeSet(Transformer<T> transformer) {
        beforeSets.add(transformer);
        return this;
    }

    public ChillField<T> beforeReturn(Transformer<T> transformer) {
        beforeReturns.add(transformer);
        return this;
    }

    public ChillField<T> autoGenerated() {
        this.readOnly = true;
        this.generated = true;
        return this;
    }

    public ChillField<T> primaryKey() {
        this.primaryKey = true;
        return this;
    }

    public ChillField<T> optimisticLock() {
        this.optimisticLock = true;
        return this;
    }

    public ChillField<T> validateWith(ChillValidation.Validator<T> o, String message) {
        validations.add(new ChillValidation<>(o, message));
        return this;
    }

    public void validate(ChillValidation.Errors errors) {
        for (ChillValidation<T> validation : validations) {
            validation.validate(errors, this);
        }
    }

    @Override
    public String toString() {
        Object rawValue = rawValue();
        if (rawValue instanceof String) {
            rawValue = "\"" + rawValue + "\"";
        }
        return columnName + ":" + rawValue;
    }

    public ChillField<T> required() {
        return required("Field %1$s cannot be null");
    }
    public ChillField<T> required(String message) {
        required = true;
        validateWith(Objects::nonNull, message);
        return this;
    }

    public ChillField<String> password() {
        this.password = true;
        beforeSet(value -> {
            String str = String.valueOf(value);
            // TODO make pluggable
            return (T) TheMissingUtils.toArgon2EncodedString(str);
        });
        return (ChillField<String>) this;
    }

    public boolean isPassword() {
        return password;
    }

    public boolean passwordMatches(String passwd) {
        String passwordHash = String.valueOf(rawValue());
        return TheMissingUtils.matches(passwd, passwordHash);
    }

    public ChillField<String> email() {
        ChillField<String> thisAsStrField = (ChillField<String>) this;
        thisAsStrField.validateWith(ChillValidation::validEmail, "Invalid Email: %2$s");
        return thisAsStrField;
    }

    public ChillField<String> uuid() {
        readOnly = true;
        uuid = true;
        return (ChillField<String>) lazy(() -> UUID.randomUUID().toString());
    }

    public String getTypeName() {
        return  getType().getTypeName().replace("$", ".");
    }

    public ChillField<T> beforeCreate(Consumer<ChillField<T>> callback){
        beforeCreates.add(callback);
        return this;
    }

    void beforeCreate() {
        beforeCreates.forEach(fieldConsumer -> fieldConsumer.accept(this));
    }

    void afterCreate() {
        afterCreates.forEach(fieldConsumer -> fieldConsumer.accept(this));
    }

    public ChillField<T> beforeUpdate(Consumer<ChillField<T>> callback){
        beforeUpdates.add(callback);
        return this;
    }

    void beforeUpdate() {
        beforeUpdates.forEach(fieldConsumer -> fieldConsumer.accept(this));
    }

    void afterUpdate() {
        afterUpdates.forEach(fieldConsumer -> fieldConsumer.accept(this));
    }

    void beforeSave() {
        beforeSaves.forEach(fieldConsumer -> fieldConsumer.accept(this));
    }

    void afterSave() {
        afterSaves.forEach(fieldConsumer -> fieldConsumer.accept(this));
    }

    public ChillField<T> readOnly() {
        readOnly = true;
        return this;
    }

    public void updateLastSaved() {
        lastSavedValue = value;
    }

    public void setFromString(String stringValue) {
        if (this.type.equals(String.class)) {
            set((T) stringValue);
        } else if (this.type.equals(Integer.class)) {
            set((T) Integer.valueOf(stringValue));
        } else if (this.type.equals(Long.class)) {
            set((T) Long.valueOf(stringValue));
        } else if (this.type.equals(Boolean.class)) {
            set((T) (Object) "true".equalsIgnoreCase(stringValue));
        } else if (this.type.isEnum()) {
            set(enumValueFor(stringValue));
        } else {
            throw new IllegalArgumentException("I don't know how to convert a string into a " + this.type.getName());
        }
    }

    public ChillField<T> withDefault(T defaultValue) {
        set(defaultValue);
        return this;
    }

    public boolean isBoolean() {
        return getType().equals(Boolean.class) || getType().equals(Boolean.TYPE);
    }

    public boolean isUUID() {
        return uuid;
    }

    public static class FK<S extends ChillRecord, T> extends ChillField<T> {

        private final Class self;
        private String foreignColumn;
        private ChillRecord cachedRecord;

        public FK(Class self, ChillRecord record, String columnName, Class<T> foreignType) {
            super(record, columnName, foreignType);
            this.self = self;
            this.foreignColumn = "id";
        }

        public FK<S, T> withColumn(String foreignColumn) {
            this.foreignColumn = foreignColumn;
            return this;
        }

        public Long fkValue() {
            return (Long) rawValue();
        }

        @Override
        public T get() {
            T valueToReturn;
            if (cachedRecord != null) {
                return (T) cachedRecord;
            } else {
                var aLong = fkValue();
                var query = new ChillQuery(getType()).where(foreignColumn, aLong);
                valueToReturn = (T) query.first();
            }
            for (var beforeReturn : beforeReturns) {
                valueToReturn = beforeReturn.transform(valueToReturn);
            }
            cachedRecord = (ChillRecord) valueToReturn;
            return valueToReturn;
        }

        @Override
        public void set(T value) {
            for (var beforeSet : beforeSets) {
                value = beforeSet.transform(value);
            }
            Object fkValue = getFKValue(value);
            cachedRecord = null;
            if (fkValue == null) {
                throw new IllegalStateException("Could not find field " + foreignColumn + " on " + cachedRecord);
            } else {
                setRaw(fkValue);
            }
        }

        private Object getFKValue(Object value) {
            if (value instanceof ChillRecord) {
                ChillRecord  otherRecord = (ChillRecord) value;
                Object fkValue = null;
                for (ChillField field : otherRecord.getFields()) {
                    if (field.getColumnName().equals(foreignColumn)) {
                        fkValue = field.get();
                        break;
                    }
                }
                return fkValue;
            } else {
                return value;
            }
        }

        public ChillQuery<S> reverse(ChillRecord forRecord){
            ChillQuery<S> query = new ChillQuery(self);
            query.where(this.getColumnName(), getFKValue(forRecord));
            return query;
        }

        public String getForeignColumn() {
            return foreignColumn;
        }
    }

    //===================================================
    // Functional Interfaces
    //===================================================
    public interface Transformer<T> {
        T transform(T value);
    }

    public static class Many<T extends ChillRecord> {
        private final String foreignKeyColumn;
        private final Class componentType;
        private final ChillField joinValueField;
        private Many through;

        public <T extends ChillRecord> Many(ChillRecord record, Class<T> type, String foreignKeyColumn) {
            this.componentType = type;
            this.foreignKeyColumn = foreignKeyColumn;
            record.manys.add(this);
            // TODO parameterize 'id'
            this.joinValueField = record.getFieldsAsMap().get("id");
        }

        public ChillQuery<T> get() {
            var query = new ChillQuery(componentType);
            if (through != null) {
                FK joinFk = findJoinFKFor(componentType, through.componentType);
                query = query.join(joinFk);
            }
            return query.where(foreignKeyColumn, joinValueField.get());
        }

        private FK findJoinFKFor(Class type, Class componentType) {
            ChillRecord prototype = ChillRecord.getPrototype(type);
            FK fk = prototype.getFields().ofType(FK.class).first(field -> field.getType().equals(componentType));
            return fk;
        }

        public String getTypeName() {
            return "chill.db.ChillQuery<" + componentType.getName() + ">";
        }

        public Many<T> through(Many through) {
            this.through = through;
            return this;
        }
    }
}
