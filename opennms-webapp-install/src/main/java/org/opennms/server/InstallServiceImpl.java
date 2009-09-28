package org.opennms.server;

import java.util.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Appender;

import org.opennms.client.*;
import org.opennms.client.LoggingEvent.LogLevel;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class InstallServiceImpl extends RemoteServiceServlet implements InstallService {
    public boolean checkOwnershipFileExists() {
        return false;
    }

    public String getOwnershipFilename(){
        return "fake_file_282829034.txt";
    }

    public void resetOwnershipFilename() {

    }

    public void setAdminPassword(String password) {

    }

    public boolean connectToDatabase() {
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

    public void updateDatabase() {

    }

    public boolean checkIpLike() {
        Logger.getLogger(this.getClass()).info("InstallServiceImpl.checkIpLike()");
        return false;
    }
}
