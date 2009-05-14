package org.opennms.core.schema;

import java.sql.Driver;

import liquibase.CompositeFileOpener;
import liquibase.FileSystemFileOpener;
import liquibase.Liquibase;
import liquibase.commandline.CommandLineFileOpener;
import liquibase.commandline.CommandLineUtils;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.JDBCException;

public class Migration {
    private String m_jdbcUrl;
    private String m_jdbcDriver = "org.postgresql.Driver";
    private String m_databaseHost;
    private String m_databaseName;
    private String m_databaseUser;
    private String m_adminUser;
    private String m_adminPassword;
    private String m_migrationFile;

    /**
     * Get the JDBC connection URL.  Defaults to jdbc:postgresql://host/database.
     * @return the JDBC URL
     */
    public String getJdbcUrl() {
        if (m_jdbcUrl == null) {
               return String.format("jdbc:postgresql://%s/%s", getDatabaseHost(), getDatabaseName()); 
        }
        return m_jdbcUrl;
    }
    public void  setJdbcUrl(String jdbcUrl) {
        m_jdbcUrl = jdbcUrl;
    }

    /**
     * Get the JDBC driver class name.  Defaults to "org.postgresql.Driver"
     * @return the class name
     */
    public String getJdbcDriver() {
        return m_jdbcDriver;
    }
    public void setJdbcDriver(String jdbcDriver) {
        m_jdbcDriver = jdbcDriver;
    }

    public String getDatabaseHost() {
        return m_databaseHost;
    }
    public void setDatabaseHost(String databaseHost) {
        m_databaseHost = databaseHost;
    }

    public String getDatabaseName() {
        return m_databaseName;
    }
    public void setDatabaseName(String databaseName) {
        m_databaseName = databaseName;
    }

    public String getDatabaseUser() {
        return m_databaseUser;
    }
    public void setDatabaseUser(String databaseUser) {
        m_databaseUser = databaseUser;
    }

    public String getAdminUser() {
        return m_adminUser;
    }
    public void setAdminUser(String adminUser) {
        m_adminUser = adminUser;
    }

    public String getAdminPassword() {
        return m_adminPassword;
    }
    public void setAdminPassword(String adminPassword) {
        m_adminPassword = adminPassword;
    }

    public String getMigrationFile() {
        return m_migrationFile;
    }
    public void setMigrationFile(String migrationFile) {
        m_migrationFile = migrationFile;
    }

    public void perform() throws MigrationException {
        Database db;
        try {
            db = CommandLineUtils.createDatabaseObject(
                this.getClass().getClassLoader(),
                getJdbcUrl(),
                getAdminUser(),
                getAdminPassword(),
                getJdbcDriver(),
                "public",
                null);
        } catch (JDBCException e) {
            throw new MigrationException("unable to get database implementation for JDBC driver " + getJdbcDriver(), e);
        }

        if (db != null) {
            FileSystemFileOpener fsOpener = new FileSystemFileOpener();
            CommandLineFileOpener clOpener = new CommandLineFileOpener(this.getClass().getClassLoader());
            CompositeFileOpener fileOpener = new CompositeFileOpener(fsOpener, clOpener);
            Liquibase liquibase = new Liquibase(getMigrationFile(), fileOpener, db);

        }
    }

    private ClassLoader getMigrationClassLoader() {
        return null;
    }
}
