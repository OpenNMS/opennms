package org.opennms.server;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Appender;

import org.opennms.client.*;
import org.opennms.client.LoggingEvent.LogLevel;
import org.opennms.install.Installer;
import org.opennms.netmgt.config.C3P0ConnectionFactory;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.opennms.netmgt.dao.db.InstallerDb;
import org.opennms.netmgt.dao.db.SimpleDataSource;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class InstallServiceImpl extends RemoteServiceServlet implements InstallService {
    private final String OWNERSHIP_FILE_CONTEXT_ATTRIBUTE = "__install_ownership_file";

    public boolean checkOwnershipFileExists() {
        ServletContext context = this.getServletContext();
        String attribute = (String)context.getAttribute(OWNERSHIP_FILE_CONTEXT_ATTRIBUTE);
        if (attribute == null) {
            return false;
        } else {
            // TODO: Figure out an API call to make that will fetch the OpenNMS install path
            if (new File("/opt/opennms", attribute).isFile()) {
                return true;
            } else {
                return false;
            }
        }
    }

    public String getOwnershipFilename(){
        ServletContext context = this.getServletContext();
        String attribute = (String)context.getAttribute(OWNERSHIP_FILE_CONTEXT_ATTRIBUTE);
        if (attribute == null) {
            attribute = "opennms_" + Math.round(Math.random() * (double)100000000) + ".txt";
            context.setAttribute(OWNERSHIP_FILE_CONTEXT_ATTRIBUTE, attribute);
        }
        return attribute;
    }

    public void resetOwnershipFilename() {
        ServletContext context = this.getServletContext();
        String attribute = "opennms_" + Math.round(Math.random() * (double)100000000) + ".txt";
        context.setAttribute(OWNERSHIP_FILE_CONTEXT_ATTRIBUTE, attribute);
    }

    public void setAdminPassword(String password) {

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
        return false;
    }

    public void setDatabaseConfig(String arguments){
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

    /**
     * Initiate the installer class. This will generate log messages that will be
     * relayed to the web UI by the log4j appender.
     */
    public void updateDatabase() {
        Thread thread = new Thread() {
            public void run() {
                try {
                    Installer.main(new String[] { "-dis" });
                } catch (Exception e) {
                    Logger.getLogger(this.getClass()).error("Installation failed: " + e.getMessage(), e);
                }
            }
        };
        thread.start();
    }

    public boolean checkIpLike() {
        Logger.getLogger(this.getClass()).info("InstallServiceImpl.checkIpLike()");
        return false;
    }
}
