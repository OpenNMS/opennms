/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.schema;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.LogLevel;
import liquibase.resource.ResourceAccessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

/**
 * <p>Migrator class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Migrator {
	
	private static final Logger LOG = LoggerFactory.getLogger(Migrator.class);
	
    private static final Pattern POSTGRESQL_VERSION_PATTERN = Pattern.compile("^(?:PostgreSQL|EnterpriseDB) (\\d+\\.\\d+)");
    public static final float POSTGRES_MIN_VERSION = 7.4f;
    public static final float POSTGRES_MAX_VERSION_PLUS_ONE = 9.9f;

    private static final String IPLIKE_SQL_RESOURCE = "iplike.sql";

    private DataSource m_dataSource;
    private DataSource m_adminDataSource;
    private Float m_databaseVersion;
    private boolean m_validateDatabaseVersion = true;
    private boolean m_createUser = true;
    private boolean m_createDatabase = true;

    /**
     * <p>Constructor for Migrator.</p>
     */
    public Migrator() {
        initLogging();
    }

    private void initLogging() {
        LogFactory.getLogger().setLogLevel(LogLevel.INFO);
    }

    public void enableDebug() {
        LogFactory.getLogger().setLogLevel(LogLevel.DEBUG);
    }

    /**
     * <p>getDataSource</p>
     *
     * @return a {@link javax.sql.DataSource} object.
     */
    public DataSource getDataSource() {
        return m_dataSource;
    }

    /**
     * <p>setDataSource</p>
     *
     * @param dataSource a {@link javax.sql.DataSource} object.
     */
    public void setDataSource(final DataSource dataSource) {
        m_dataSource = dataSource;
    }

    /**
     * <p>getAdminDataSource</p>
     *
     * @return a {@link javax.sql.DataSource} object.
     */
    public DataSource getAdminDataSource() {
        return m_adminDataSource;
    }

    /**
     * <p>setAdminDataSource</p>
     *
     * @param dataSource a {@link javax.sql.DataSource} object.
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
     * @param create a boolean.
     */
    public void setCreateUser(final boolean create) {
        m_createUser = create;
    }

    /**
     * <p>setCreateDatabase</p>
     *
     * @param create a boolean.
     */
    public void setCreateDatabase(final boolean create) {
        m_createDatabase = create;
    }

    /**
     * <p>getDatabaseVersion</p>
     *
     * @return a {@link java.lang.Float} object.
     * @throws org.opennms.core.schema.MigrationException if any.
     */
    public Float getDatabaseVersion() throws MigrationException {
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
     * @throws org.opennms.core.schema.MigrationException if any.
     */
    public void validateDatabaseVersion() throws MigrationException {
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
                                                     dbv.floatValue(), POSTGRES_MIN_VERSION, POSTGRES_MAX_VERSION_PLUS_ONE
                );

        if (dbv < POSTGRES_MIN_VERSION || dbv >= POSTGRES_MAX_VERSION_PLUS_ONE) {
            throw new MigrationException(message);
        }
    }

    /**
     * Get the expected extension for this platform.
     * @return
     */
    private String getExtension(final boolean jni) {
        final String osName = System.getProperty("os.name").toLowerCase();
        if (osName.startsWith("windows")) {
            return "dll";
        } else if (osName.startsWith("mac")) {
            if (jni) {
                return "jnilib";
            } else {
                return "so";
            }
        }
        return "so";
    }

    /**
     * <p>createLangPlPgsql</p>
     *
     * @throws org.opennms.core.schema.MigrationException if any.
     */
    public void createLangPlPgsql() throws MigrationException {
    	LOG.info("adding PL/PgSQL support to the database, if necessary");
        Statement st = null;
        ResultSet rs = null;
        Connection c = null;
        try {
            c = m_dataSource.getConnection();
            st = c.createStatement();
            rs = st.executeQuery("SELECT oid FROM pg_proc WHERE " + "proname='plpgsql_call_handler' AND " + "proargtypes = ''");
            if (rs.next()) {
            	LOG.info("PL/PgSQL call handler exists");
            } else {
            	LOG.info("adding PL/PgSQL call handler");
                st.execute("CREATE FUNCTION plpgsql_call_handler () " + "RETURNS OPAQUE AS '$libdir/plpgsql." + getExtension(false) + "' LANGUAGE 'c'");
            }
            rs.close();

            rs = st.executeQuery("SELECT pg_language.oid "
                    + "FROM pg_language, pg_proc WHERE "
                    + "pg_proc.proname='plpgsql_call_handler' AND "
                    + "pg_proc.proargtypes = '' AND "
                    + "pg_proc.oid = pg_language.lanplcallfoid AND "
                    + "pg_language.lanname = 'plpgsql'");
            if (rs.next()) {
            	LOG.info("PL/PgSQL language exists");
            } else {
            	LOG.info("adding PL/PgSQL language");
                st.execute("CREATE TRUSTED PROCEDURAL LANGUAGE 'plpgsql' "
                        + "HANDLER plpgsql_call_handler LANCOMPILER 'PL/pgSQL'");
            }
        } catch (final SQLException e) {
            throw new MigrationException("an error occurred getting the version from the database", e);
        } finally {
            cleanUpDatabase(c, null, st, rs);
        }
    }

    /**
     * <p>databaseUserExists</p>
     *
     * @param migration a {@link org.opennms.core.schema.Migration} object.
     * @return a boolean.
     * @throws org.opennms.core.schema.MigrationException if any.
     */
    public boolean databaseUserExists(final Migration migration) throws MigrationException {
        Statement st = null;
        ResultSet rs = null;
        Connection c = null;
        try {
            c = m_adminDataSource.getConnection();
            st = c.createStatement();
            rs = st.executeQuery("SELECT usename FROM pg_user WHERE usename = '" + migration.getDatabaseUser() + "'");
            if (rs.next()) {
                final String datname = rs.getString("usename");
                if (datname != null && datname.equalsIgnoreCase(migration.getDatabaseUser())) {
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
     * @param migration a {@link org.opennms.core.schema.Migration} object.
     * @throws org.opennms.core.schema.MigrationException if any.
     */
    public void createUser(final Migration migration) throws MigrationException {
        if (!m_createUser || databaseUserExists(migration)) {
            return;
        }

        LOG.info("creating OpenNMS user, if necessary");
        Statement st = null;
        ResultSet rs = null;
        Connection c = null;
        try {
            c = m_adminDataSource.getConnection();
            st = c.createStatement();
            st.execute("CREATE USER " + migration.getDatabaseUser() + " WITH PASSWORD '" + migration.getDatabasePassword() + "'");
        } catch (final SQLException e) {
            throw new MigrationException("an error occurred creating the OpenNMS user", e);
        } finally {
            cleanUpDatabase(c, null, st, rs);
        }
    }

    /**
     * <p>databaseExists</p>
     *
     * @param migration a {@link org.opennms.core.schema.Migration} object.
     * @return a boolean.
     * @throws org.opennms.core.schema.MigrationException if any.
     */
    public boolean databaseExists(final Migration migration) throws MigrationException {
        return databaseExists(migration.getDatabaseName());
    }

    public boolean databaseExists(String databaseName) throws MigrationException {
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
            throw new MigrationException("an error occurred determining whether the OpenNMS user exists", e);
        } finally {
            cleanUpDatabase(c, null, st, rs);
        }
    }

    public void createSchema(final Migration migration) throws MigrationException {
        if (!m_createDatabase || schemaExists(migration)) {
            return;
        }
    }

    public boolean schemaExists(final Migration migration) throws MigrationException {
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
     * @param migration a {@link org.opennms.core.schema.Migration} object.
     * @throws org.opennms.core.schema.MigrationException if any.
     */
    public void createDatabase(final Migration migration) throws MigrationException {
        if (!m_createDatabase || databaseExists(migration)) {
            return;
        }
        LOG.info("creating OpenNMS database, if necessary");
        if (!databaseUserExists(migration)) {
            throw new MigrationException(String.format("database will not be created: unable to grant access (user %s does not exist)", migration.getDatabaseUser()));
        }

        Statement st = null;
        ResultSet rs = null;
        Connection c = null;
        try {
            c = m_adminDataSource.getConnection();
            st = c.createStatement();
            st.execute("CREATE DATABASE \"" + migration.getDatabaseName() + "\" WITH ENCODING='UNICODE'");
            st.execute("GRANT ALL ON DATABASE \"" + migration.getDatabaseName() + "\" TO \"" + migration.getDatabaseUser() + "\"");
        } catch (final SQLException e) {
            throw new MigrationException("an error occurred creating the OpenNMS database", e);
        } finally {
            cleanUpDatabase(c, null, st, rs);
        }
    }

    /**
     * <p>checkUnicode</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void checkUnicode(final Migration migration) throws Exception {
        LOG.info("checking if database \"" + migration.getDatabaseName() + "\" is unicode");

        Statement st = null;
        ResultSet rs = null;
        Connection c = null;

        try {
                c = m_adminDataSource.getConnection();
                st = c.createStatement();
                rs = st.executeQuery("SELECT encoding FROM pg_database WHERE LOWER(datname)='" + migration.getDatabaseName().toLowerCase() + "'");
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
     * @throws java.sql.SQLException if any.
     */
    public void databaseSetUser(final Migration m_migration) throws MigrationException {
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
            st = c.prepareStatement("ALTER TABLE ? OWNER TO ?");
            for (final String objName : objects) {
                st.setString(1, objName);
                st.setString(2, m_migration.getDatabaseUser());
                st.execute();
            }
        } catch (SQLException e) {
            throw new MigrationException("an error occurred setting table ownership", e);
        } finally {
            cleanUpDatabase(c, null, st, rs);
        }
    }

    /**
     * <p>vacuumDatabase</p>
     *
     * @param full a boolean.
     * @throws java.sql.SQLException if any.
     */
    public void vacuumDatabase(final boolean full) throws MigrationException {
        Connection c = null;
        Statement st = null;

        try {
            c = m_dataSource.getConnection();

            st = c.createStatement();
            LOG.info("optimizing database (VACUUM ANALYZE)");
            st.execute("VACUUM ANALYZE");

            if (full) {
                LOG.info("recovering database disk space (VACUUM FULL)");
                st.execute("VACUUM FULL");
            }
        } catch (SQLException e) {
            throw new MigrationException("an error occurred vacuuming the databse", e);
        } finally {
            cleanUpDatabase(c, null, st, null);
        }
    }

    /**
     * <p>updateIplike</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void updateIplike() throws MigrationException {

        boolean insert_iplike = !isIpLikeUsable();

        if (insert_iplike) {
            dropExistingIpLike();

            if (!installCIpLike("foo")) {
                setupPlPgsqlIplike();
            }
        }

        // XXX This error is generated from Postgres if eventtime(text)
        // does not exist:
        // ERROR: function eventtime(text) does not exist
        LOG.info("checking for stale eventtime.so references");
        Connection c = null;
        Statement st = null;
        try {
            c = m_dataSource.getConnection();
            st = c.createStatement();
            st.execute("DROP FUNCTION eventtime(text)");
        } catch (final SQLException e) {
            /*
             * SQL Status code: 42883: ERROR: function %s does not exist
             */
            if (e.toString().indexOf("does not exist") != -1
                    || "42883".equals(e.getSQLState())) {
            } else {
                throw new MigrationException("error checking for stale eventtime.so references", e);
            }
        } finally {
            cleanUpDatabase(c, null, st, null);
        }
    }

    /**
     * <p>isIpLikeUsable</p>
     *
     * @return a boolean.
     */
    public boolean isIpLikeUsable() throws MigrationException {
        Connection c = null;
        Statement st = null;

        try {
            LOG.info("checking if iplike is usable");
            c = m_dataSource.getConnection();
            st = c.createStatement();

            try {
                st.execute("SELECT IPLIKE('127.0.0.1', '*.*.*.*')");
            } catch (final SQLException selectException) {
                return false;
            }

            st.close();

            LOG.info("checking if iplike supports IPv6");
            st = c.createStatement();
            st.execute("SELECT IPLIKE('fe80:0000:5ab0:35ff:feee:cecd', 'fe80:*::cecd')");
        } catch (final SQLException e) {
            throw new MigrationException("error checking if iplike is usable", e);
        } finally {
            cleanUpDatabase(c, null, st, null);
        }

        return true;
    }

    private boolean installCIpLike(String pgIplikeLocation) throws MigrationException {
        if (pgIplikeLocation == null) {
            LOG.info("Skipped inserting C iplike function (location of iplike function not set)");
            return false;
        }

        LOG.info("inserting C iplike function");

        Statement st = null;
        Connection c = null;

        try {
            c  = m_dataSource.getConnection();
            st = c.createStatement();
            try {
                st.execute("CREATE FUNCTION iplike(text,text) RETURNS bool " + "AS '" + pgIplikeLocation + "' LANGUAGE 'c' WITH(isstrict)");
                return true;
            } catch (final SQLException e) {
                return false;
            }
        } catch (final SQLException e) {
            throw new MigrationException("error installing C iplike function", e);
        } finally {
            cleanUpDatabase(c, null, st, null);
        }
    }

    private void dropExistingIpLike() throws MigrationException {
        Connection c = null;
        Statement st = null;

        LOG.info("removing existing iplike definition (if any)");
        try {
            c = m_dataSource.getConnection();
            st = c.createStatement();
            st.execute("DROP FUNCTION iplike(text,text)");
        } catch (final SQLException dropException) {
            if (dropException.toString().contains("does not exist")
                    || "42883".equals(dropException.getSQLState())) {
            } else {
                throw new MigrationException("could not remove existing iplike definition (if it exists)", dropException);
            }
        } finally {
            cleanUpDatabase(c, null, st, null);
        }
    }

    /**
     * <p>setupPlPgsqlIplike</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void setupPlPgsqlIplike() throws MigrationException {
        LOG.info("inserting PL/pgSQL iplike function");

        InputStream sqlfile = null;
        final StringBuffer createFunction = new StringBuffer();
        try {
            sqlfile = getClass().getResourceAsStream(IPLIKE_SQL_RESOURCE);
            if (sqlfile == null) {
                throw new MigrationException("unable to locate " + IPLIKE_SQL_RESOURCE);
            }

            final BufferedReader in = new BufferedReader(new InputStreamReader(sqlfile, StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
                createFunction.append(line).append("\n");
            }
        } catch (final IOException e) {
            throw new MigrationException("error reading PL/pgSQL iplike function from file " + IPLIKE_SQL_RESOURCE, e);
        } finally {
            // don't forget to close the input stream
            try {
                if (sqlfile != null) {
                    sqlfile.close();
                }
            } catch (final IOException e) {
                // purposefully eat it so we don't hide any exceptions that occurred earlier (and matter more)
            }
        }

        Connection c = null;
        Statement st = null;
        try {
            c = m_dataSource.getConnection();
            st = c.createStatement();

            st.execute(createFunction.toString());
        } catch (final SQLException e) {
            throw new MigrationException("could not insert PL/pgSQL iplike function", e);
        } finally {
            cleanUpDatabase(c, null, st, null);
        }
    }

    /**
     * <p>databaseRemoveDB</p>
     *
     * @throws java.sql.SQLException if any.
     */
    public void databaseRemoveDB(Migration migration) throws MigrationException {
        LOG.info("removing database '" + migration.getDatabaseName());

        Connection c = null;
        Statement st = null;

        try {
            c = m_adminDataSource.getConnection();
            st = c.createStatement();
            st.execute("DROP DATABASE \"" + migration.getDatabaseName() + "\"");
        } catch (SQLException e) {
            throw new MigrationException("could not remove database " + migration.getDatabaseName(), e);
        } finally {
            cleanUpDatabase(c, null, st, null);
        }
    }

    /**
     * <p>prepareDatabase</p>
     *
     * @param migration a {@link org.opennms.core.schema.Migration} object.
     * @throws org.opennms.core.schema.MigrationException if any.
     */
    public void prepareDatabase(final Migration migration) throws MigrationException {
        validateDatabaseVersion();
        createUser(migration);
        createSchema(migration);
        createDatabase(migration);
        createLangPlPgsql();
    }

    /**
     * <p>migrate</p>
     *
     * @param migration a {@link org.opennms.core.schema.Migration} object.
     * @throws org.opennms.core.schema.MigrationException if any.
     */
    public void migrate(final Migration migration) throws MigrationException {
        Connection connection = null;
        DatabaseConnection dbConnection = null;

        try {
            connection = m_dataSource.getConnection();
            dbConnection = new JdbcConnection(connection);

            ResourceAccessor accessor = migration.getAccessor();
            if (accessor == null) accessor = new SpringResourceAccessor();

            final Liquibase liquibase = new Liquibase( migration.getChangeLog(), accessor, dbConnection );
            liquibase.setChangeLogParameter("install.database.admin.user", migration.getAdminUser());
            liquibase.setChangeLogParameter("install.database.admin.password", migration.getAdminPassword());
            liquibase.setChangeLogParameter("install.database.user", migration.getDatabaseUser());
            liquibase.getDatabase().setDefaultSchemaName(migration.getSchemaName());

            final String contexts = System.getProperty("opennms.contexts", "production");
            liquibase.update(contexts);
        } catch (final Throwable e) {
            throw new MigrationException("unable to migrate the database", e);
        } finally {
            cleanUpDatabase(connection, dbConnection, null, null);
        }
    }

    public void generateChangelog() {

    }

    /**
     * <p>getMigrationResourceLoader</p>
     *
     * @param migration a {@link org.opennms.core.schema.Migration} object.
     * @return a {@link org.springframework.core.io.ResourceLoader} object.
     */
    protected ResourceLoader getMigrationResourceLoader(final Migration migration) {
        final File changeLog = new File(migration.getChangeLog());
        final List<URL> urls = new ArrayList<URL>();
        try {
            if (changeLog.exists()) {
                urls.add(changeLog.getParentFile().toURI().toURL());
            }
        } catch (final MalformedURLException e) {
		LOG.info("unable to figure out URL for {}", migration.getChangeLog(), e);
        }
        final ClassLoader cl = new URLClassLoader(urls.toArray(new URL[0]), this.getClass().getClassLoader());
        return new DefaultResourceLoader(cl);
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

}
