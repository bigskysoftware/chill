package chill.db;

import chill.utils.ChillLogs;
import chill.utils.NiceList;
import chill.utils.Pair;
import chill.utils.TheMissingUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
public class ChillQuery<T extends ChillRecord> implements Iterable<T> {

    public enum Direction {
        ASCENDING,
        DESCENDING
    }

    private final Class<T> clazz;
    private final T protoInstance;
    private final StringBuilder whereClause = new StringBuilder();
    private final LinkedList queryArguments = new LinkedList();
    private final Map<String, Object> colValues = new LinkedHashMap<>();
    private final String tableName;
    private final ChillLogs.LogCategory log;
    private NiceList<Pair<String, Direction>> orders = new NiceList();

    private boolean instantiatable = true;


    public ChillQuery(Class<T> clazz) {
        this.clazz = clazz;
        this.log = ChillLogs.get(clazz);
        this.protoInstance = makeInstance();
        this.tableName = protoInstance.getTableName();
    }

    public ChillQuery(ChillRecord chillRecord) {
        this((Class<T>) chillRecord.getClass());
        NiceList<ChillField> fields = chillRecord.getFields();
        for (ChillField field : fields) {
            if (field.isDirty() && field.isInDatabase()) {
                where$(field.getColumnName(), field.rawValue());
            }
        }
    }

    private T makeInstance() {
        return TheMissingUtils.newInstance(clazz);
    }

    private ChillQuery(ChillQuery<T> from) {
        this.clazz = from.clazz;
        this.log = from.log;
        this.protoInstance = from.protoInstance;
        this.whereClause.append(from.whereClause);
        this.queryArguments.addAll(from.queryArguments);
        this.colValues.putAll(from.colValues);
        this.instantiatable = from.instantiatable;
        this.tableName = from.tableName;
    }

    public ChillQuery<T> where(Object... conditions) {
        return new ChillQuery<T>(this).where$(conditions);
    }

    public ChillQuery<T> reorder(String col) {
        ChillQuery<T> ts = new ChillQuery<>(this);
        ts.orders = new NiceList<>();
        return ts.orderBy$(col, Direction.ASCENDING);
    }
    public ChillQuery<T> reorderDescending(String col) {
        ChillQuery<T> ts = new ChillQuery<>(this);
        ts.orders = new NiceList<>();
        return ts.orderBy$(col, Direction.DESCENDING);
    }

    public ChillQuery<T> ascendingBy(String col) {
        return new ChillQuery<T>(this).orderBy$(col, Direction.ASCENDING);
    }

    public ChillQuery<T> descendingBy(String col) {
        return new ChillQuery<T>(this).orderBy$(col, Direction.DESCENDING);
    }

    private ChillQuery<T> orderBy$(String col, Direction direction) {
        orders.add(Pair.of(col, direction));
        return this;
    }

    public T firstWhere(Object... conditions) {
        return new ChillQuery<T>(this).where$(conditions).first();
    }

    private ChillQuery<T> where$(Object... conditions) {
        if (conditions.length > 0) {
            Map conditionParameters = new HashMap();
            String newWhereSQL = String.valueOf(conditions[0]);
            if (!whereClause.isEmpty()) {
                whereClause.append(" AND\n");
            }

            if (newWhereSQL.contains("$")) {
                // query contains a $, this is a named argument style query

                for (int i = 1; i < conditions.length; i = i + 2) {
                    Object name = conditions[i];
                    Object value = conditions[i + 1];
                    conditionParameters.put(name, value);
                }
                Pattern vars = Pattern.compile("\\$[a-zA-Z]*");
                Matcher matcher = vars.matcher(newWhereSQL);
                while (matcher.find()) {
                    String group = matcher.group(0);
                    queryArguments.add(conditionParameters.get(group.substring(1)));
                    newWhereSQL = matcher.replaceFirst("?");
                    matcher = vars.matcher(newWhereSQL);
                }
                whereClause.append(newWhereSQL + "\n");
                instantiatable = false;
            } else if (newWhereSQL.contains("?")  || conditions.length == 1) {
                // query is positional style
                for (int i = 1; i < conditions.length; i++) {
                    Object condition = conditions[i];
                    queryArguments.add(condition);
                }
                whereClause.append(newWhereSQL + "\n");
                instantiatable = false;
            } else {
                // name/value query
                for (int i = 0; i < conditions.length - 1; i= i + 2) {
                    String field = String.valueOf(conditions[i]);
                    Object value = conditions[i + 1];
                    queryArguments.add(value);
                    if (i > 0) {
                        whereClause.append(" AND ");
                    }
                    // TODO - support other operations, NULL values, IN, etc.
                    whereClause.append(field).append("=?\n");
                    colValues.put(field, value);
                }
            }
        }
        return this;
    }

