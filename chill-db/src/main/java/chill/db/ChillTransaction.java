package chill.db;

import chill.utils.ChillLogs;

import java.sql.*;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executor;

import static chill.utils.TheMissingUtils.safely;

public class ChillTransaction implements AutoCloseable{

    public static ChillLogs.LogCategory LOG = ChillLogs.get(ChillTransaction.class);

    private static final ThreadLocal<LinkedList<ChillTransaction>> CURRENT_TRANSACTION = new ThreadLocal<>();
    private final Connection connection;
    private final Connection wrapper;
    private final UUID uuid;

    public static ChillTransaction start(Connection connection) {
        ChillTransaction newTransaction = new ChillTransaction(connection);
        var stack = CURRENT_TRANSACTION.get();
        if (stack == null) CURRENT_TRANSACTION.set(stack = new LinkedList<>());
        stack.push(newTransaction);
        return newTransaction;
    }

    public static ChillTransaction getCurrentTransaction() {
        var stack = CURRENT_TRANSACTION.get();
        if (stack == null) CURRENT_TRANSACTION.set(stack = new LinkedList<>());
        if (stack.isEmpty()) return null;
        return stack.peek();
    }

    public static Connection currentTransactionConnection() {
        if (CURRENT_TRANSACTION.get() == null) CURRENT_TRANSACTION.set(new LinkedList<>());
        ChillTransaction currentTransaction = CURRENT_TRANSACTION.get().peek();
        if (currentTransaction != null) {
            return currentTransaction.getTransactionConnection();
        } else {
            return null;
        }
    }

    private ChillTransaction(Connection connection) {
        safely(() -> connection.setAutoCommit(false));
        this.connection = connection;
        this.wrapper = new TransactionWrapperConnection();
        this.uuid = UUID.randomUUID();
        if (ChillRecord.shouldLog()) {
            LOG.info("Starting transaction " + uuid);
        }
    }

    @Override
    public void close() {
        try {
            safely(this.connection::close);
        } finally {
            CURRENT_TRANSACTION.remove();
        }
    }

    public Connection getTransactionConnection() {
        return wrapper;
    }

    public void commit() {
        if (ChillRecord.shouldLog()) {
            LOG.info("Committing transaction " + uuid);
        }
        safely(this.connection::commit);
    }

    public void rollback() {
        if (ChillRecord.shouldLog()) {
            LOG.info("Rolling back transaction " + uuid);
        }
        safely(() -> this.connection.rollback());
    }

    public void join() {
        if (ChillRecord.shouldLog()) {
            LOG.info("Joining transaction " + uuid);
        }
    }

    private class TransactionWrapperConnection implements Connection {

        public Statement createStatement() throws SQLException {
            return connection.createStatement();
        }

        public PreparedStatement prepareStatement(String sql) throws SQLException {
            return connection.prepareStatement(sql);
        }

        public CallableStatement prepareCall(String sql) throws SQLException {
            return connection.prepareCall(sql);
        }

        public String nativeSQL(String sql) throws SQLException {
            return connection.nativeSQL(sql);
        }

        public void setAutoCommit(boolean autoCommit) throws SQLException {
            // ignore autocommit
//            connection.setAutoCommit(autoCommit);
        }

        public boolean getAutoCommit() throws SQLException {
            return connection.getAutoCommit();
        }

        public void commit() throws SQLException {
            // transaction will commit
//            connection.commit();
        }

        public void rollback() throws SQLException {
            connection.rollback();
        }

        public void close() throws SQLException {
            // ignore closing
//            connection.close();
        }

        public boolean isClosed() throws SQLException {
            return connection.isClosed();
        }

        public DatabaseMetaData getMetaData() throws SQLException {
            return connection.getMetaData();
        }

        public void setReadOnly(boolean readOnly) throws SQLException {
            connection.setReadOnly(readOnly);
        }

        public boolean isReadOnly() throws SQLException {
            return connection.isReadOnly();
        }

        public void setCatalog(String catalog) throws SQLException {
            connection.setCatalog(catalog);
        }

        public String getCatalog() throws SQLException {
            return connection.getCatalog();
        }

