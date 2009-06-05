package org.opennms.core.schema;


public class Migration {
    private String m_jdbcUrl;
    private String m_jdbcDriver = "org.postgresql.Driver";
    private String m_databaseHost;
    private String m_databaseName;
    private String m_databaseUser;
    private String m_databasePassword;
    private String m_adminUser;
    private String m_adminPassword;
    private String m_changeLog;

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

    public String getDatabasePassword() {
        return m_databasePassword;
    }
    public void setDatabasePassword(String databasePassword) {
        m_databasePassword = databasePassword;
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

    public String getChangeLog() {
        return m_changeLog;
    }
    public void setChangeLog(String changeLog) {
        m_changeLog = changeLog;
    }
}
