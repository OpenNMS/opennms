//This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Jan 27: Implement EventProcessor interface and make this class
//              and its subclasses thread safe. - dj@opennms.org
// 2008 Jan 27: Push methods in Persist that are only used by a single
//              subclass into that subclass.
// 2008 Jan 26: Dependency injection using setter injection instead of
//              constructor injection and implement InitializingBean.
//              Move some common setters and initializion into Persist.
//              Implement log method. - dj@opennms.org
// 2008 Jan 23: Use Java 5 generics, format code, wrap debug logs within an if
//              statement unless they are logging a plain String. - dj@opennms.org
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.eventd.processor;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;

import javax.sql.DataSource;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.eventd.EventdConstants;
import org.opennms.netmgt.eventd.EventdServiceManager;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

/**
 * EventWriter loads the information in each 'Event' into the database.
 * 
 * While loading mutiple values of the same element into a single DB column, the
 * mutiple values are delimited by MULTIPLE_VAL_DELIM.
 * 
 * When an element and its attribute are loaded into a single DB column, the
 * value and the attribute are separated by a DB_ATTRIB_DELIM.
 * 
 * When using delimiters to append values, if the values already have the
 * delimiter, the delimiter in the value is escaped as in URLs.
 * 
 * Values for the ' <parms>' block are loaded with each parm name and parm value
 * delimited with the NAME_VAL_DELIM.
 * 
 * @see org.opennms.netmgt.model.events.Constants#MULTIPLE_VAL_DELIM
 * @see org.opennms.netmgt.model.events.Constants#DB_ATTRIB_DELIM
 * @see org.opennms.netmgt.model.events.Constants#NAME_VAL_DELIM
 * 
 * @author <A HREF="mailto:david@opennms.org">David Hustace </A>
 * @author Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * 
 * Changes:
 * 
 * - Alarm persisting added (many moons ago)
 * - Alarm persisting now removes oldest events by default.  Use "auto-clean" attribute
 *   in eventconf files.
 */
public abstract class AbstractJdbcPersister implements InitializingBean, EventProcessor {
    // Field sizes in the events table
    protected static final int EVENT_UEI_FIELD_SIZE = 256;

    protected static final int EVENT_HOST_FIELD_SIZE = 256;

    protected static final int EVENT_INTERFACE_FIELD_SIZE = 16;

    protected static final int EVENT_DPNAME_FIELD_SIZE = 12;

    protected static final int EVENT_SNMPHOST_FIELD_SIZE = 256;

    protected static final int EVENT_SNMP_FIELD_SIZE = 256;

    protected static final int EVENT_DESCR_FIELD_SIZE = 4000;

    protected static final int EVENT_LOGGRP_FIELD_SIZE = 32;

    protected static final int EVENT_LOGMSG_FIELD_SIZE = 256;

    protected static final int EVENT_PATHOUTAGE_FIELD_SIZE = 1024;

    protected static final int EVENT_CORRELATION_FIELD_SIZE = 1024;

    protected static final int EVENT_OPERINSTRUCT_FIELD_SIZE = 1024;

    protected static final int EVENT_AUTOACTION_FIELD_SIZE = 256;

    protected static final int EVENT_OPERACTION_FIELD_SIZE = 256;

    protected static final int EVENT_OPERACTION_MENU_FIELD_SIZE = 64;

//    protected static final int EVENT_NOTIFICATION_FIELD_SIZE = 128;

    protected static final int EVENT_TTICKET_FIELD_SIZE = 128;

    protected static final int EVENT_FORWARD_FIELD_SIZE = 256;

    protected static final int EVENT_MOUSEOVERTEXT_FIELD_SIZE = 64;

    protected static final int EVENT_ACKUSER_FIELD_SIZE = 256;

    protected static final int EVENT_SOURCE_FIELD_SIZE = 128;
    
    protected static final int EVENT_X733_ALARMTYPE_SIZE = 31;

    /**
     * The character to put in if the log or display is to be set to yes
     */
    protected static final char MSG_YES = 'Y';

    /**
     * The character to put in if the log or display is to be set to no
     */
    protected static final char MSG_NO = 'N';

    private EventdServiceManager m_eventdServiceManager;

    private DataSource m_dataSource;
    
    private String m_getNextIdString;

    public AbstractJdbcPersister() {
    }

    /**
     * Sets the statement up for a String value.
     * 
     * @param stmt
     *            The statement to add the value to.
     * @param ndx
     *            The ndx for the value.
     * @param value
     *            The value to add to the statement.
     * 
     * @exception java.sql.SQLException
     *                Thrown if there is an error adding the value to the
     *                statement.
     */
    protected void set(PreparedStatement stmt, int ndx, String value) throws SQLException {
        if (value == null || value.length() == 0) {
            stmt.setNull(ndx, Types.VARCHAR);
        } else {
            stmt.setString(ndx, value);
        }
    }

