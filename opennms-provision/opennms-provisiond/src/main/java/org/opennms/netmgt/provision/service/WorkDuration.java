/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;

/**
 * <p>WorkDuration class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class WorkDuration {

	private String m_name = null;
	private long m_start = -1L;
	private long m_end = -1L;
	
	/**
	 * <p>Constructor for WorkDuration.</p>
	 */
	public WorkDuration() {
		this(null);
	}

	/**
	 * <p>Constructor for WorkDuration.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	public WorkDuration(String name) {
		m_name = name;
	}
	
	/**
	 * <p>setName</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	public void setName(String name) {
		m_name = name;
	}

	/**
	 * <p>start</p>
	 */
	public void start() {
		m_start = System.currentTimeMillis();
	}

	/**
	 * <p>end</p>
	 */
	public void end() {
		m_end = System.currentTimeMillis();
	}
	
	/**
	 * <p>getLength</p>
	 *
	 * @return a long.
	 */
	public long getLength() {
		if (m_start == -1L) return 0L;
		long end = (m_end == -1L ? System.currentTimeMillis() : m_end);
		return end - m_start;
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
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
