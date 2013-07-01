/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.archive;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.logging.Logging;
import org.opennms.core.utils.TimeConverter;
import org.opennms.netmgt.config.EventsArchiverConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * <pre>
 *
 *  The EventsArchiver is responsible for archiving and removing events
 *  from the 'events' table.
 *
 *  The archival/deletion depends on the 'eventLog' and the 'eventDisplay'
 *  values for the event -
 *
 *  If the 'eventLog == N and the eventDisplay == N',
 *  the event is simply deleted from the events table
 *
 *  If the 'eventLog == N and the eventDisplay == Y',
 *  the event is deleted ONLY if the event has been acknowledged
 *
 *  If the 'eventLog == Y and the eventDisplay == N',
 *  the event is sent to the archive file and deleted from the table
 *
 *  If the 'eventLog == Y and the eventDisplay == Y',
 *  the event is sent to the archive file and deleted from the table
 *  ONLY if the event has been acknowledged
 *
 *  An event is considered acknowledged if the 'eventAckUser' column has
 *  a non-null value
 *
 *  An EventsArchiver run depends on attributes in the events archiver
 *  configuration file. The following are the properties that govern a run -
 *
 *  - archiveAge
 *    This determines which events are to be removed - i.e events older
 *    than current time minus this time are removed
 *
 *  - separator
 *    This is the separator used in between event table column values when an
 *    event is written to the archive file
 *
 *  The EventsArchiver uses Apache's log4j both for its output logs and for
 *  the actual archiving itself - the set up for the log4j appenders for
 *  this archiver are all doneexclusively in the 'events.archiver.properties'
 *  property file
 *

 *  @author &lt;A HREF=&quot;mailto:sowmya@opennms.org&quot;&gt;Sowmya Nataraj&lt;/A&gt;
 *  @author &lt;A HREF=&quot;http://www.opennms.org&quot;&gt;OpenNMS&lt;/A&gt;
 *  @author &lt;A HREF=&quot;mailto:sowmya@opennms.org&quot;&gt;Sowmya Nataraj&lt;/A&gt;
 *  @author &lt;A HREF=&quot;http://www.opennms.org&quot;&gt;OpenNMS&lt;/A&gt;
 * @version $Id: $
 */
public class EventsArchiver {
    
    private static final Logger LOG = LoggerFactory.getLogger(EventsArchiver.class);
    
    /**
     * The SQL statement to select events that have their eventCreateTime
     * earlier than a specified age
     */
    private static final String DB_SELECT_EVENTS_TO_ARCHIVE =
        "SELECT * " +
        "FROM events " +
        "WHERE (eventcreatetime < ?)";

    /**
     * The SQL statement to delete events based on their eventID
     */
    private static final String DB_DELETE_EVENT =
        "DELETE " +
        "FROM events " +
        "WHERE (eventID = ?)";

    /**
     * The column name for eventID in the events table
     */
    private static final String EVENT_ID = "eventID";

    /**
     * The column name for 'eventLog' in the events table
     */
    private static final String EVENT_LOG = "eventLog";

    /**
     * The column name for 'eventDisplay' in the events table
     */
    private static final String EVENT_DISPLAY = "eventDisplay";

    /**
     * The column name for 'eventAckUser' in the events table
     */
    private static final String EVENT_ACK_USER = "eventAckUser";

    /**
     * The value for the event log or display field if set to true
     */
    private static final String MSG_YES = "Y";

    /**
     * The value for the event log or display field if set to false
     */
    private static final String MSG_NO = "N";


    /**
     * The archive age in milliseconds. Events created before this are
     * archived/deleted.
     */
    private long m_archAge;

    /**
     * The separator to be used when writing events into the archive
     */
    private String m_archSeparator;

    /**
     * The database connection
     */
    private Connection m_conn;

    /**
     * The prepared statement to select the events
     */
    private PreparedStatement m_eventsGetStmt;

    /**
     * The prepared statement to delete the events
     */
    private PreparedStatement m_eventDeleteStmt;

