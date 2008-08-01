/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created August 1, 2008
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.tl1d;

import java.util.Date;

public class Tl1Message {

	String m_host;
	Date m_timestamp;
	String m_severity;
	String m_message;
	String m_rawMessage;
	
	
	public static Tl1Message create(String rawMessage) {
		// TODO need to add code to parse the raw message
		// Just return a bogus one for now
		return new Tl1Message("localhost", new Date(), "WHOA!", rawMessage, rawMessage);
	}
	
	public Tl1Message(String host, Date timestamp, String severity, String message, String rawMessage) {
		m_host = host;
		m_timestamp = timestamp;
		m_severity = severity;
		m_message = message;
		m_rawMessage = rawMessage;
	}
	
	public String getHost() {
		return m_host;
	}

	public void setHost(String host) {
		m_host = host;
	}

	public Date getTimestamp() {
		return m_timestamp;
	}

	public void setTimestamp(Date timestamp) {
		m_timestamp = timestamp;
	}

	public String getSeverity() {
		return m_severity;
	}

	public void setSeverity(String severity) {
		m_severity = severity;
	}

	public String getMessage() {
		return m_message;
	}

	public void setMessage(String message) {
		m_message = message;
	}
	
	public String getRawMessage() {
		return m_rawMessage;
	}
	
	public void setRawMessage(String rawMessage) {
		m_rawMessage = rawMessage;
	}
	
	public String toString() {
		return m_message;
	}


}
