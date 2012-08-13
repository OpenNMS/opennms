/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/* wrapper object to deal with old castor resources */
public class BasicSchedule {

	private String m_name;
	private String m_type;
	private List<Time> m_times = new ArrayList<Time>();

	public String getName() {
		return m_name;
	}
	
	public void setName(final String name) {
		m_name = name;
	}

	public String getType() {
		return m_type;
	}

	public void setType(final String type) {
		m_type = type;
	}

	public void setTimeCollection(final Collection<Time> times) {
		synchronized(m_times) {
			m_times.clear();
			m_times.addAll(times);
		}
	}

	public Collection<Time> getTimeCollection() {
		synchronized(m_times) {
			return m_times;
		}
	}
	
	public Enumeration<Time> enumerateTime() {
		synchronized(m_times) {
			return Collections.enumeration(m_times);
		}
	}

	public int getTimeCount() {
		synchronized(m_times) {
			return m_times.size();
		}
	}

	public Time getTime(final int index) {
		synchronized(m_times) {
			return m_times.get(index);
		}
	}
}