    /**
     * Read the required properties and set up the logs, the archive etc.
     * @throws ArchiverException Thrown if a required property is not specified or is incorrect
     */
    private void init() throws ArchiverException {
        Map mdc = Logging.getCopyOfContextMap();

        try {
        
            // The general logs from the events archiver go to this category'
            Logging.putPrefix("archiver");


        EventsArchiverConfigFactory eaFactory;
        
        try {
            EventsArchiverConfigFactory.init();
             eaFactory =
                EventsArchiverConfigFactory.getInstance();
        } catch (MarshalException ex) {
            LOG.error("MarshalException", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (ValidationException ex) {
            LOG.error("ValidationException", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (IOException ex) {
            LOG.error("IOException", ex);
            throw new UndeclaredThrowableException(ex);
        }

        // get archive age
        String archAgeStr = eaFactory.getArchiveAge();
        long archAge;
        try {
            archAge = TimeConverter.convertToMillis(archAgeStr);
        } catch (NumberFormatException nfe) {
            throw new ArchiverException("Archive age: " + archAgeStr
                                        + "- Incorrect format "
                                        + nfe.getMessage());
        }

        /*
         * Set actual time that is to be used for the select from the
         * database.
         */
        m_archAge = System.currentTimeMillis() - archAge;

        // get the separator to be used between column names in the archive
        String separator = eaFactory.getSeparator();
        if (separator == null) {
            m_archSeparator = "#";
        } else {
            m_archSeparator = separator;
        }

        // info logs
        if (LOG.isInfoEnabled()) {
            // get this in readable format
            archAgeStr = new java.util.Date(m_archAge).toString();
            LOG.info("Events archive age specified = {}", archAgeStr);
            LOG.info("Events archive age in millisconds = {}", archAge);

            LOG.info("Events created before \'" + archAgeStr + " \' will be deleted");

            LOG.info("Separator to be used in archive: {}", m_archSeparator);
        }

        // Make sure we can connect to the database
        try {
            DataSourceFactory.init();
            m_conn = DataSourceFactory.getInstance().getConnection();
        } catch (IOException e) {
            LOG.error("IOException while initializing database", e);
            throw new UndeclaredThrowableException(e);
        } catch (MarshalException e) {
            LOG.error("MarshalException while initializing database", e);
            throw new UndeclaredThrowableException(e);
        } catch (ValidationException e) {
            LOG.error("ValidationException while initializing database", e);
            throw new UndeclaredThrowableException(e);
        } catch (PropertyVetoException e) {
            LOG.error("PropertyVetoException while initializing database", e);
            throw new UndeclaredThrowableException(e);
        } catch (SQLException e) {
            LOG.error("SQLException while initializing database", e);
            throw new UndeclaredThrowableException(e);
        } catch (ClassNotFoundException e) {
            LOG.error("ClassNotFoundException while initializing database", e);
            throw new UndeclaredThrowableException(e);
        }
        // XXX should we be throwing ArchiverException instead?
        } finally {
            Logging.setContextMap(mdc);
        }
    }

    /**
     * Remove event with eventID from events table. NOTE: Postgres does not have
     * the ResultSet.deleteRow() implemented! - so use the eventID to delete!
     */
    private boolean removeEvent(String eventID) {
        try {
            m_eventDeleteStmt.setString(1, eventID);
            m_eventDeleteStmt.executeUpdate();
        } catch (SQLException sqle) {
            LOG.error("Unable to delete event \'" + eventID + "\': " + sqle.getMessage());
            return false;
        }

        // debug logs
        LOG.debug("EventID: " + eventID + " removed from events table");

        return true;
    }

    /**
     * Select the events created before 'age', log them to the archive file if
     * required and delete these events.
     * 
     * NOTE: Postgres does not have the ResultSet.deleteRow() implemented! - so
     * use the eventID to delete!
     */
    private void archiveEvents() {
        // number of events sent to the archive file
        int archCount = 0;

        // number of events deleted from the events table
        int remCount = 0;

        ResultSet eventsRS = null;
        try {
            m_eventsGetStmt.setTimestamp(1, new Timestamp(m_archAge));
            eventsRS = m_eventsGetStmt.executeQuery();
            int colCount = eventsRS.getMetaData().getColumnCount();
            String eventID;
            String eventUEI;
            String eventLog;
            String eventDisplay;
            String eventAckUser;

            boolean ret;

            while (eventsRS.next()) {
                // get the eventID for the event
                eventID = eventsRS.getString(EVENT_ID);

                // get uei for event
                eventUEI = eventsRS.getString("eventUei");

                // get eventLog for this row
                eventLog = eventsRS.getString(EVENT_LOG);

                // get eventDisplay for this row
                eventDisplay = eventsRS.getString(EVENT_DISPLAY);

                // eventAckUser for this event
                eventAckUser = eventsRS.getString(EVENT_ACK_USER);

                LOG.debug("Event id: " + eventID + " uei: " + eventUEI + " log: " + eventLog + " display: " + eventDisplay + " eventAck: " + eventAckUser);

                if (eventLog.equals(MSG_NO) && eventDisplay.equals(MSG_NO)) {
                    // log = N, display = N, delete event
                    ret = removeEvent(eventID);
                    if (ret) {
                        remCount++;
                    }
                } else if (eventLog.equals(MSG_YES)
                           && eventDisplay.equals(MSG_NO)) {
                    // log = Y, display = N, archive event, then delete
                    ret = removeEvent(eventID);
                    if (ret) {
                        sendToArchive(eventsRS, colCount);
                        LOG.debug("eventID " + eventID + " archived");

                        archCount++;

                        remCount++;
                    }
                } else if (eventLog.equals(MSG_NO)
                           && eventDisplay.equals(MSG_YES)) {
                    /*
                     * log = N, display = Y, delete event only if event has been
                     * acknowledged.
                     */
                    if (eventAckUser != null) {
                        ret = removeEvent(eventID);
                        if (ret) {
                            remCount++;
                        }
                    }
                } else {
                    /*
                     * log = Y, display = Y, log and delete event only if event
                     * has been acknowledged.
                     */
                    if (eventAckUser != null) {
                        ret = removeEvent(eventID);
                        if (ret) {
                            sendToArchive(eventsRS, colCount);
                            LOG.debug("eventID " + eventID + " archived");
                            archCount++;

                            remCount++;
                        }
                    }
                }

            }

            LOG.info("Number of events removed from the event table: {}", remCount);
            LOG.info("Number of events sent to the archive: {}", archCount);
        } catch (Throwable oe) {
            LOG.error("EventsArchiver: Error reading events for archival: ");
            LOG.error(oe.getMessage());
        } finally {
            try {
                eventsRS.close();
            } catch (Throwable e) {
                LOG.info("EventsArchiver: Exception while events result " + "set: message -> " + e.getMessage());
            }
        }

    }

    /**
     * Archive the current row of the result set
     * 
     * @exception SQLException
     *                thrown if there is an error getting column values from the
     *                result set
     */
    private void sendToArchive(ResultSet eventsRS, int colCount)
            throws SQLException {
        StringBuffer outBuf = new StringBuffer();

        for (int index = 1; index <= colCount; index++) {
            String colValue = eventsRS.getString(index);
            if (index == 1) {
                outBuf.append(colValue);
            } else {
                outBuf.append(m_archSeparator + colValue);
            }
        }

        String outBufStr = outBuf.toString();
        LOG.error(outBufStr);
    }

    /**
     * Close the database statements and the connection and close log4j
     * Appenders and categories
     */
    private void close() {
        try {
            m_eventsGetStmt.close();
        } catch (SQLException e) {
            LOG.warn("Unable to close get statement", e);
        }
        
        try {
            m_eventDeleteStmt.close();
        } catch (SQLException e) {
            LOG.warn("Unable to close delete statement", e);
        }
        
        try {
            m_conn.close();
        } catch (SQLException e) {
            LOG.warn("Unable to close connection", e);
        }

    }

    /**
     * The events archiver constructor - reads required properties, initializes
     * the database connection and the prepared statements to select and delete
     * events
     *
     * @throws org.opennms.netmgt.archive.ArchiverException if any.
     */
    public EventsArchiver() throws ArchiverException {
        // call init
        init();
        
        // initialize the prepared statements
        try {
            m_eventsGetStmt =
                m_conn.prepareStatement(DB_SELECT_EVENTS_TO_ARCHIVE);
            m_eventDeleteStmt = m_conn.prepareStatement(DB_DELETE_EVENT);
        } catch (SQLException e) {
            LOG.error("EventsArchiver: Exception in opening the database " + "connection or in the prepared statement for the " + "get events");
            LOG.error(e.getMessage());
            throw new ArchiverException("EventsArchiver: " + e.getMessage());
        }
    }

    /**
     * <p>main</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(String[] args) {
        try {
            // create the archiver
            EventsArchiver ea = new EventsArchiver();

            /*
             * Remove events.  This method sends removed events
             * to archive file if configured for archival.
             */
            ea.archiveEvents();

            // close the archiver
            ea.close();
        } catch (ArchiverException ae) {
            System.err.println(ae.getMessage());
        }
    }
}
