package org.opennms.core.schema;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.log.LogFactory;

import org.opennms.core.utils.ThreadCategory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

/**
 * <p>Migrator class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Migrator {
    /** Constant <code>POSTGRES_MIN_VERSION=7.4f</code> */
    public static final float POSTGRES_MIN_VERSION = 7.4f;
    /** Constant <code>POSTGRES_MAX_VERSION_PLUS_ONE=9.1f</code> */
    public static final float POSTGRES_MAX_VERSION_PLUS_ONE = 9.1f;

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
        LogFactory.getLogger().setLevel(Level.INFO);
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
    public void setDataSource(DataSource dataSource) {
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
    public void setAdminDataSource(DataSource dataSource) {
        m_adminDataSource = dataSource;
    }

    /**
     * <p>setValidateDatabaseVersion</p>
     *
     * @param validate a boolean.
     */
    public void setValidateDatabaseVersion(boolean validate) {
        m_validateDatabaseVersion = validate;
    }

    /**
     * <p>setCreateUser</p>
     *
     * @param create a boolean.
     */
    public void setCreateUser(boolean create) {
        m_createUser = create;
    }

    /**
     * <p>setCreateDatabase</p>
     *
     * @param create a boolean.
     */
    public void setCreateDatabase(boolean create) {
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
            } catch (SQLException e) {
                throw new MigrationException("an error occurred getting the version from the database", e);
            } finally {
                cleanUpDatabase(c, st, rs);
            }

            Matcher m = Pattern.compile("^PostgreSQL (\\d+\\.\\d+)").matcher(versionString);

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
            log().info("skipping database version validation");
            return;
        }
        log().info("validating database version");

        Float dbv = getDatabaseVersion();
        if (dbv == null) {
            throw new MigrationException("unable to determine database version");
        }

        String message = "Unsupported database version \"" + dbv + "\" -- you need at least "
            + POSTGRES_MIN_VERSION + " and less than " + POSTGRES_MAX_VERSION_PLUS_ONE
            + ".  Use the \"-Q\" option to disable this check if you feel brave and are willing "
            + "to find and fix bugs found yourself.";
        
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
        log().info("adding PL/PgSQL support to the database, if necessary");
        Statement st = null;
        ResultSet rs = null;
        Connection c = null;
        try {
            c = m_dataSource.getConnection();
            st = c.createStatement();
            rs = st.executeQuery("SELECT oid FROM pg_proc WHERE " + "proname='plpgsql_call_handler' AND " + "proargtypes = ''");
            if (rs.next()) {
                log().info("PL/PgSQL call handler exists");
            } else {
                log().info("adding PL/PgSQL call handler");
                st.execute("CREATE FUNCTION plpgsql_call_handler () " + "RETURNS OPAQUE AS '$libdir/plpgsql." + getExtension(false) + "' LANGUAGE 'c'");
            }

            rs = st.executeQuery("SELECT pg_language.oid "
                + "FROM pg_language, pg_proc WHERE "
                + "pg_proc.proname='plpgsql_call_handler' AND "
                + "pg_proc.proargtypes = '' AND "
                + "pg_proc.oid = pg_language.lanplcallfoid AND "
                + "pg_language.lanname = 'plpgsql'");
            if (rs.next()) {
                log().info("PL/PgSQL language exists");
            } else {
                log().info("adding PL/PgSQL language");
                st.execute("CREATE TRUSTED PROCEDURAL LANGUAGE 'plpgsql' "
                    + "HANDLER plpgsql_call_handler LANCOMPILER 'PL/pgSQL'");
            }
        } catch (SQLException e) {
            throw new MigrationException("an error occurred getting the version from the database", e);
        } finally {
            cleanUpDatabase(c, st, rs);
        }
    }

    /**
     * <p>databaseUserExists</p>
     *
     * @param migration a {@link org.opennms.core.schema.Migration} object.
     * @return a boolean.
     * @throws org.opennms.core.schema.MigrationException if any.
     */
    public boolean databaseUserExists(Migration migration) throws MigrationException {
        Statement st = null;
        ResultSet rs = null;
        Connection c = null;
        try {
            c = m_adminDataSource.getConnection();
            st = c.createStatement();
            rs = st.executeQuery("SELECT usename FROM pg_user WHERE usename = '" + migration.getDatabaseUser() + "'");
            if (rs.next()) {
                String datname = rs.getString("usename");
                if (datname != null && datname.equalsIgnoreCase(migration.getDatabaseUser())) {
                    return true;
                } else {
                    return false;
                }
            }
            return rs.next();
        } catch (SQLException e) {
            throw new MigrationException("an error occurred determining whether the OpenNMS user exists", e);
        } finally {
            cleanUpDatabase(c, st, rs);
        }
    }

    /**
     * <p>createUser</p>
     *
     * @param migration a {@link org.opennms.core.schema.Migration} object.
     * @throws org.opennms.core.schema.MigrationException if any.
     */
    public void createUser(Migration migration) throws MigrationException {
        if (!m_createUser || databaseUserExists(migration)) {
            return;
        }

        log().info("creating OpenNMS user, if necessary");
        Statement st = null;
        ResultSet rs = null;
        Connection c = null;
        try {
            c = m_adminDataSource.getConnection();
            st = c.createStatement();
            st.execute("CREATE USER " + migration.getDatabaseUser() + " WITH PASSWORD '" + migration.getDatabasePassword() + "' CREATEDB CREATEUSER");
        } catch (SQLException e) {
            throw new MigrationException("an error occurred creating the OpenNMS user", e);
        } finally {
            cleanUpDatabase(c, st, rs);
        }
    }

    /**
     * <p>databaseExists</p>
     *
     * @param migration a {@link org.opennms.core.schema.Migration} object.
     * @return a boolean.
     * @throws org.opennms.core.schema.MigrationException if any.
     */
    public boolean databaseExists(Migration migration) throws MigrationException {
        Statement st = null;
        ResultSet rs = null;
        Connection c = null;
        try {
            c = m_adminDataSource.getConnection();
            st = c.createStatement();
            rs = st.executeQuery("SELECT datname from pg_database WHERE datname = '" + migration.getDatabaseName() + "'");
            if (rs.next()) {
                String datname = rs.getString("datname");
                if (datname != null && datname.equalsIgnoreCase(migration.getDatabaseName())) {
                    return true;
                } else {
                    return false;
                }
            }
            return rs.next();
        } catch (SQLException e) {
            throw new MigrationException("an error occurred determining whether the OpenNMS user exists", e);
        } finally {
            cleanUpDatabase(c, st, rs);
        }
    }

    /**
     * <p>createDatabase</p>
     *
     * @param migration a {@link org.opennms.core.schema.Migration} object.
     * @throws org.opennms.core.schema.MigrationException if any.
     */
    public void createDatabase(Migration migration) throws MigrationException {
        if (!m_createDatabase || databaseExists(migration)) {
            return;
        }
        log().info("creating OpenNMS database, if necessary");
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
        } catch (SQLException e) {
            throw new MigrationException("an error occurred creating the OpenNMS database", e);
        } finally {
            cleanUpDatabase(c, st, rs);
        }
    }

    /**
     * <p>prepareDatabase</p>
     *
     * @param migration a {@link org.opennms.core.schema.Migration} object.
     * @throws org.opennms.core.schema.MigrationException if any.
     */
    public void prepareDatabase(Migration migration) throws MigrationException {
        validateDatabaseVersion();
        createUser(migration);
        createDatabase(migration);
        createLangPlPgsql();
    }

    /**
     * <p>migrate</p>
     *
     * @param migration a {@link org.opennms.core.schema.Migration} object.
     * @throws org.opennms.core.schema.MigrationException if any.
     */
    public void migrate(Migration migration) throws MigrationException {
        Connection connection;
        Database database;

        SpringFileOpener sfo = new SpringFileOpener();
        sfo.setResourceLoader(getMigrationResourceLoader(migration));

        try {
            connection = m_dataSource.getConnection();
        } catch (Exception e) {
            throw new MigrationException("unable to get a database connection from the datasource", e);
        }

        try {
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
        } catch (Exception e) {
            cleanUpDatabase(connection, null, null);
            throw new MigrationException("unable to determine the Liquibase database object", e);
        }

        Liquibase liquibase = new Liquibase( migration.getChangeLog(), sfo, database );
        liquibase.setChangeLogParameterValue("install.database.admin.user", migration.getAdminUser());
        liquibase.setChangeLogParameterValue("install.database.admin.password", migration.getAdminPassword());
        liquibase.setChangeLogParameterValue("install.database.user", migration.getDatabaseUser());

        final String contexts = System.getProperty("opennms.contexts", "production");
        try {
            liquibase.update(contexts);
        } catch (Exception e) {
            cleanUpDatabase(connection, null, null);
            throw new MigrationException("unable to update the database", e);
        }

        cleanUpDatabase(connection, null, null);
    }

    /**
     * <p>getMigrationResourceLoader</p>
     *
     * @param migration a {@link org.opennms.core.schema.Migration} object.
     * @return a {@link org.springframework.core.io.ResourceLoader} object.
     */
    protected ResourceLoader getMigrationResourceLoader(Migration migration) {
        File changeLog = new File(migration.getChangeLog());
        List<URL> urls = new ArrayList<URL>();
        try {
            if (changeLog.exists()) {
                urls.add(changeLog.getParentFile().toURI().toURL());
            }
        } catch (MalformedURLException e) {
            log().warn("unable to figure out URL for " + migration.getChangeLog(), e);
        }
        ClassLoader cl = new URLClassLoader(urls.toArray(new URL[0]), this.getClass().getClassLoader());
        return new DefaultResourceLoader(cl);
    }

    private void cleanUpDatabase(Connection c, Statement st, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log().warn("unable to close version-check result set", e);
            }
        }
        if (st != null) {
            try {
                st.close();
            } catch (SQLException e) {
                log().warn("unable to close version-check statement", e);
            }
        }
        if (c != null) {
            try {
                c.close();
            } catch (SQLException e) {
                log().warn("unable to close version-check connection", e);
            }
        }
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(Migrator.class);
    }

}
