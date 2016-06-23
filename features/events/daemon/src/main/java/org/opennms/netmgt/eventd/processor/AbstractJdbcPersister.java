/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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

import javax.sql.DataSource;

import org.opennms.netmgt.dao.api.EventdServiceManager;
import org.opennms.netmgt.eventd.EventUtil;
import org.opennms.netmgt.eventd.EventdConstants;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @see org.opennms.netmgt.events.api.EventDatabaseConstants#MULTIPLE_VAL_DELIM
 * @see org.opennms.netmgt.events.api.EventDatabaseConstants#DB_ATTRIB_DELIM
 * @see org.opennms.netmgt.events.api.EventDatabaseConstants#NAME_VAL_DELIM
 * @see org.opennms.netmgt.events.api.EventDatabaseConstants#MULTIPLE_VAL_DELIM
 * @see org.opennms.netmgt.events.api.EventDatabaseConstants#DB_ATTRIB_DELIM
 * @see org.opennms.netmgt.events.api.EventDatabaseConstants#NAME_VAL_DELIM
 * @see org.opennms.netmgt.events.api.EventDatabaseConstants#MULTIPLE_VAL_DELIM
 * @see org.opennms.netmgt.events.api.EventDatabaseConstants#DB_ATTRIB_DELIM
 * @see org.opennms.netmgt.events.api.EventDatabaseConstants#NAME_VAL_DELIM
 * @author <A HREF="mailto:david@opennms.org">David Hustace </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 *
 * Changes:
 *
 * - Alarm persisting added (many moons ago)
 * - Alarm persisting now removes oldest events by default.  Use "auto-clean" attribute
 *   in eventconf files.
 */
public abstract class AbstractJdbcPersister implements InitializingBean, EventWriter {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractJdbcPersister.class);

    private EventdServiceManager m_eventdServiceManager;

    private EventUtil m_eventUtil;

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
    protected static void set(PreparedStatement stmt, int ndx, String value) throws SQLException {
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
    protected static void set(PreparedStatement stmt, int ndx, int value) throws SQLException {
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
    protected static void set(PreparedStatement stmt, int ndx, Timestamp value) throws SQLException {
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
    protected static void set(PreparedStatement stmt, int ndx, char value) throws SQLException {
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
    protected static Timestamp getEventTime(Event event) {
        return new Timestamp(event.getTime().getTime());
    }
    
    /**
     * <p>getNextId</p>
     *
     * @return a int.
     * @throws java.sql.SQLException if any.
     */
    protected int getNextId() throws SQLException {
        return new JdbcTemplate(getDataSource()).queryForObject(getGetNextIdString(), Integer.class);
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.sql.SQLException if any.
     */
    @Override
    public void afterPropertiesSet() throws SQLException {
        Assert.state(m_eventdServiceManager != null, "property eventdServiceManager must be set");
        Assert.state(m_eventUtil != null, "property eventUtil must be set");
        Assert.state(m_dataSource != null, "property dataSource must be set");
        Assert.state(m_getNextIdString != null, "property getNextIdString must be set");
    }

    /**
     * <p>getEventdServiceManager</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.EventdServiceManager} object.
     */
    public EventdServiceManager getEventdServiceManager() {
        return m_eventdServiceManager;
    }

    /**
     * <p>setEventdServiceManager</p>
     *
     * @param eventdServiceManager a {@link org.opennms.netmgt.dao.api.EventdServiceManager} object.
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

    public EventUtil getEventUtil() {
        return m_eventUtil;
    }

    public void setEventUtil(EventUtil eventUtil) {
        m_eventUtil = eventUtil;
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
    protected static boolean checkEventSanityAndDoWeProcess(Event event, String logPrefix) {
        Assert.notNull(event, "event argument must not be null");

        /*
         * Check value of <logmsg> attribute 'dest', if set to
         * "donotpersist" or "suppress" then simply return, the UEI is not to be
         * persisted to the database
         */
        Assert.notNull(event.getLogmsg(), "event does not have a logmsg");
        if (
            "donotpersist".equals(event.getLogmsg().getDest()) || 
            "suppress".equals(event.getLogmsg().getDest())
        ) {
            LOG.debug("{}: uei '{}' marked as '{}'; not processing event.", logPrefix, event.getUei(), event.getLogmsg().getDest());
            return false;
        }
        return true;
    }
}
