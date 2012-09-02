/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd.processor;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;

import javax.sql.DataSource;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.eventd.EventdConstants;
import org.opennms.netmgt.eventd.EventdServiceManager;
import org.opennms.netmgt.model.events.EventProcessor;
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
 * @see org.opennms.netmgt.model.events.Constants#MULTIPLE_VAL_DELIM
 * @see org.opennms.netmgt.model.events.Constants#DB_ATTRIB_DELIM
 * @see org.opennms.netmgt.model.events.Constants#NAME_VAL_DELIM
 * @see org.opennms.netmgt.model.events.Constants#MULTIPLE_VAL_DELIM
 * @see org.opennms.netmgt.model.events.Constants#DB_ATTRIB_DELIM
 * @see org.opennms.netmgt.model.events.Constants#NAME_VAL_DELIM
 * @author <A HREF="mailto:david@opennms.org">David Hustace </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 *
 * Changes:
 *
 * - Alarm persisting added (many moons ago)
 * - Alarm persisting now removes oldest events by default.  Use "auto-clean" attribute
 *   in eventconf files.
 * @author Sowmya Nataraj </A>
 * @author <A HREF="mailto:david@opennms.org">David Hustace </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 *
 * Changes:
 *
 * - Alarm persisting added (many moons ago)
 * - Alarm persisting now removes oldest events by default.  Use "auto-clean" attribute
 *   in eventconf files.
 * @version $Id: $
 */
public abstract class AbstractJdbcPersister implements InitializingBean, EventProcessor {
    // Field sizes in the events table
    /** Constant <code>EVENT_UEI_FIELD_SIZE=256</code> */
    protected static final int EVENT_UEI_FIELD_SIZE = 256;

    /** Constant <code>EVENT_HOST_FIELD_SIZE=256</code> */
    protected static final int EVENT_HOST_FIELD_SIZE = 256;

    /** 
     * Constant <code>EVENT_INTERFACE_FIELD_SIZE=50</code>.
     * This value must be long enough to accommodate an IPv6 address
     * with scope identifier suffix (if present). Basic IPv6 addresses
     * are 39 characters so this will accommodate a 10-digit scope
     * identifier (any 32-bit decimal value). 
     */
    protected static final int EVENT_INTERFACE_FIELD_SIZE = 50;

    /** Constant <code>EVENT_DPNAME_FIELD_SIZE=12</code> */
    protected static final int EVENT_DPNAME_FIELD_SIZE = 12;

    /** Constant <code>EVENT_SNMPHOST_FIELD_SIZE=256</code> */
    protected static final int EVENT_SNMPHOST_FIELD_SIZE = 256;

    /** Constant <code>EVENT_SNMP_FIELD_SIZE=256</code> */
    protected static final int EVENT_SNMP_FIELD_SIZE = 256;

    /** Constant <code>EVENT_LOGGRP_FIELD_SIZE=32</code> */
    protected static final int EVENT_LOGGRP_FIELD_SIZE = 32;

    /** Constant <code>EVENT_PATHOUTAGE_FIELD_SIZE=1024</code> */
    protected static final int EVENT_PATHOUTAGE_FIELD_SIZE = 1024;

    /** Constant <code>EVENT_CORRELATION_FIELD_SIZE=1024</code> */
    protected static final int EVENT_CORRELATION_FIELD_SIZE = 1024;

    /** Constant <code>EVENT_OPERINSTRUCT_FIELD_SIZE=1024</code> */
    protected static final int EVENT_OPERINSTRUCT_FIELD_SIZE = 1024;

    /** Constant <code>EVENT_AUTOACTION_FIELD_SIZE=256</code> */
    protected static final int EVENT_AUTOACTION_FIELD_SIZE = 256;

    /** Constant <code>EVENT_OPERACTION_FIELD_SIZE=256</code> */
    protected static final int EVENT_OPERACTION_FIELD_SIZE = 256;

    /** Constant <code>EVENT_OPERACTION_MENU_FIELD_SIZE=64</code> */
    protected static final int EVENT_OPERACTION_MENU_FIELD_SIZE = 64;

//    protected static final int EVENT_NOTIFICATION_FIELD_SIZE = 128;

    /** Constant <code>EVENT_TTICKET_FIELD_SIZE=128</code> */
    protected static final int EVENT_TTICKET_FIELD_SIZE = 128;

    /** Constant <code>EVENT_FORWARD_FIELD_SIZE=256</code> */
    protected static final int EVENT_FORWARD_FIELD_SIZE = 256;

    /** Constant <code>EVENT_MOUSEOVERTEXT_FIELD_SIZE=64</code> */
    protected static final int EVENT_MOUSEOVERTEXT_FIELD_SIZE = 64;