        public void setTransactionIsolation(int level) throws SQLException {
            connection.setTransactionIsolation(level);
        }

        public int getTransactionIsolation() throws SQLException {
            return connection.getTransactionIsolation();
        }

        public SQLWarning getWarnings() throws SQLException {
            return connection.getWarnings();
        }

        public void clearWarnings() throws SQLException {
            connection.clearWarnings();
        }

        public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            return connection.createStatement(resultSetType, resultSetConcurrency);
        }

        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
        }

        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return connection.prepareCall(sql, resultSetType, resultSetConcurrency);
        }

        public Map<String, Class<?>> getTypeMap() throws SQLException {
            return connection.getTypeMap();
        }

        public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
            connection.setTypeMap(map);
        }

        public void setHoldability(int holdability) throws SQLException {
            connection.setHoldability(holdability);
        }

        public int getHoldability() throws SQLException {
            return connection.getHoldability();
        }

        public Savepoint setSavepoint() throws SQLException {
            return connection.setSavepoint();
        }

        public Savepoint setSavepoint(String name) throws SQLException {
            return connection.setSavepoint(name);
        }

        public void rollback(Savepoint savepoint) throws SQLException {
            connection.rollback(savepoint);
        }

        public void releaseSavepoint(Savepoint savepoint) throws SQLException {
            connection.releaseSavepoint(savepoint);
        }

        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
            return connection.prepareStatement(sql, autoGeneratedKeys);
        }

        public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
            return connection.prepareStatement(sql, columnIndexes);
        }

        public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
            return connection.prepareStatement(sql, columnNames);
        }

        public Clob createClob() throws SQLException {
            return connection.createClob();
        }

        public Blob createBlob() throws SQLException {
            return connection.createBlob();
        }

        public NClob createNClob() throws SQLException {
            return connection.createNClob();
        }

        public SQLXML createSQLXML() throws SQLException {
            return connection.createSQLXML();
        }

        public boolean isValid(int timeout) throws SQLException {
            return connection.isValid(timeout);
        }

        public void setClientInfo(String name, String value) throws SQLClientInfoException {
            connection.setClientInfo(name, value);
        }

        public void setClientInfo(Properties properties) throws SQLClientInfoException {
            connection.setClientInfo(properties);
        }

        public String getClientInfo(String name) throws SQLException {
            return connection.getClientInfo(name);
        }

        public Properties getClientInfo() throws SQLException {
            return connection.getClientInfo();
        }

        public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
            return connection.createArrayOf(typeName, elements);
        }

        public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
            return connection.createStruct(typeName, attributes);
        }

        public void setSchema(String schema) throws SQLException {
            connection.setSchema(schema);
        }

        public String getSchema() throws SQLException {
            return connection.getSchema();
        }

        public void abort(Executor executor) throws SQLException {
            connection.abort(executor);
        }

        public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
            connection.setNetworkTimeout(executor, milliseconds);
        }

        public int getNetworkTimeout() throws SQLException {
            return connection.getNetworkTimeout();
        }

        public void beginRequest() throws SQLException {
            connection.beginRequest();
        }

        public void endRequest() throws SQLException {
            connection.endRequest();
        }

        public boolean setShardingKeyIfValid(ShardingKey shardingKey, ShardingKey superShardingKey, int timeout) throws SQLException {
            return connection.setShardingKeyIfValid(shardingKey, superShardingKey, timeout);
        }

        public boolean setShardingKeyIfValid(ShardingKey shardingKey, int timeout) throws SQLException {
            return connection.setShardingKeyIfValid(shardingKey, timeout);
        }

        public void setShardingKey(ShardingKey shardingKey, ShardingKey superShardingKey) throws SQLException {
            connection.setShardingKey(shardingKey, superShardingKey);
        }

        public void setShardingKey(ShardingKey shardingKey) throws SQLException {
            connection.setShardingKey(shardingKey);
        }

        public <T> T unwrap(Class<T> iface) throws SQLException {
            return connection.unwrap(iface);
        }

        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return connection.isWrapperFor(iface);
        }
    }
}