    public T newRecord() {
        if (instantiatable) {
            T t = makeInstance();
            ChillRecord.populateFromHashMap(t, colValues);
            return t;
        } else {
            throw new IllegalStateException("This query cannot be used to instantiate new elements because it has non-column conditions on it");
        }
    }

    private String getTableName() {
        return this.tableName;
    }

    public String sql() {
        var fields = getFields();
        var sql = "SELECT " + fields.map(ChillField::getColumnName).join(", ") + "\n" +
                "FROM " + getTableName();
        if (whereClause.length() > 0) {
            sql = sql + "\nWHERE " + whereClause;
        }
        if (orders.size() > 0) {
            sql = sql + "\n ORDER BY ";
            for (int i = 0; i < orders.size(); i++) {
                Pair<String, Direction> order = orders.get(i);
                sql = sql + order.first + (order.second == Direction.ASCENDING ? " ASC " : " DESC ");
                if (i < orders.size() - 2) {
                    sql += ", ";
                }
            }
        }
        return sql;
    }

    public String countSQL() {
        String sql = "SELECT COUNT(1) FROM " + getTableName();
        if (whereClause.length() > 0) {
            sql = sql + "\nWHERE " + whereClause;
        }
        return sql;
    }

    public String firstSQL() {
        var fields = getFields();
        String sql = "SELECT " + fields.map(ChillField::getColumnName).join(", ") + "\n" +
                "FROM " + getTableName();
        if (whereClause.length() > 0) {
            sql = sql + "\nWHERE " + whereClause;
        }
        sql = sql + "\nLIMIT 1";
        return sql;
    }

    public String deleteSQL() {
        String sql = "DELETE  FROM " + getTableName();
        if (whereClause.length() > 0) {
            sql = sql + "\nWHERE " + whereClause;
        }
        return sql;
    }

    private NiceList<ChillField> getFields() {
        return protoInstance.getFields().filter(ChillField::isInDatabase);
    }

    public List args() {
        return queryArguments;
    }

    protected void reload(ChillRecord record) {
        NiceList<ChillField> primaryKeyFields = record.getFields().filter(ChillField::isPrimaryKey);
        if (primaryKeyFields.isEmpty()) {
            throw new UnsupportedOperationException(record + " does not have primary keys to reload via.");
        }
        NiceList<Object> primaryKeyValues = primaryKeyFields.map(ChillField::get);
        String whereClause = primaryKeyFields.map(field -> field.getColumnName() + "=?").join(" AND ");
        LinkedList<Object> queryList = new LinkedList<>();
        queryList.add(whereClause);
        queryList.addAll(primaryKeyValues);
        TheMissingUtils.safely(() -> {
            try (var resultSetAndConnection =  where(queryList.toArray()).executeQuery()) {
                if (resultSetAndConnection.resultSet.next()) {
                    ChillRecord.populateFromResultSet(record, resultSetAndConnection.resultSet);
                } else {
                    throw new IllegalStateException("Could not reload " + record);
                }
            }
        });
    }
    public T findByPrimaryKey(Object... keys) {
        String where = getFields().filter(ChillField::isPrimaryKey).map(field -> field.getColumnName() + "=?").join(" AND ");
        LinkedList<Object> queryList = new LinkedList<>();
        queryList.add(where);
        queryList.addAll(Arrays.asList(keys));
        return TheMissingUtils.safely(() -> {
            try (var resultSetAndConnection = where(queryList.toArray()).executeQuery()) {
                if (resultSetAndConnection.resultSet.next()) {
                    T t = makeInstance();
                    ChillRecord.populateFromResultSet(t, resultSetAndConnection.resultSet);
                    return t;
                } else {
                    return null;
                }
            }
        });
    }

