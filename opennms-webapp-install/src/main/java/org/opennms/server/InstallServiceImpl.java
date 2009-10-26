package org.opennms.server;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.client.DatabaseConnectionSettings;
import org.opennms.client.DatabaseDoesNotExistException;
import org.opennms.client.InstallService;
import org.opennms.client.LoggingEvent;
import org.opennms.client.LoggingEvent.LogLevel;
import org.opennms.install.Installer;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.opennmsDataSources.DataSourceConfiguration;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.dao.db.InstallerDb;
import org.opennms.netmgt.dao.db.SimpleDataSource;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class InstallServiceImpl extends RemoteServiceServlet implements InstallService {
    private static final long serialVersionUID = 7921657972081359016L;

    private static final String OWNERSHIP_FILE_SESSION_ATTRIBUTE = "__install_ownership_file";
    // private static final String DATABASE_SETTINGS_SESSION_ATTRIBUTE = "__install_database_settings";
    private static final String OPENNMS_DATASOURCE_NAME = "opennms";
    private static final String OPENNMS_ADMIN_DATASOURCE_NAME = "opennms-admin";
    private static final String OPENNMS_DB_USERNAME = "opennms";
    private static final String OPENNMS_DB_PASSWORD = "opennms";

    private static boolean m_updateIsInProgress = false;
    private static boolean m_lastUpdateSucceeded = false;

    /**
     * Fetch the OpenNMS home directory, as set in the <code>opennms.home</code>
     * system property.
     */
    protected String getOpennmsInstallPath() {
        return ConfigFileConstants.getHome();
    }

    public boolean checkOwnershipFileExists() {
        // return true;
        HttpServletRequest request = this.getThreadLocalRequest();
        if (request == null) {
            throw new IllegalStateException("No HTTP request object available.");
        }

        HttpSession session = request.getSession(true);
        if (session == null) {
            throw new IllegalStateException("No HTTP session object available.");
        }

        String attribute = (String)session.getAttribute(OWNERSHIP_FILE_SESSION_ATTRIBUTE);
        if (attribute == null) {
            return false;
        } else {
            if (new File(this.getOpennmsInstallPath(), attribute).isFile()) {
                return true;
            } else {
                return false;
            }
        }
    }

    public String getOwnershipFilename(){
        HttpServletRequest request = this.getThreadLocalRequest();
        if (request == null) {
            throw new IllegalStateException("No HTTP request object available.");
        }

        HttpSession session = request.getSession(true);
        if (session == null) {
            throw new IllegalStateException("No HTTP session object available.");
        }

        String attribute = (String)session.getAttribute(OWNERSHIP_FILE_SESSION_ATTRIBUTE);
        if (attribute == null) {
            attribute = "opennms_" + Math.round(Math.random() * (double)100000000) + ".txt";
            session.setAttribute(OWNERSHIP_FILE_SESSION_ATTRIBUTE, attribute);
        }
        return attribute;
    }

    public void resetOwnershipFilename() {
        HttpSession session = this.getThreadLocalRequest().getSession(true);
        String attribute = "opennms_" + Math.round(Math.random() * (double)100000000) + ".txt";
        session.setAttribute(OWNERSHIP_FILE_SESSION_ATTRIBUTE, attribute);
    }

    public boolean isAdminPasswordSet() throws IllegalStateException {
        /*
        Userinfo userinfo = null;
        try {
            userinfo = (Userinfo)Unmarshaller.unmarshal(Userinfo.class, 
                new FileReader(
                    new File(
                        new File(this.getOpennmsInstallPath(), "etc"), 
                        "users.xml"
                    )
                )
            );
        } catch (MarshalException e) {
            throw new IllegalStateException("<code>users.xml</code> file cannot be read properly.");
        } catch (ValidationException e) {
            throw new IllegalStateException("<code>users.xml</code> file is invalid and cannot be read.");
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("<code>users.xml</code> file cannot be found in the OpenNMS home directory.");
        }

        for(User user : userinfo.getUsers().getUser()) {
            if ("admin".equals(user.getUserId())) {
                if (user.getPassword() == null || "".equals(user.getPassword().trim())) {
                    // If the password is null or blank, return false
                    return false;
                } else if ("21232F297A57A5A743894A0E4A801FC3".equals(user.getPassword())) {
                    // If the password is still set to the default value, return false
                    return false;
                } else {
                    return true;
                }
            }
        }
        // If there is no "admin" entry in users.xml, return false
        return true;
         */

        UserManager manager = UserFactory.getInstance();
        try {
            User user = manager.getUser("admin");
            if (user == null) {
                throw new IllegalStateException("There is no <code>admin</code> user in the <code>users.xml</code> file.");
            } else {
                if (user.getPassword() == null || "".equals(user.getPassword().trim())) {
                    // If the password is null or blank, return false
                    return false;
                } else if ("21232F297A57A5A743894A0E4A801FC3".equals(user.getPassword().trim())) {
                    // If the password is still set to the default value, return false
                    return false;
                } else {
                    return true;
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not check password: " + e.getMessage());
        }
    }

    public void setAdminPassword(String password) {
        UserManager manager = UserFactory.getInstance();
        try {
            manager.setUnencryptedPassword("admin", password);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not store password: " + e.getMessage());
        }
    }

    public DatabaseConnectionSettings getDatabaseConnectionSettings() throws IllegalStateException {
        String dbName = null;
        String adminUser = null;
        String adminPassword = null;
        String driver = null;
        String adminUrl = null;
        String url = null;

        File configFile = new File(new File(this.getOpennmsInstallPath(), "etc"), "opennms-datasources.xml");
        DataSourceConfiguration config = null;
        if (configFile.canRead()) {
            try {
                config = DataSourceConfiguration.unmarshal(new FileReader(configFile));
            } catch (MarshalException e) {
                throw new IllegalStateException("Cannot read from <code>" + configFile.getPath() + "</code>: " + e.getMessage());
            } catch (ValidationException e) {
                throw new IllegalArgumentException("Invalid configuration data in <code>" + configFile.getPath() + "</code>: " + e.getMessage());
            } catch (IOException e) {
                throw new IllegalStateException("Cannot read from <code>" + configFile.getPath() + "</code>: " + e.getMessage());
            }
        } else {
            throw new IllegalStateException("Location <code>" + configFile.getPath() + "</code> is not readable.");
        }

        for (JdbcDataSource ds : config.getJdbcDataSource()) {
            if (OPENNMS_DATASOURCE_NAME.equals(ds.getName())) {
                url = ds.getUrl();
                // TODO: Fetch username and password of opennms user
            } else if (OPENNMS_ADMIN_DATASOURCE_NAME.equals(ds.getName())) {
                dbName = ds.getDatabaseName();
                adminUser = ds.getUserName();
                adminPassword = ds.getPassword();
                driver = ds.getClassName();
                adminUrl = ds.getUrl();
            }
        }

        return new DatabaseConnectionSettings(dbName, adminUser, adminPassword, driver, adminUrl, url);
    }

    public void connectToDatabase(String dbName, String dbAdminUser, String dbAdminPassword, String driver, String dbAdminUrl, String dbNmsUrl) throws IllegalStateException {
        validateDbParameters(new DatabaseConnectionSettings(dbName, dbAdminUser, dbAdminPassword, driver, dbAdminUrl, dbNmsUrl));
        InstallerDb db = new InstallerDb();
        db.setDatabaseName(dbName);
        // Only used when creating an OpenNMS user in the database
        db.setPostgresOpennmsUser(OPENNMS_DB_USERNAME);
        db.setPostgresOpennmsPassword(OPENNMS_DB_PASSWORD);
        try {
            db.setAdminDataSource(new SimpleDataSource(driver, dbAdminUrl, dbAdminUser, dbAdminPassword));
            db.setDataSource(new SimpleDataSource(driver, dbNmsUrl, OPENNMS_DB_USERNAME, OPENNMS_DB_PASSWORD));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("PostgreSQL driver could not be loaded.", e);
        }

        boolean databaseExists = false;
        try {
            databaseExists = db.databaseDBExists();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not check database list: " + e.getMessage());
        }

        if (databaseExists) {
            // TODO: Change this to an appropriate connection test
            // Try to vacuum the database to test connectivity
            try {
                db.vacuumDatabase(false);
                // If the test completes, then store the database connectivity information
                this.setDatabaseConfig(dbName, dbAdminUser, dbAdminPassword, driver, dbAdminUrl, dbNmsUrl);
            } catch (SQLException e) {
                throw new IllegalArgumentException("Database connection failed: " + e.getMessage());
            }
        } else {
            throw new DatabaseDoesNotExistException();
        }
    }

    public void createDatabase(String dbName, String dbAdminUser, String dbAdminPassword, String driver, String dbAdminUrl) throws DatabaseDoesNotExistException, IllegalStateException {
        validateDbParameters(new DatabaseConnectionSettings(dbName, dbAdminUser, dbAdminPassword, driver, dbAdminUrl, null));
        InstallerDb db = new InstallerDb();
        db.setDatabaseName(dbName);
        // Only used when creating an OpenNMS user in the database
        db.setPostgresOpennmsUser(OPENNMS_DB_USERNAME);
        db.setPostgresOpennmsPassword(OPENNMS_DB_PASSWORD);
        try {
            db.setAdminDataSource(new SimpleDataSource(driver, dbAdminUrl, dbAdminUser, dbAdminPassword));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("PostgreSQL driver could not be loaded.", e);
        }

        // Sanity check to make sure that the database doesn't already exist
        boolean databaseExists = true;
        try {
            databaseExists = db.databaseDBExists();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not check database list: " + e.getMessage());
        }

        if (databaseExists) {
            throw new IllegalStateException("Database already exists.");
        } else {
            try {
                db.databaseAddUser();
                db.databaseAddDB();
            } catch (SQLException e) {
                throw new IllegalStateException(e.getMessage());
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage());
            }
        }
    }

    /**
     * TODO: Figure out if we always want to persist database connection settings directly
     * to the opennms-datasources.xml file or if we should wait until the end to write to
     * the configuration files.
     */
    protected void setDatabaseConfig(String dbName, String dbAdminUser, String dbAdminPassword, String driver, String dbAdminUrl, String dbNmsUrl) throws IllegalStateException, IllegalArgumentException {
        validateDbParameters(new DatabaseConnectionSettings(dbName, dbAdminUser, dbAdminPassword, driver, dbAdminUrl, dbNmsUrl));
        /*
        HttpSession session = this.getThreadLocalRequest().getSession(true);
        session.setAttribute(DATABASE_SETTINGS_SESSION_ATTRIBUTE, new String[] {
            dbName,
            user,
            password,
            driver,
            url,
            binaryDirectory
        });
         */

        JdbcDataSource adminDs = new JdbcDataSource();
        adminDs.setClassName(driver);
        adminDs.setDatabaseName(dbName);
        adminDs.setName(OPENNMS_ADMIN_DATASOURCE_NAME);
        adminDs.setPassword(dbAdminPassword);
        adminDs.setUrl(dbNmsUrl);
        adminDs.setUserName(dbAdminUser);

        JdbcDataSource opennmsDs = new JdbcDataSource();
        opennmsDs.setClassName(driver);
        opennmsDs.setDatabaseName(dbName);
        opennmsDs.setName(OPENNMS_DATASOURCE_NAME);
        opennmsDs.setPassword(OPENNMS_DB_PASSWORD);
        opennmsDs.setUrl(dbNmsUrl);
        opennmsDs.setUserName(OPENNMS_DB_USERNAME);

        DataSourceConfiguration config = new DataSourceConfiguration();
        config.addJdbcDataSource(opennmsDs);
        config.addJdbcDataSource(adminDs);
        File configFile = new File(new File(this.getOpennmsInstallPath(), "etc"), "opennms-datasources.xml");
        if (configFile.canWrite()) {
            try {
                config.marshal(new FileWriter(configFile));
            } catch (MarshalException e) {
                throw new IllegalStateException("Cannot write to <code>" + configFile.getPath() + "</code>: " + e.getMessage());
            } catch (ValidationException e) {
                throw new IllegalArgumentException("Invalid configuration data specified: " + e.getMessage());
            } catch (IOException e) {
                throw new IllegalStateException("Cannot write to <code>" + configFile.getPath() + "</code>: " + e.getMessage());
            }
        } else {
            throw new IllegalStateException("Location <code>" + configFile.getPath() + "</code> is not writable.");
        }
    }

    public List<LoggingEvent> getDatabaseUpdateLogs(int offset){
        List<LoggingEvent> retval = new ArrayList<LoggingEvent>();
        for (org.apache.log4j.spi.LoggingEvent event : ((ListAppender)Logger.getRootLogger().getAppender("UNCATEGORIZED")).getEvents(offset, 200)) {
            LogLevel level = null;
            switch(event.getLevel().toInt()) {
            case (Level.TRACE_INT): level = LogLevel.TRACE; break;
            case (Level.DEBUG_INT): level = LogLevel.DEBUG; break;
            case (Level.INFO_INT): level = LogLevel.INFO; break;
            case (Level.WARN_INT): level = LogLevel.WARN; break;
            case (Level.ERROR_INT): level = LogLevel.ERROR; break;
            case (Level.FATAL_INT): level = LogLevel.FATAL; break;
            // If the level is set to any other value, skip this message
            default: continue;
            }
            retval.add(new LoggingEvent(event.getLoggerName(), event.getTimeStamp(), level, event.getMessage().toString()));
        }
        return retval;
    }

    public void clearDatabaseUpdateLogs(){
        ListAppender appender = ((ListAppender)Logger.getRootLogger().getAppender("UNCATEGORIZED"));
        appender.clear();
    }

    /**
     * Initiate the installer class. This will generate log messages that will be
     * relayed to the web UI by the log4j appender.
     */
    public void updateDatabase() {
        Thread thread = new Thread() {
            public void run() {
                // Don't need synchronized blocks when updating a boolean primitive
                m_updateIsInProgress = true;
                try {
                    Installer.main(new String[] { "-dis" });
                    m_lastUpdateSucceeded = true;
                } catch (Exception e) {
                    Logger.getLogger(this.getClass()).error("Installation failed: " + e.getMessage(), e);
                    m_lastUpdateSucceeded = false;
                } finally {
                    m_updateIsInProgress = false;
                }
            }
        };
        thread.start();
    }

    public boolean isUpdateInProgress() {
        // Don't need synchronized blocks when accessing a boolean primitive
        return m_updateIsInProgress;
    }

    public boolean didLastUpdateSucceed() {
        // Don't need synchronized blocks when accessing a boolean primitive
        return m_lastUpdateSucceeded;
    }

    public boolean checkIpLike() throws IllegalStateException {
        // We should have a proper opennms-datasources.xml stored at this point so try to load it
        // by using the normal {@link org.opennms.netmgt.config.DataSourceFactory} class.
        try {
            DataSourceFactory.init();
        } catch (MarshalException e) {
            throw new IllegalStateException("Could not load database configuration: " + e.getMessage());
        } catch (ValidationException e) {
            throw new IllegalStateException("Could not load database configuration: " + e.getMessage());
        } catch (IOException e) {
            throw new IllegalStateException("Could not load database configuration: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("PostgreSQL driver could not be loaded: " + e.getMessage());
        } catch (PropertyVetoException e) {
            throw new IllegalStateException("Could not load database configuration: " + e.getMessage());
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load database configuration: " + e.getMessage());
        }

        /*
        InstallerDb db = new InstallerDb();
        HttpSession session = this.getThreadLocalRequest().getSession(true);
        String[] dbSettings = (String[])session.getAttribute(DATABASE_SETTINGS_SESSION_ATTRIBUTE);
        if (dbSettings == null || dbSettings.length != 6) {
            throw new IllegalStateException("Database settings have not been specified yet.");
        }

        // TODO: Replace indices with constants
        db.setDatabaseName(dbSettings[0]);
        db.setPostgresOpennmsUser(OPENNMS_DB_USERNAME);
        db.setPostgresOpennmsPassword(OPENNMS_DB_PASSWORD);
        try {
            db.setAdminDataSource(new SimpleDataSource(dbSettings[3], dbSettings[4], dbSettings[1], dbSettings[2]));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("PostgreSQL driver could not be loaded.", e);
        }

        // TODO: Are there additional tests that we need to perform on the database?
        return db.isIpLikeUsable();
         */

        InstallerDb db = new InstallerDb();
        db.setAdminDataSource(DataSourceFactory.getInstance(OPENNMS_ADMIN_DATASOURCE_NAME));
        db.setDataSource(DataSourceFactory.getInstance(OPENNMS_DATASOURCE_NAME));
        // TODO: Are there additional tests that we need to perform on the database?
        return db.isIpLikeUsable();
    }

    protected static void validateDbParameters(DatabaseConnectionSettings settings) throws IllegalArgumentException {
        if (settings.getDbName() == null || "".equals(settings.getDbName().trim())) {
            throw new IllegalArgumentException("Database name cannot be blank.");
        } else if (settings.getDriver() == null || "".equals(settings.getDriver().trim())) {
            throw new IllegalArgumentException("Driver class cannot be blank.");
        } else if (settings.getAdminUser() == null || "".equals(settings.getAdminUser().trim())) {
            throw new IllegalArgumentException("Admin user cannot be blank.");
        } else if (settings.getAdminPassword() == null || "".equals(settings.getAdminPassword().trim())) {
            throw new IllegalArgumentException("Admin password cannot be blank.");
        }
    }
}
