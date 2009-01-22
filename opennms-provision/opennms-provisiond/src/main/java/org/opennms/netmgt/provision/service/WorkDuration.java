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
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.service;

public class WorkDuration {

	private String m_name = null;
	private long m_start = -1L;
	private long m_end = -1L;
	
	public WorkDuration() {
		this(null);
	}

	public WorkDuration(String name) {
		m_name = name;
	}
	
	public void setName(String name) {
		m_name = name;
	}

	public void start() {
		m_start = System.currentTimeMillis();
	}

	public void end() {
		m_end = System.currentTimeMillis();
	}
	
	public long getLength() {
		if (m_start == -1L) return 0L;
		long end = (m_end == -1L ? System.currentTimeMillis() : m_end);
		return end - m_start;
	}

	public String toString() {
		return (m_name == null ? "" : m_name+": ")+(m_start == -1L ? "has not begun": elapsedTime());
	}

	private String elapsedTime() {

		long duration = getLength();

		long hours = duration / 3600000L;
		duration = duration % 3600000L;
		long mins = duration / 60000L;
		duration = duration % 60000L;
		long secs = duration / 1000L;
		long millis = duration % 1000L;

		StringBuffer elapsed = new StringBuffer();
		if (hours > 0)
			elapsed.append(hours).append("h ");
		if (mins > 0)
			elapsed.append(mins).append("m ");
		if (secs > 0)
			elapsed.append(secs).append("s ");
		if (millis > 0)
			elapsed.append(millis).append("ms");

		return elapsed.toString();

	}

}