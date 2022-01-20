/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
 ******************************************************************************/

package org.opennms.core.schema;

import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Perform administrative setup of the database, including creation of the opennms database.
 */
public class MigratorAdminInitialize {
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(MigratorAdminInitialize.class);

    private Logger log = DEFAULT_LOGGER;

    private static final Logger LOG = LoggerFactory.getLogger(MigratorAdminInitialize.class);
    private static final Pattern POSTGRESQL_VERSION_PATTERN = Pattern.compile("^(?:PostgreSQL|EnterpriseDB) (\\d+\\.\\d+)");
    private static final float POSTGRESQL_MIN_VERSION_INCLUSIVE = Float.parseFloat(System.getProperty("opennms.postgresql.minVersion", "10.0"));
    private static final float POSTGRESQL_MAX_VERSION_EXCLUSIVE = Float.parseFloat(System.getProperty("opennms.postgresql.maxVersion", "15.0"));

    private DataSource m_adminDataSource;
    private Float m_databaseVersion;
    private boolean m_validateDatabaseVersion = true;
    private boolean m_createUser = true;
    private boolean m_createDatabase = true;

    private String m_databaseName;
    private String m_schemaName;
    private String m_adminUser;
    private String m_adminPassword;
    private String m_databaseUser;
    private String m_databasePassword;

//========================================
// Constructors
//========================================

    public MigratorAdminInitialize() {
    }


//========================================
// Getters and Setters
//========================================

    /**
     * <p>getAdminDataSource</p>
     *
     * @return a {@link DataSource} object.
     */
    public DataSource getAdminDataSource() {
        return m_adminDataSource;
    }

    /**
     * <p>setAdminDataSource</p>
     *
     * @param dataSource a {@link DataSource} object.
     */
    public void setAdminDataSource(final DataSource dataSource) {
        m_adminDataSource = dataSource;
    }

    /**
     * <p>setValidateDatabaseVersion</p>
     *
     * @param validate a boolean.
     */
    public void setValidateDatabaseVersion(final boolean validate) {
        m_validateDatabaseVersion = validate;
    }

    /**
     * <p>setCreateUser</p>
     *
     * @param createUser a boolean.
     */
    public void setCreateUser(final boolean createUser) {
        m_createUser = createUser;
    }

    /**
     * <p>setCreateDatabase</p>
     *
     * @param createDatabase a boolean.
     */
    public void setCreateDatabase(final boolean createDatabase) {
        m_createDatabase = createDatabase;
    }

    /**
     * <p>getDatabaseName</p>
     *
     * @return a {@link String} object.
     */
    public String getDatabaseName() {
        return m_databaseName;
    }
    /**
     * <p>setDatabaseName</p>
     *
     * @param databaseName a {@link String} object.
     */
    public void setDatabaseName(String databaseName) {
        m_databaseName = databaseName;
    }

    public String getSchemaName() {
    	return m_schemaName;
    }

    public void setSchemaName(final String schemaName) {
    	m_schemaName = schemaName;
    }

    /**
     * <p>getAdminUser</p>
     *
     * @return a {@link String} object.
     */
    public String getAdminUser() {
        return m_adminUser;
    }
    /**
     * <p>setAdminUser</p>
     *
     * @param adminUser a {@link String} object.
     */
    public void setAdminUser(String adminUser) {
        m_adminUser = adminUser;
    }

    /**
     * <p>getAdminPassword</p>
     *
     * @return a {@link String} object.
     */
    public String getAdminPassword() {
        return m_adminPassword;
    }
    /**
     * <p>setAdminPassword</p>
     *
     * @param adminPassword a {@link String} object.
     */
    public void setAdminPassword(String adminPassword) {
        m_adminPassword = adminPassword;
    }

    /**
     * <p>getDatabaseUser</p>
     *
     * @return a {@link String} object.
     */
    public String getDatabaseUser() {
        return m_databaseUser;
    }
    /**
     * <p>setDatabaseUser</p>
     *
     * @param databaseUser a {@link String} object.
     */
    public void setDatabaseUser(String databaseUser) {
        m_databaseUser = databaseUser;
    }

    /**
     * <p>getDatabasePassword</p>
     *
     * @return a {@link String} object.
     */
    public String getDatabasePassword() {
        return m_databasePassword;
    }
    /**
     * <p>setDatabasePassword</p>
     *
     * @param databasePassword a {@link String} object.
     */
    public void setDatabasePassword(String databasePassword) {
        m_databasePassword = databasePassword;
    }


//========================================
// Interface
//========================================

