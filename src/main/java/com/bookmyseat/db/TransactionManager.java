package com.bookmyseat.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Wraps a single Connection for a unit-of-work transaction.
 *
 * Usage pattern:
 *   try (TransactionManager tx = new TransactionManager()) {
 *       tx.begin();
 *       someDAO.doInsert(tx.getConnection(), data);
 *       anotherDAO.doUpdate(tx.getConnection(), data);  // same Connection!
 *       tx.commit();
 *   } catch (Exception e) {
 *       tx.rollback();
 *       throw e;
 *   }
 *
 * KEY RULE: All DAO calls inside a transaction MUST use tx.getConnection()
 * — never open a new connection mid-transaction.
 */
public class TransactionManager implements AutoCloseable {

    private final Connection connection;

    public TransactionManager() throws SQLException {
        this.connection = DBConnection.getInstance().getConnection();
    }

    /**
     * Begins a serializable transaction.
     * Use SERIALIZABLE for the booking flow to prevent phantom reads.
     * Use READ_COMMITTED for read-only queries (override via overloaded begin).
     */
    public void begin() throws SQLException {
        connection.setAutoCommit(false);
        connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
    }

    /** Begin with explicit isolation level. */
    public void begin(int isolationLevel) throws SQLException {
        connection.setAutoCommit(false);
        connection.setTransactionIsolation(isolationLevel);
    }

    /** Returns the managed connection — pass this to every DAO call in this transaction. */
    public Connection getConnection() {
        return connection;
    }

    public void commit() throws SQLException {
        connection.commit();
    }

    public void rollback() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.rollback();
            }
        } catch (SQLException e) {
            System.err.println("[TX] Rollback failed: " + e.getMessage());
        }
    }

    /** Returns connection to the pool. Called automatically in try-with-resources. */
    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.setAutoCommit(true); // reset before returning to pool
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("[TX] Connection close failed: " + e.getMessage());
        }
    }
}
