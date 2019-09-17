/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.distributed.kvstore.pgshared;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.opennms.features.distributed.kvstore.api.AbstractAsyncKeyValueStore;

/**
 * A {@link org.opennms.features.distributed.kvstore.api.KeyValueStore} backed by Postgres.
 * <p>
 * Postgres key value stores should implement this class with concrete types.
 *
 * @param <T> the type this store persists
 * @param <S> the SQL type this store persists if the value type must be wrapped with an SQL type
 */
public abstract class AbstractPostgresKeyValueStore<T, S> extends AbstractAsyncKeyValueStore<T> {
    private static final String VALUE_COLUMN = "value";
    private static final String KEY_COLUMN = "key";
    private static final String CONTEXT_COLUMN = "context";
    private static final String LAST_UPDATED_COLUMN = "last_updated";
    private static final String EXPIRES_AT_COLUMN = "expires_at";

    private final DataSource dataSource;

    public AbstractPostgresKeyValueStore(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    /**
     * Check the result set to see if it has already expired due to TTL but has not been cleaned up yet. In this case we
     * will want to treat the record as though it does not exist (it should be automatically cleaned up in the future).
     */
    private static boolean isExpired(ResultSet resultSet) throws SQLException {
        long now = System.currentTimeMillis();
        Timestamp expiresAt = resultSet.getTimestamp(EXPIRES_AT_COLUMN);

        return expiresAt != null && expiresAt.getTime() < now;
    }

    private PreparedStatement getSelectStatement(Connection connection) throws SQLException {
        return connection.prepareStatement(String.format("SELECT %s, %s FROM %s WHERE %s = ? AND %s = ?",
                VALUE_COLUMN, EXPIRES_AT_COLUMN, getTableName(), KEY_COLUMN, CONTEXT_COLUMN));
    }

    private PreparedStatement getUpsertStatement(Connection connection) throws SQLException {
        return connection.prepareStatement(String.format(
                "INSERT INTO %s (%s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, " + getValueStatementPlaceholder() + ") ON " +
                        "CONFLICT ON CONSTRAINT " + getPkConstraintName() + " DO UPDATE SET %s = ?, %s = ?, %s = " +
                        getValueStatementPlaceholder(), getTableName(), KEY_COLUMN, CONTEXT_COLUMN, LAST_UPDATED_COLUMN,
                EXPIRES_AT_COLUMN, VALUE_COLUMN, LAST_UPDATED_COLUMN, EXPIRES_AT_COLUMN, VALUE_COLUMN
        ));
    }

    private PreparedStatement getLastUpdatedStatement(Connection connection) throws SQLException {
        return connection.prepareStatement(String.format("SELECT %s, %s FROM %s WHERE %s = ? AND %s = ?",
                LAST_UPDATED_COLUMN, EXPIRES_AT_COLUMN, getTableName(), KEY_COLUMN, CONTEXT_COLUMN));
    }

    private PreparedStatement getEnumerateStatement(Connection connection) throws SQLException {
        return connection.prepareStatement(String.format("SELECT %s, %s, %s FROM %s WHERE %s = ?",
                KEY_COLUMN, VALUE_COLUMN, EXPIRES_AT_COLUMN, getTableName(), CONTEXT_COLUMN));
    }

    private PreparedStatement getDeleteStatement(Connection connection) throws SQLException {
        return connection.prepareStatement(String.format("DELETE FROM %s WHERE %s = ? AND %s = ?",
                getTableName(), KEY_COLUMN, CONTEXT_COLUMN));
    }

    private PreparedStatement getTruncateStatement(Connection connection) throws SQLException {
        return connection.prepareStatement(String.format("DELETE FROM %s WHERE %s = ?",
                getTableName(), CONTEXT_COLUMN));
    }

    @Override
    public long put(String key, T value, String context, Integer ttlInSeconds) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);

        long now = System.currentTimeMillis();

        withStatement(this::getUpsertStatement, upsertStatement -> {
            // The below sets the prepared values for both the INSERT and UPDATE cases hence some values being 
            // repeated
            upsertStatement.setString(1, key);
            upsertStatement.setString(2, context);
            upsertStatement.setTimestamp(3, new java.sql.Timestamp(now));
            upsertStatement.setTimestamp(6, new java.sql.Timestamp(now));

            if (ttlInSeconds != null) {
                long expireTime = now + TimeUnit.MILLISECONDS.convert(ttlInSeconds, TimeUnit.SECONDS);
                upsertStatement.setTimestamp(4, new java.sql.Timestamp(expireTime));
                upsertStatement.setTimestamp(7, new java.sql.Timestamp(expireTime));
            } else {
                upsertStatement.setNull(4, Types.DATE);
                upsertStatement.setNull(7, Types.DATE);
            }

            upsertStatement.setObject(5, getSQLTypeFromValueType(value));
            upsertStatement.setObject(8, getSQLTypeFromValueType(value));
            return upsertStatement.execute();
        });

