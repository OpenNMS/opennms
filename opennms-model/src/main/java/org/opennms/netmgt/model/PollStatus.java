//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;


/**
 * Represents the status of a node, interface or services
 * @author brozow
 */
@Embeddable
public class PollStatus implements Serializable {
    private static final long serialVersionUID = 2L;

    private Date m_timestamp = new Date();

    /**
     * Status of the pollable object.
     */
    private int m_statusCode;
    
    private String m_reason;
    
    private long m_responseTime = -1L;
    private long m_nanoResponseTime = -1L;
    private Collection<Long> m_responseTimes = new ArrayList<Long>();
    
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

    private static int decodeStatusName(String statusName) {

        for (int statusCode = 0; statusCode < s_statusNames.length; statusCode++) {
            if (s_statusNames[statusCode].equalsIgnoreCase(statusName)) {
                return statusCode;
            }
        }
        return SERVICE_UNKNOWN;
    }

    public static PollStatus decode(String statusName) {
        return decode(statusName, null, -1L);
    }

    public static PollStatus decode(String statusName, String reason) {
        return decode(statusName, reason, -1L);
    }

    public static PollStatus decode(String statusName, long responseTime) {
        return decode(statusName, null, responseTime);
    }

    public static PollStatus decode(String statusName, String reason, long responseTime) {
        return new PollStatus(decodeStatusName(statusName), reason, responseTime);
    }

    public static PollStatus get(int status, String reason) {
        return get(status, reason, -1L);
    }

    public static PollStatus get(int status, String reason, long responseTime) {
        return new PollStatus(status, reason, responseTime);
    }

    private PollStatus() {
        this(SERVICE_UNKNOWN, null, -1L);
    }

    private PollStatus(int statusCode, String reason, long responseTime) {
        m_statusCode = statusCode;
        m_reason = reason;
        m_responseTime = responseTime;
    }

    public static PollStatus up() {
        return up(-1L);
    }

    public static PollStatus up(long responseTime) {
        return available(responseTime);
    }

    public static PollStatus available() {
        return available(-1L);
    }

    public static PollStatus available(long responseTime) {
        return new PollStatus(SERVICE_AVAILABLE, null, responseTime);
    }

    public static PollStatus unknown() {
        return unknown(null);
    }

    public static PollStatus unknown(String reason) {
        return new PollStatus(SERVICE_UNKNOWN, reason, -1L);
    }

    public static PollStatus unresponsive() {
        return unresponsive(null);
    }

    public static PollStatus unresponsive(String reason) {
        return new PollStatus(SERVICE_UNRESPONSIVE, reason, -1L);
    }

    public static PollStatus down() {
        return down(null);
    }

    public static PollStatus unavailable() {
        return unavailable(null);
    }

    public static PollStatus down(String reason) {
        return unavailable(reason);
    }

    public static PollStatus unavailable(String reason) {
        return new PollStatus(SERVICE_UNAVAILABLE, reason, -1L);
    }

    public boolean equals(Object o) {
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

    public void setTimestamp(Date timestamp) {
        m_timestamp = timestamp;
    }

    @Column(name="statusReason", length=255, nullable=true)
    public String getReason() {
        return m_reason;
    }

    public void setReason(String reason) {
        m_reason = reason;
    }

    @Column(name="nanoResponseTime", nullable=true)
    public long getNanoResponseTime() {
    	return m_nanoResponseTime;
    }
    
    public void setNanoResponseTime(long nanoResponseTime) {
    	m_nanoResponseTime = nanoResponseTime;
    	this.setResponseTime(nanoResponseTime / 1000000);
    }
    
    @Column(name="responseTime", nullable=true)
    public long getResponseTime() {
        return m_responseTime;
    }

    public void setResponseTime(long responseTime) {
        m_responseTime = responseTime;
    }

    @Transient
    public Collection<Long> getResponseTimes() {
    	return m_responseTimes;
    }
    
    public void setResponseTimes(Collection<Long> responseTimes) {
    	m_responseTimes = responseTimes;
    }

    @Column(name="statusCode", nullable=false)
    public int getStatusCode() {
        return m_statusCode;
    }

    @SuppressWarnings("unused")
    private void setStatusCode(int statusCode) {
        m_statusCode = statusCode;
    }

    @Transient
    public String getStatusName() {
        return s_statusNames[m_statusCode];
    }


}