    public void initializeDatabase(boolean updateDatabase, boolean timescaleDB) throws MigrationException, Exception, IOException {
        validateDatabaseVersion();

        // Creating extension in template1 before creating opennms DB.
        if(timescaleDB) {
            addTimescaleDBExtension();
        }
        if (updateDatabase) {
            this.databaseSetOwner();
            prepareDatabase();
        }

        checkUnicode();
        checkTime();
    }

    /**
     * <p>dropDatabase</p>
     *
     * @throws SQLException if any.
     */
    public void dropDatabase() throws MigrationException {
        LOG.info("removing database '" + getDatabaseName() + "'");

        Connection c = null;
        Statement st = null;

        try {
            c = m_adminDataSource.getConnection();
            st = c.createStatement();
            st.execute("DROP DATABASE \"" + getDatabaseName() + "\"");
        } catch (SQLException e) {
            throw new MigrationException("could not drop database " + getDatabaseName(), e);
        } finally {
            cleanUpDatabase(c, null, st, null);
        }
    }

//========================================
// Internals
//========================================

    /**
     * <p>getDatabaseVersion</p>
     *
     * @return a {@link Float} object.
     * @throws MigrationException if any.
     */
    private Float getDatabaseVersion() throws MigrationException {
        if (m_databaseVersion == null) {
            String versionString = null;
            Statement st = null;
            ResultSet rs = null;
            Connection c = null;
            try {
                c = m_adminDataSource.getConnection();
                st = c.createStatement();
                rs = st.executeQuery("SELECT version()");
                if (!rs.next()) {
                    throw new MigrationException("Database didn't return any rows for 'SELECT version()'");
                }

                versionString = rs.getString(1);

                rs.close();
                st.close();
            } catch (final SQLException e) {
                throw new MigrationException("an error occurred getting the version from the database", e);
            } finally {
                cleanUpDatabase(c, null, st, rs);
            }
            final Matcher m = POSTGRESQL_VERSION_PATTERN.matcher(versionString);

            if (!m.find()) {
                throw new MigrationException("Could not parse version number out of version string: " + versionString);
            }

            m_databaseVersion = Float.parseFloat(m.group(1));
        }

        return m_databaseVersion;
    }

    /**
     * <p>validateDatabaseVersion</p>
     *
     * @throws MigrationException if any.
     */
    private void validateDatabaseVersion() throws MigrationException {
        if (!m_validateDatabaseVersion) {
            LOG.info("skipping database version validation");
            return;
        }
        LOG.info("validating database version");

        final Float dbv = getDatabaseVersion();
        if (dbv == null) {
            throw new MigrationException("unable to determine database version");
        }

        final String message = String.format(
                "Unsupported database version \"%f\" -- you need at least %f and less than %f.  "
                        + "Use the \"-Q\" option to disable this check if you feel brave and are willing "
                        + "to find and fix bugs found yourself.",
                        dbv, POSTGRESQL_MIN_VERSION_INCLUSIVE, POSTGRESQL_MAX_VERSION_EXCLUSIVE
                );

        if (dbv < POSTGRESQL_MIN_VERSION_INCLUSIVE || dbv >= POSTGRESQL_MAX_VERSION_EXCLUSIVE) {
            throw new MigrationException(message);
        }
    }

    /**
     * <p>databaseUserExists</p>
     *
     * @return a boolean.
     * @throws MigrationException if any.
     */
    private boolean databaseUserExists() throws MigrationException {
        Statement st = null;
        ResultSet rs = null;
        Connection c = null;
        try {
            c = m_adminDataSource.getConnection();
            st = c.createStatement();
            rs = st.executeQuery("SELECT usename FROM pg_user WHERE usename = '" + getUserForONMSDB() + "'");
            if (rs.next()) {
                final String datname = rs.getString("usename");
                if (datname != null && datname.equalsIgnoreCase(getUserForONMSDB())) {
                    return true;
                } else {
                    return false;
                }
            }
            return rs.next();
        } catch (final SQLException e) {
            throw new MigrationException("an error occurred determining whether the OpenNMS user exists", e);
        } finally {
            cleanUpDatabase(c, null, st, rs);
        }
    }

