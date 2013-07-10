package org.opennms.core.test.db;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

public interface TemporaryDatabase extends DataSource {
    public static final String TEST_DB_NAME_PREFIX = "opennms_test_";
    public static final String URL_PROPERTY = "mock.db.url";
    public static final String ADMIN_USER_PROPERTY = "mock.db.adminUser";
    public static final String ADMIN_PASSWORD_PROPERTY = "mock.db.adminPassword";
    public static final String DEFAULT_DRIVER = "org.postgresql.Driver";
    public static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/";
    public static final String DEFAULT_ADMIN_USER = "postgres";
    public static final String DEFAULT_ADMIN_PASSWORD = "";

    public String getTestDatabase();
    public void setPopulateSchema(boolean populate);
    public void create() throws TemporaryDatabaseException;
    public void drop() throws TemporaryDatabaseException;
    public int countRows(final String sql, Object... values);
    public SimpleJdbcTemplate getJdbcTemplate();
}
