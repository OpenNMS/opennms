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


/**
 * Represents the status of a node, interface or services
 * @author brozow
 */
public class PollStatus {
    
    /**
     * Status of the pollable object.
     */

    public static final PollStatus STATUS_UP = new PollStatus(PollStatus.SERVICE_AVAILABLE, "Up");

    public static final PollStatus STATUS_DOWN = new PollStatus(PollStatus.SERVICE_UNAVAILABLE, "Down");
    
    public static final PollStatus STATUS_UNRESPONSIVE = new PollStatus(PollStatus.SERVICE_UNRESPONSIVE, "Unresponsive");
    
    public static final PollStatus STATUS_UNKNOWN = new PollStatus(PollStatus.SERVICE_UNKNOWN, "Unknown");
    
    int m_statusCode;
    String m_statusName;
    String m_reason;

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
    
    public static PollStatus getPollStatus(int status) {
        switch (status) {
        case PollStatus.SERVICE_AVAILABLE:
            return STATUS_UP;
        case PollStatus.SERVICE_UNRESPONSIVE:
            return STATUS_UNRESPONSIVE;
        case PollStatus.SERVICE_UNAVAILABLE:
        default:
            return STATUS_DOWN;
        }
    }
    
    public static PollStatus decodePollStatus(String statusName) {
        if (STATUS_UP.getStatusName().equalsIgnoreCase(statusName))
            return STATUS_UP;
        if (STATUS_DOWN.getStatusName().equalsIgnoreCase(statusName))
            return STATUS_DOWN;
        if (STATUS_UNRESPONSIVE.getStatusName().equalsIgnoreCase(statusName))
            return STATUS_UNRESPONSIVE;
        if (STATUS_UNKNOWN.getStatusName().equalsIgnoreCase(statusName))
            return STATUS_UNKNOWN;
        return STATUS_UNKNOWN;
    }
    
    public static PollStatus getPollStatus(int status, String reason) {
        return new PollStatus(getPollStatus(status), reason);
    }
    
    public static PollStatus getPollStatus(PollStatus status, String reason) {
        return new PollStatus(status, reason);
    }
    
    public static PollStatus decodePollStatus(String statusName, String reason) {
        return new PollStatus(decodePollStatus(statusName), reason);
    }
    
    private PollStatus(PollStatus s, String reason) {
        this(s.getStatusCode(), s.getStatusName(), reason);
    }

    private PollStatus(int statusCode, String statusName) {
        this(statusCode, statusName, null);
    }
    
    private PollStatus(int statusCode, String statusName, String reason) {
        m_statusCode = statusCode;
        m_statusName = statusName;
        m_reason = reason;
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

    public boolean isUp() {
        return !isDown();
    }
    
    public boolean isDown() {
        return this.equals(STATUS_DOWN);
    }
    
    public String toString() {
        return m_statusName;
    }

    public String getReason() {
        return m_reason;
    }

    public void setReason(String reason) {
        m_reason = reason;
    }

    public int getStatusCode() {
        return m_statusCode;
    }

    public void setStatusCode(int statusCode) {
        m_statusCode = statusCode;
    }

    public String getStatusName() {
        return m_statusName;
    }

    public void setStatusName(String statusName) {
        m_statusName = statusName;
    }

}