    /**
     * <p>createUser</p>
     *
     * @throws MigrationException if any.
     */
    private void createUser() throws MigrationException {
        if (!m_createUser || databaseUserExists()) {
            return;
        }

        LOG.info("creating OpenNMS user, if necessary");
        Statement st = null;
        ResultSet rs = null;
        Connection c = null;
        try {
            c = m_adminDataSource.getConnection();
            st = c.createStatement();
            st.execute("CREATE USER " + getUserForONMSDB() + " WITH PASSWORD '" + getDatabasePassword() + "'");
        } catch (final SQLException e) {
            throw new MigrationException("an error occurred creating the OpenNMS user", e);
        } finally {
            cleanUpDatabase(c, null, st, rs);
        }
    }

    private String getUserForONMSDB() {
        String user = getDatabaseUser();
        user = user.indexOf("@")>0? user.substring(0, user.indexOf("@")):user;
        return user;
    }

    /**
     * <p>databaseExists</p>
     *
     * @return a boolean.
     * @throws MigrationException if any.
     */
    private boolean databaseExists() throws MigrationException {
        return databaseExists(getDatabaseName());
    }

    private boolean databaseExists(String databaseName) throws MigrationException {
        Statement st = null;
        ResultSet rs = null;
        Connection c = null;
        try {
            c = m_adminDataSource.getConnection();
            st = c.createStatement();
            rs = st.executeQuery("SELECT datname from pg_database WHERE datname = '" + databaseName + "'");
            if (rs.next()) {
                final String datname = rs.getString("datname");
                if (datname != null && datname.equalsIgnoreCase(databaseName)) {
                    return true;
                } else {
                    return false;
                }
            }
            return rs.next();
        } catch (final SQLException e) {
            throw new MigrationException("an error occurred determining whether the OpenNMS database exists", e);
        } finally {
            cleanUpDatabase(c, null, st, rs);
        }
    }

    private void createSchema() throws MigrationException {
        if (!m_createDatabase || schemaExists()) {
            return;
        }
    }

    private boolean schemaExists() throws MigrationException {
        /* FIXME: not sure how to ask postgresql for a schema
        Statement st = null;
        ResultSet rs = null;
        Connection c = null;
        try {
            c = m_adminDataSource.getConnection();
            st = c.createStatement();
            rs = st.executeQuery("SELECT datname from pg_database WHERE datname = '" + migration.getDatabaseName() + "'");
            if (rs.next()) {
                final String datname = rs.getString("datname");
                if (datname != null && datname.equalsIgnoreCase(migration.getDatabaseName())) {
                    return true;
                } else {
                    return false;
                }
            }
            return rs.next();
        } catch (final SQLException e) {
            throw new MigrationException("an error occurred determining whether the OpenNMS user exists", e);
        } finally {
            cleanUpDatabase(c, st, rs);
        }
         */
        return true;
    }

    /**
     * <p>createDatabase</p>
     *
     * @throws MigrationException if any.
     */
    private void createDatabase() throws MigrationException {
        if (!m_createDatabase || databaseExists()) {
            return;
        }
        LOG.info("creating OpenNMS database, if necessary");
        if (!databaseUserExists()) {
            throw new MigrationException(String.format("database will not be created: unable to grant access (user %s does not exist)", getDatabaseUser()));
        }

        Statement st = null;
        ResultSet rs = null;
        Connection c = null;
        try {
            c = m_adminDataSource.getConnection();
            st = c.createStatement();
            st.execute("CREATE DATABASE \"" + getDatabaseName() + "\" WITH ENCODING='UNICODE'");
            st.execute("GRANT ALL ON DATABASE \"" + getDatabaseName() + "\" TO \"" + getUserForONMSDB() + "\"");
        } catch (final SQLException e) {
            throw new MigrationException("an error occurred creating the OpenNMS database: " + e, e);
        } finally {
            cleanUpDatabase(c, null, st, rs);
        }
    }

    /**
     * <p>checkUnicode</p>
     *
     * @throws Exception if any.
     */
    private void checkUnicode() throws Exception {
        LOG.info("checking if database \"" + getDatabaseName() + "\" is unicode");

        Statement st = null;
        ResultSet rs = null;
        Connection c = null;

        try {
            c = m_adminDataSource.getConnection();
            st = c.createStatement();
            rs = st.executeQuery("SELECT encoding FROM pg_database WHERE LOWER(datname)='" + getDatabaseName().toLowerCase() + "'");
            if (rs.next()) {
                if (rs.getInt(1) == 5 || rs.getInt(1) == 6) {
                    return;
                }
            }

            throw new MigrationException("OpenNMS requires a Unicode database.  Please delete and recreate your\ndatabase and try again.");
        } finally {
            cleanUpDatabase(c, null, st, rs);
        }
    }

