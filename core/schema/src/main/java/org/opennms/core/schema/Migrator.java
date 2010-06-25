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

public class Migrator {
    public static final float POSTGRES_MIN_VERSION = 7.4f;
    public static final float POSTGRES_MAX_VERSION_PLUS_ONE = 9.1f;

    private DataSource m_dataSource;
    private DataSource m_adminDataSource;
    private Float m_databaseVersion;
    private boolean m_validateDatabaseVersion = true;
    private boolean m_createUser = true;
    private boolean m_createDatabase = true;

    public Migrator() {
        initLogging();
    }

    private void initLogging() {
        LogFactory.getLogger().setLevel(Level.INFO);
    }

    public DataSource getDataSource() {
        return m_dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
    }

    public DataSource getAdminDataSource() {
        return m_adminDataSource;
    }

    public void setAdminDataSource(DataSource dataSource) {
        m_adminDataSource = dataSource;
    }

    public void setValidateDatabaseVersion(boolean validate) {
        m_validateDatabaseVersion = validate;
    }

    public void setCreateUser(boolean create) {
        m_createUser = create;
    }

    public void setCreateDatabase(boolean create) {
        m_createDatabase = create;
    }

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
    
    public void validateDatabaseVersion() throws MigrationException {
        if (!m_validateDatabaseVersion) {
            return;
        }

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

    public void createLangPlPgsql() throws MigrationException {
        Statement st = null;
        ResultSet rs = null;
        Connection c = null;
        try {
            c = m_adminDataSource.getConnection();
            st = c.createStatement();
            rs = st.executeQuery("SELECT oid FROM pg_proc WHERE " + "proname='plpgsql_call_handler' AND " + "proargtypes = ''");
            if (!rs.next()) {
                st.execute("CREATE FUNCTION plpgsql_call_handler () " + "RETURNS OPAQUE AS '$libdir/plpgsql' LANGUAGE 'c'");
            }

            rs = st.executeQuery("SELECT pg_language.oid "
                + "FROM pg_language, pg_proc WHERE "
                + "pg_proc.proname='plpgsql_call_handler' AND "
                + "pg_proc.proargtypes = '' AND "
                + "pg_proc.oid = pg_language.lanplcallfoid AND "
                + "pg_language.lanname = 'plpgsql'");
            if (!rs.next()) {
                st.execute("CREATE TRUSTED PROCEDURAL LANGUAGE 'plpgsql' "
                    + "HANDLER plpgsql_call_handler LANCOMPILER 'PL/pgSQL'");
            }
        } catch (SQLException e) {
            throw new MigrationException("an error occurred getting the version from the database", e);
        } finally {
            cleanUpDatabase(c, st, rs);
        }
    }

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

    public void createUser(Migration migration) throws MigrationException {
        if (!m_createUser || databaseUserExists(migration)) {
            return;
        }

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

    public void createDatabase(Migration migration) throws MigrationException {
        if (!m_createDatabase || databaseExists(migration)) {
            return;
        }
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

    public void prepareDatabase(Migration migration) throws MigrationException {
        log().info("validating database version");
        validateDatabaseVersion();
        log().info("adding PL/PgSQL support to the database, if necessary");
        createLangPlPgsql();
        log().info("creating OpenNMS user, if necessary");
        createUser(migration);
        log().info("creating OpenNMS database, if necessary");
        createDatabase(migration);
    }

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