    public T first() {
        String sql = firstSQL();
        if (ChillRecord.shouldLog()) {
            log.info(ChillRecord.makeQueryLog("QUERY", sql, args()));
        }
        return TheMissingUtils.safely(() -> {
            try (Connection connection = ChillRecord.connectionSource.getConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                int col = 1;
                for (Object value : args()) {
                    preparedStatement.setObject(col++, value);
                }
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    T t = makeInstance();
                    ChillRecord.populateFromResultSet(t, resultSet);
                    return t;
                } else {
                    return null;
                }
            }
        });
    }

    public int delete() {
        String sql = deleteSQL();
        if (ChillRecord.shouldLog()) {
            log.info(ChillRecord.makeQueryLog("QUERY", sql, args()));
        }
        return TheMissingUtils.safely(() -> {
            try (Connection connection = ChillRecord.connectionSource.getConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                int col = 1;
                for (Object value : args()) {
                    preparedStatement.setObject(col++, value);
                }
                return preparedStatement.executeUpdate();
            }
        });
    }

    public NiceList<T> toList() {
        NiceList<T> nice = new NiceList<>();
        for (T t : this) {
            nice.add(t);
        }
        return nice;
    }

    public class ResultSetAndConnection implements AutoCloseable {
        public ResultSet resultSet;
        public Connection connection;

        public ResultSetAndConnection(ResultSet resultSet, Connection connection) {
            this.resultSet = resultSet;
            this.connection = connection;
        }

        @Override
        public void close() throws Exception {
            resultSet.close();
            connection.close();
        }
    }

    private ResultSetAndConnection executeQuery() {
        String sql = sql();
        if (ChillRecord.shouldLog()) {
            log.info(ChillRecord.makeQueryLog("QUERY", sql, args()));
        }
        return TheMissingUtils.safely(() -> {
            Connection connection = ChillRecord.connectionSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            int col = 1;
            for (Object value : args()) {
                preparedStatement.setObject(col++, value);
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            return new ResultSetAndConnection(resultSet, connection);
        });
    }

    public int count() {
        String sql = countSQL();
        if (ChillRecord.shouldLog()) {
            log.info(ChillRecord.makeQueryLog("QUERY", sql, args()));
        }
        return TheMissingUtils.safely(() -> {
            try (Connection connection = ChillRecord.connectionSource.getConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                int col = 1;
                for (Object value : args()) {
                    preparedStatement.setObject(col++, value);
                }
                ResultSet resultSet = preparedStatement.executeQuery();
                boolean next = resultSet.next();
                return resultSet.getInt(1);
            }
        });
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private boolean resultSetClosed;
            ResultSetAndConnection resultsAndConnection = executeQuery();
            boolean currentResultConsumed = true;
            T currentResult = null;

            @Override
            public boolean hasNext() {
                return TheMissingUtils.safely(this::hasNextImpl);
            }

            private boolean hasNextImpl() throws Exception {
                if (resultSetClosed) {
                    return false;
                }
                if (currentResultConsumed) {
                    boolean resultsHaveNext = resultsAndConnection.resultSet.next();
                    if (resultsHaveNext) {
                        currentResultConsumed = false;
                    } else {
                        resultsAndConnection.connection.close();
                        resultSetClosed = true;
                        return false;
                    }
                }
                return true;
            }

            @Override
            public T next() {
                return TheMissingUtils.safely(this::nextImpl);
            }

            private T nextImpl() throws Exception {
                if (resultSetClosed) {
                    throw new NoSuchElementException();
                }
                if (currentResultConsumed) {
                    if (!resultsAndConnection.resultSet.next()) {
                        throw new NoSuchElementException();
                    }
                }
                T instance = makeInstance();
                ChillRecord.populateFromResultSet(instance, resultsAndConnection.resultSet);
                currentResultConsumed = true;
                return instance;
            }
        };
    }
}