    /** Constant <code>EVENT_ACKUSER_FIELD_SIZE=256</code> */
    protected static final int EVENT_ACKUSER_FIELD_SIZE = 256;

    /** Constant <code>EVENT_SOURCE_FIELD_SIZE=128</code> */
    protected static final int EVENT_SOURCE_FIELD_SIZE = 128;
    
    /** Constant <code>EVENT_X733_ALARMTYPE_SIZE=31</code> */
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

    /**
     * <p>Constructor for AbstractJdbcPersister.</p>
     */
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
     * @exception java.sql.SQLException
     *                Thrown if there is an error adding the value to the
     *                statement.
     * @throws java.sql.SQLException if any.
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
     * @exception java.sql.SQLException
     *                Thrown if there is an error adding the value to the
     *                statement.
     * @throws java.sql.SQLException if any.
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
     * @exception java.sql.SQLException
     *                Thrown if there is an error adding the value to the
     *                statement.
     * @throws java.sql.SQLException if any.
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
     * @exception java.sql.SQLException
     *                Thrown if there is an error adding the value to the
     *                statement.
     * @throws java.sql.SQLException if any.
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
     * @return The integer identifier for the service name.
     * @exception java.sql.SQLException
     *                Thrown if there is an error accessing the stored data or
     *                the SQL text is malformed. This will also be thrown if the
     *                result cannot be obtained.
     * @see EventdConstants#SQL_DB_SVCNAME_TO_SVCID
     * @throws java.sql.SQLException if any.
     */
    protected int getServiceID(String name) throws SQLException {
        return m_eventdServiceManager.getServiceId(name);
    }

    /**
     * <p>getEventTime</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return a {@link java.sql.Timestamp} object.
     */
    protected Timestamp getEventTime(Event event) {
        try {
            return new Timestamp(EventConstants.parseToDate(event.getTime()).getTime());
        } catch (ParseException e) {
            log().warn("Failed to convert time " + event.getTime() + " to Timestamp, setting current time instead.  Exception: " + e, e);
            return new Timestamp(System.currentTimeMillis());
        }
    }
    
    /**
     * <p>getNextId</p>
     *
     * @return a int.
     * @throws java.sql.SQLException if any.
     */
    protected int getNextId() throws SQLException {
        return new JdbcTemplate(getDataSource()).queryForInt(getGetNextIdString());
    }

    /**
     * <p>log</p>
     *
     * @return a {@link org.opennms.core.utils.ThreadCategory} object.
     */
    protected ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.sql.SQLException if any.
     */
    @Override
    public void afterPropertiesSet() throws SQLException {
        Assert.state(m_eventdServiceManager != null, "property eventdServiceManager must be set");
        Assert.state(m_dataSource != null, "property dataSource must be set");
        Assert.state(m_getNextIdString != null, "property getNextIdString must be set");
    }

    /**
     * <p>getEventdServiceManager</p>
     *
     * @return a {@link org.opennms.netmgt.eventd.EventdServiceManager} object.
     */
    public EventdServiceManager getEventdServiceManager() {
        return m_eventdServiceManager;
    }

    /**
     * <p>setEventdServiceManager</p>
     *
     * @param eventdServiceManager a {@link org.opennms.netmgt.eventd.EventdServiceManager} object.
     */
    public void setEventdServiceManager(EventdServiceManager eventdServiceManager) {
        m_eventdServiceManager = eventdServiceManager;
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
     * <p>getGetNextIdString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getGetNextIdString() {
        return m_getNextIdString;
    }

    /**
     * <p>setGetNextIdString</p>
     *
     * @param getNextIdString a {@link java.lang.String} object.
     */
    public void setGetNextIdString(String getNextIdString) {
        m_getNextIdString = getNextIdString;
    }

    /**
     * <p>checkEventSanityAndDoWeProcess</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @param logPrefix a {@link java.lang.String} object.
     * @return a boolean.
     */
    protected boolean checkEventSanityAndDoWeProcess(Event event, String logPrefix) {
        Assert.notNull(event, "event argument must not be null");
    
        /*
         * Check value of <logmsg> attribute 'dest', if set to
         * "donotpersist" then simply return, the uei is not to be
         * persisted to the database
         */
        Assert.notNull(event.getLogmsg(), "event does not have a logmsg");
        if ("donotpersist".equals(event.getLogmsg().getDest()) || "suppress".equals(event.getLogmsg().getDest())) {
            if (log().isDebugEnabled()) {
                log().debug(logPrefix + ": uei '" + event.getUei() + "' marked as '" + event.getLogmsg().getDest() + "'; not processing event.");
            }
            return false;
        }
        return true;
    }
}