    /**
     * <p>databaseSetUser</p>
     *
     * @throws SQLException if any.
     */
    private void databaseSetOwner() throws MigrationException {
        PreparedStatement st = null;
        ResultSet rs = null;
        Connection c = null;

        try {
            c = m_adminDataSource.getConnection();

            final String[] tableTypes = {"TABLE"};
            rs = c.getMetaData().getTables(null, "public", "%", tableTypes);

            final HashSet<String> objects = new HashSet<String>();
            while (rs.next()) {
                objects.add(rs.getString("TABLE_NAME"));
            }

            //
            // NOTE: have seen problems using parameters here; using string manipulation instead; note that sql-injection
            //       should not be an issue because the table names are received from the DB, and the user-name is
            //       configured.  Still, using parameter injection would be preferable.
            //
//            st = c.prepareStatement("ALTER TABLE ? OWNER TO ?");
//            for (final String objName : objects) {
//                st.setString(1, objName);
//                st.setString(2, getDatabaseUser());
//                st.execute();
//            }

            for (final String objName : objects) {
                st = c.prepareStatement("ALTER TABLE \"" + objName + "\" OWNER TO \"" + getDatabaseUser() + "\"");
                st.execute();
            }

        } catch (SQLException e) {
            throw new MigrationException("an error occurred setting table ownership " + st, e);
        } finally {
            cleanUpDatabase(c, null, st, rs);
        }
    }

    private void addTimescaleDBExtension() throws MigrationException {
        LOG.info("adding timescaledb extension in template db");

        Connection c = null;
        Statement st = null;

        try {
            c = m_adminDataSource.getConnection();
            st = c.createStatement();
            st.execute("CREATE EXTENSION IF NOT EXISTS timescaledb;");
        } catch (SQLException e) {
            throw new MigrationException("could not add timescaledb extension", e);
        } finally {
            cleanUpDatabase(c, null, st, null);
        }

    }

    /**
     * <p>prepareDatabase</p>
     *
     * @throws MigrationException if any.
     */
    private void prepareDatabase() throws MigrationException {
        validateDatabaseVersion();
        createUser();
        createSchema();
        createDatabase();
    }

    private void cleanUpDatabase(final Connection c, DatabaseConnection dbc, final Statement st, final ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (final SQLException e) {
                LOG.warn("Failed to close result set.", e);
            }
        }
        if (st != null) {
            try {
                st.close();
            } catch (final SQLException e) {
                LOG.warn("Failed to close statement.", e);
            }
        }
        if (dbc != null) {
            try {
                dbc.close();
            } catch (final DatabaseException e) {
                LOG.warn("Failed to close database connection.", e);
            }
        }
        if (c != null) {
            try {
                c.close();
            } catch (final SQLException e) {
                LOG.warn("Failed to close connection.", e);
            }
        }
    }

    // Ensures that the database time and the system time running the installer match
    // If the difference is greater than 1s, it fails
    private void checkTime() throws Exception {
        LOG.info("checking if time of database \"" + getDatabaseName() + "\" is matching system time");

        try (Statement st = m_adminDataSource.getConnection().createStatement()) {
            final long beforeQueryTime = System.currentTimeMillis();
            try (ResultSet rs = st.executeQuery("SELECT NOW()")) {
                if (rs.next()) {
                    final Timestamp currentDatabaseTime = rs.getTimestamp(1);
                    final long currentSystemTime = System.currentTimeMillis();
                    final long diff = currentDatabaseTime.getTime() - currentSystemTime;
                    final long queryExecuteDelta = Math.abs(currentSystemTime - beforeQueryTime);
                    if (Math.abs(diff) > 1000 + queryExecuteDelta) {
                        LOG.info("NOT OK");
                        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
                        final String databaseDateString = simpleDateFormat.format(new Date(currentDatabaseTime.getTime()));
                        final String systemTimeDateString = simpleDateFormat.format(new Date(currentSystemTime));
                        throw new Exception("Database time and system time differ."
                                + "System time: " + systemTimeDateString + ", database time: " + databaseDateString
                                + ", diff: " + Math.abs(diff) + "ms. The maximum allowed difference is 1000ms."
                                + " Please update either the database time or system time");
                    }
                    LOG.info("OK");
                }
            }
        }
    }
}