        return now;
    }

    @Override
    public Optional<T> get(String key, String context) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(context);

        return withStatement(this::getSelectStatement, selectStatement -> {
            selectStatement.setString(1, key);
            selectStatement.setString(2, context);

            try (ResultSet resultSet = selectStatement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                // Return an empty result if we find an expired record
                if (isExpired(resultSet)) {
                    return Optional.empty();
                }

                return Optional.of(getValueTypeFromSQLType(resultSet, VALUE_COLUMN));
            }
        });
    }

    @Override
    public Optional<Optional<T>> getIfStale(String key, String context, long timestamp) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(context);

        // Note that the below is intentionally not acting atomically as the caller shouldn't be sensitive to the change
        // between the getLastUpdated and get

        OptionalLong lastUpdated = getLastUpdated(key, context);

        // There was no entry
        if (!lastUpdated.isPresent()) {
            return Optional.empty();
        }

        // Entry existed but caller's copy is not stale
        if (timestamp >= lastUpdated.getAsLong()) {
            return Optional.of(Optional.empty());
        }

        // Entry existed and caller's copy is stale
        Optional<T> value = get(key, context);

        // The value was removed between checking last updated and now
        if (!value.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(value);
    }

    @Override
    public OptionalLong getLastUpdated(String key, String context) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(context);

        return withStatement(this::getLastUpdatedStatement, lastUpdatedStatement -> {
            lastUpdatedStatement.setString(1, key);
            lastUpdatedStatement.setString(2, context);

            try (ResultSet resultSet = lastUpdatedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    return OptionalLong.empty();
                }

                // Return an empty result if we find an expired record
                if (isExpired(resultSet)) {
                    return OptionalLong.empty();
                }

                return OptionalLong.of(resultSet.getTimestamp(LAST_UPDATED_COLUMN).getTime());
            }
        });
    }

    @Override
    public Map<String, T> enumerateContext(String context) {
        Objects.requireNonNull(context);

        return withStatement(this::getEnumerateStatement, enumerateStatement -> {
            Map<String, T> resultMap = new HashMap<>();
            enumerateStatement.setString(1, context);

            try (ResultSet enumerateResult = enumerateStatement.executeQuery()) {
                while (enumerateResult.next()) {
                    // Ignore results that are already expired
                    if (!isExpired(enumerateResult)) {
                        resultMap.put(enumerateResult.getString(KEY_COLUMN),
                                getValueTypeFromSQLType(enumerateResult, VALUE_COLUMN));
                    }
                }
            }

            return resultMap;
        });
    }

    @Override
    public void delete(String key, String context) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(context);

        withStatement(this::getDeleteStatement, deleteStatement -> {
            deleteStatement.setString(1, key);
            deleteStatement.setString(2, context);

            return deleteStatement.execute();
        });
    }

    @Override
    public void truncateContext(String context) {
        Objects.requireNonNull(context);
        
        withStatement(this::getTruncateStatement, truncateStatement -> {
            truncateStatement.setString(1, context);
            
            return truncateStatement.execute();
        });
    }

    /**
     * Sub classes should override this method to provide handling for converting from type T to a JDBC type if type T
     * is not a native JDBC type.
     */
    @SuppressWarnings("unchecked")
    protected S getSQLTypeFromValueType(T value) {
        return (S) value;
    }

    /**
     * Sub classes must override this method to provide handling for converting from the JDBC result to type T.
     */
    protected abstract T getValueTypeFromSQLType(ResultSet resultSet, String columnName) throws SQLException;

    /**
     * Sub classes should override this to add additional specificity to the SQL placeholder in the prepared statements
     * if necessary.
     */
    protected String getValueStatementPlaceholder() {
        return "?";
    }

    /**
     * @return the name of the table for this store
     */
    protected abstract String getTableName();

    /**
     * @return the name of the primary key constraint for the table this store persists to
     */
    protected abstract String getPkConstraintName();

    @Override
    public String getName() {
        return "Postgres";
    }

    private <U> U withStatement(ConnectionToStatement connectionToStatement, StatementToResult<U> statementToResult) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connectionToStatement.getStatement(connection)) {
                return statementToResult.getResult(statement);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    private interface StatementToResult<T> {
        T getResult(PreparedStatement statement) throws SQLException;
    }

    @FunctionalInterface
    private interface ConnectionToStatement {
        PreparedStatement getStatement(Connection connection) throws SQLException;
    }
}