    /**
     * Sets the statement up for an integer type. If the integer type is less
     * than zero, then it is set to null!
     * 
     * @param stmt
     *            The statement to add the value to.
     * @param ndx
     *            The ndx for the value.
     * @param value
     *            The value to add to the statement.
     * 
     * @exception java.sql.SQLException
     *                Thrown if there is an error adding the value to the
     *                statement.
     */
    protected void set(PreparedStatement stmt, int ndx, int value) throws SQLException {
        if (value < 0) {
            stmt.setNull(ndx, Types.INTEGER);
        } else {
            stmt.setInt(ndx, value);
        }
    }

    /**
     * Sets the statement up for a timestamp type.
     * 
     * @param stmt
     *            The statement to add the value to.
     * @param ndx
     *            The ndx for the value.
     * @param value
     *            The value to add to the statement.
     * 
     * @exception java.sql.SQLException
     *                Thrown if there is an error adding the value to the
     *                statement.
     */
    protected void set(PreparedStatement stmt, int ndx, Timestamp value) throws SQLException {
        if (value == null) {
            stmt.setNull(ndx, Types.TIMESTAMP);
        } else {
            stmt.setTimestamp(ndx, value);
        }
    }

    /**
     * Sets the statement up for a character value.
     * 
     * @param stmt
     *            The statement to add the value to.
     * @param ndx
     *            The ndx for the value.
     * @param value
     *            The value to add to the statement.
     * 
     * @exception java.sql.SQLException
     *                Thrown if there is an error adding the value to the
     *                statement.
     */
    protected void set(PreparedStatement stmt, int ndx, char value) throws SQLException {
        stmt.setString(ndx, String.valueOf(value));
    }

    /**
     * This method is used to convert the service name into a service id. It
     * first looks up the information from a service map of Eventd and if no
     * match is found, by performing a lookup in the database. If the conversion
     * is successful then the corresponding integer identifier will be returned
     * to the caller.
     * 
     * @param name
     *            The name of the service
     * 
     * @return The integer identifier for the service name.
     * 
     * @exception java.sql.SQLException
     *                Thrown if there is an error accessing the stored data or
     *                the SQL text is malformed. This will also be thrown if the
     *                result cannot be obtained.
     * 
     * @see EventdConstants#SQL_DB_SVCNAME_TO_SVCID
     * 
     */
    protected int getServiceID(String name) throws SQLException {
        return m_eventdServiceManager.getServiceId(name);
    }

    /**
     * @param event
     * @param log
     * @return
     */
    protected Timestamp getEventTime(Event event) {
        try {
            return new Timestamp(EventConstants.parseToDate(event.getTime()).getTime());
        } catch (ParseException e) {
            log().warn("Failed to convert time " + event.getTime() + " to Timestamp, setting current time instead.  Exception: " + e, e);
            return new Timestamp(System.currentTimeMillis());
        }
    }
    
    protected int getNextId() throws SQLException {
        return new JdbcTemplate(getDataSource()).queryForInt(getGetNextIdString());
    }

    protected Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public void afterPropertiesSet() throws SQLException {
        Assert.state(m_eventdServiceManager != null, "property eventdServiceManager must be set");
        Assert.state(m_dataSource != null, "property dataSource must be set");
        Assert.state(m_getNextIdString != null, "property getNextIdString must be set");
    }

    public EventdServiceManager getEventdServiceManager() {
        return m_eventdServiceManager;
    }

    public void setEventdServiceManager(EventdServiceManager eventdServiceManager) {
        m_eventdServiceManager = eventdServiceManager;
    }

    public DataSource getDataSource() {
        return m_dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
    }

    public String getGetNextIdString() {
        return m_getNextIdString;
    }

    public void setGetNextIdString(String getNextIdString) {
        m_getNextIdString = getNextIdString;
    }

    protected boolean checkEventSanityAndDoWeProcess(Event event, String logPrefix) {
        Assert.notNull(event, "event argument must not be null");
    
        /*
         * Check value of <logmsg> attribute 'dest', if set to
         * "donotpersist" then simply return, the uei is not to be
         * persisted to the database
         */
        Assert.notNull(event.getLogmsg(), "event does not have a logmsg");
        if ("donotpersist".equals(event.getLogmsg().getDest())) {
            if (log().isDebugEnabled()) {
                log().debug(logPrefix + ": uei '" + event.getUei() + "' marked as 'donotpersist'; not processing event.");
            }
            return false;
        }
        return true;
    }
}
