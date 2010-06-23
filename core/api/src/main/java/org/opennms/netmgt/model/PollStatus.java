//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005-2007 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Apr 05: Add serialVersionUID, mark fields private, suppress warnings from unused method. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;


/**
 * Represents the status of a node, interface or services
 * @author brozow
 */
@Embeddable
public class PollStatus implements Serializable {
    private static final long serialVersionUID = 3L;

    private Date m_timestamp = new Date();

    /**
     * Status of the pollable object.
     */
    private int m_statusCode;
    
    private String m_reason;

    private Map<String, Number> m_properties = new LinkedHashMap<String, Number>();
    
    /**
     * <P>
     * The constant that defines a service that is up but is most likely
     * suffering due to excessive load or latency issues and because of that has
     * not responded within the configured timeout period.
     * </P>
     */
    public static final int SERVICE_UNRESPONSIVE = 3;

    /**
     * <P>
     * The constant that defines a service that is not working normally and
     * should be scheduled using the downtime models.
     * </P>
     */
    public static final int SERVICE_UNAVAILABLE = 2;

    /**
     * <P>
     * The constant that defines a service as being in a normal state. If this
     * is returned by the poll() method then the framework will re-schedule the
     * service for its next poll using the standard uptime interval
     * </P>
     */
    public static final int SERVICE_AVAILABLE = 1;

    /**
     * The constant the defines a status is unknown. Used mostly internally
     */
    public static final int SERVICE_UNKNOWN = 0;

    private static final String[] s_statusNames = {
        "Unknown",
        "Up",
        "Down",
        "Unresponsive"
    };

    private static int decodeStatusName(final String statusName) {
        for (int statusCode = 0; statusCode < s_statusNames.length; statusCode++) {
            if (s_statusNames[statusCode].equalsIgnoreCase(statusName)) {
                return statusCode;
            }
        }
        return SERVICE_UNKNOWN;
    }

    public static PollStatus decode(final String statusName) {
        return decode(statusName, null, null);
    }

    public static PollStatus decode(final String statusName, final String reason) {
        return decode(statusName, reason, null);
    }

    public static PollStatus decode(final String statusName, final Double responseTime) {
        return decode(statusName, null, responseTime);
    }

    public static PollStatus decode(final String statusName, final String reason, final Double responseTime) {
        return new PollStatus(decodeStatusName(statusName), reason, responseTime);
    }

    public static PollStatus get(final int status, final String reason) {
        return get(status, reason, null);
    }

    public static PollStatus get(final int status, final Double responseTime) {
        return get(status, null, responseTime);
    }

    public static PollStatus get(final int status, final String reason, final Double responseTime) {
        return new PollStatus(status, reason, responseTime);
    }

    private PollStatus() {
        this(SERVICE_UNKNOWN, null, null);
    }

    private PollStatus(final int statusCode, final String reason, final Double responseTime) {
        setStatusCode(statusCode);
        setReason(reason);
        setResponseTime(responseTime);
    }

    public static PollStatus up() {
        return up(null);
    }

    public static PollStatus up(final Double responseTime) {
        return available(responseTime);
    }

    public static PollStatus available() {
        return available(null);
    }

    public static PollStatus available(final Double responseTime) {
        return new PollStatus(SERVICE_AVAILABLE, null, responseTime);
    }

    public static PollStatus unknown() {
        return unknown(null);
    }

    public static PollStatus unknown(final String reason) {
        return new PollStatus(SERVICE_UNKNOWN, reason, null);
    }

    public static PollStatus unresponsive() {
        return unresponsive(null);
    }

    public static PollStatus unresponsive(final String reason) {
        return new PollStatus(SERVICE_UNRESPONSIVE, reason, null);
    }

    public static PollStatus down() {
        return down(null);
    }

    public static PollStatus unavailable() {
        return unavailable(null);
    }

    public static PollStatus down(final String reason) {
        return unavailable(reason);
    }

    public static PollStatus unavailable(final String reason) {
        return new PollStatus(SERVICE_UNAVAILABLE, reason, null);
    }

    public boolean equals(final Object o) {
        if (o instanceof PollStatus) {
            return m_statusCode == ((PollStatus)o).m_statusCode;
        }
        return false;
    }

    public int hashCode() {
        return m_statusCode;
    }

    @Transient
    public boolean isUp() {
        return !isDown();
    }

    @Transient
    public boolean isAvailable() {
        return this.m_statusCode == SERVICE_AVAILABLE;
    }

    @Transient
    public boolean isUnresponsive() {
        return this.m_statusCode == SERVICE_UNRESPONSIVE;
    }

    @Transient
    public boolean isUnavailable() {
        return this.m_statusCode == SERVICE_UNAVAILABLE;
    }

    @Transient
    public boolean isDown() {
        return this.m_statusCode == SERVICE_UNAVAILABLE;
    }

    @Transient
    public boolean isUnknown() {
        return this.m_statusCode == SERVICE_UNKNOWN;
    }

    public String toString() {
        return getStatusName();
    }

    @Column(name="statusTime", nullable=false)
    public Date getTimestamp() {
        return m_timestamp;
    }

    public void setTimestamp(final Date timestamp) {
        m_timestamp = timestamp;
    }

    @Column(name="statusReason", length=255, nullable=true)
    public String getReason() {
        return m_reason;
    }

    public void setReason(final String reason) {
        m_reason = reason.substring(0, 255);
    }

    @Column(name="responseTime", nullable=true)
    public Double getResponseTime() {
        Number val = getProperty("response-time");
        return (val == null ? null : val.doubleValue());
    	
    }

    /* stores the individual item for compatibility with database schema, as well as the new property map */
    public void setResponseTime(final Double responseTime) {
        if (responseTime == null) {
            m_properties.remove("response-time");
        } else {
            m_properties.put("response-time", responseTime);
        }
    }

    @Transient
    public Map<String, Number> getProperties() {
    	if (m_properties == null) {
    		m_properties = new LinkedHashMap<String, Number>();
    	}
    	return m_properties;
    }
    
    public void setProperties(Map<String, Number> p) {
    	m_properties = p;
    }
    
    @Transient
    public Number getProperty(final String key) {
    	if (m_properties != null) {
    		return m_properties.get(key);
    	} else {
    		return null;
    	}
    }

    public void setProperty(final String key, final Number value) {
    	Map<String, Number> m = getProperties();
    	m.put(key, value);
    	setProperties(m);
    }

    @Column(name="statusCode", nullable=false)
    public int getStatusCode() {
        return m_statusCode;
    }

    private void setStatusCode(final int statusCode) {
        m_statusCode = statusCode;
    }

    @Transient
    public String getStatusName() {
        return s_statusNames[m_statusCode];
    }


}
