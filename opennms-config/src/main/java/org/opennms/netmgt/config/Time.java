/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.util.Optional;

/* wrapper object to deal with old XML resources */
public class Time {

	private String m_id;
	private String m_day;
	private String m_begins;
	private String m_ends;

	public Time() {
	}
	
	public Time(final String id, final String day, final String begins, final String ends) {
		m_id = id;
		m_day = day;
		m_begins = begins;
		m_ends = ends;
	}

	public Optional<String> getId() {
		return Optional.ofNullable(m_id);
	}
	
	public void setId(final String id) {
		m_id = id;
	}
	
	public Optional<String> getDay() {
		return Optional.ofNullable(m_day);
	}

	public void setDay(final String day) {
		m_day = day;
	}

	public String getBegins() {
		return m_begins;
	}
	
	public void setBegins(final String begins) {
		m_begins = begins;
	}
	
	public String getEnds() {
		return m_ends;
	}
	
	public void setEnds(final String ends) {
		m_ends = ends;
	}
}
