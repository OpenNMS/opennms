package org.opennms.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.client.InstallService;
import org.opennms.client.LoggingEvent;
import org.opennms.client.LoggingEvent.LogLevel;
import org.opennms.install.Installer;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.config.users.Userinfo;
import org.opennms.netmgt.dao.db.InstallerDb;
import org.opennms.netmgt.dao.db.SimpleDataSource;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class InstallServiceImpl extends RemoteServiceServlet implements InstallService {
    private static final String OWNERSHIP_FILE_SESSION_ATTRIBUTE = "__install_ownership_file";
    private static final String DATABASE_SETTINGS_SESSION_ATTRIBUTE = "__install_database_settings";

    private static boolean m_updateIsInProgress = false;
    private static boolean m_lastUpdateSucceeded = false;

    /**
     * TODO: Figure out an API call to make that will fetch the OpenNMS install
     * path possibly by sniffing the current context directory. We probably
     * shouldn't prompt the user for the information since it could possibly
     * lead to privilege escalation (maybe?). Maybe we can set a context
     * attribute in Jetty when we run the webapp that contains the directory
     * name? That's probably the most secure thing to do. That way, the person
     * who starts the webapp has control over the value.
     */
    protected String getOpennmsInstallPath() {
        return "/opt/opennms";
    }

    public boolean checkOwnershipFileExists() {
        String attribute = (String)this.getThreadLocalRequest().getSession(true).getAttribute(OWNERSHIP_FILE_SESSION_ATTRIBUTE);
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
        HttpSession session = this.getThreadLocalRequest().getSession(true);
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
            throw new IllegalStateException("<code>users.xml<code> file cannot be read properly.");
        } catch (ValidationException e) {
            throw new IllegalStateException("<code>users.xml<code> file is invalid and cannot be read.");
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("<code>users.xml<code> file cannot be found in the OpenNMS home directory.");
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
            throw new IllegalArgumentException("Could not store password: " + e.getMessage());
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

    public boolean connectToDatabase(String dbName, String user, String password, String driver, String url, String binaryDirectory) throws IllegalStateException {
        InstallerDb db = new InstallerDb();
        db.setDatabaseName(dbName);
        db.setPostgresOpennmsUser(user);
        db.setPostgresOpennmsPassword(password);
        try {
            db.setDataSource(new SimpleDataSource(driver, url, user, password));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("PostgreSQL driver could not be loaded.", e);
        }

        // TODO: Change this to an appropriate connection test
        // Try to vacuum the database to test connectivity
        try {
            db.vacuumDatabase(false);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * TODO: Figure out if we always want to persist database connection settings directly
     * to the opennms-datasources.xml file or if we should wait until the end to write to
     * the configuration files.
     */
    protected void setDatabaseConfig(String dbName, String user, String password, String driver, String url, String binaryDirectory){
        HttpSession session = this.getThreadLocalRequest().getSession(true);
        session.setAttribute(DATABASE_SETTINGS_SESSION_ATTRIBUTE, new String[] {
            dbName,
            user,
            password,
            driver,
            url,
            binaryDirectory
        });
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
        InstallerDb db = new InstallerDb();
        HttpSession session = this.getThreadLocalRequest().getSession(true);
        String[] dbSettings = (String[])session.getAttribute(DATABASE_SETTINGS_SESSION_ATTRIBUTE);
        if (dbSettings == null || dbSettings.length != 6) {
            throw new IllegalStateException("Database settings have not been specified yet.");
        }

        // TODO: Replace indices with constants
        db.setDatabaseName(dbSettings[0]);
        db.setPostgresOpennmsUser(dbSettings[1]);
        db.setPostgresOpennmsPassword(dbSettings[2]);
        try {
            db.setDataSource(new SimpleDataSource(dbSettings[3], dbSettings[4], dbSettings[1], dbSettings[2]));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("PostgreSQL driver could not be loaded.", e);
        }

        // TODO: Are there additional tests that we need to perform on the database?
        return db.isIpLikeUsable();
    }
}
