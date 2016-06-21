/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.core.camel;

import java.util.Properties;

import org.apache.camel.component.properties.PropertiesComponent;

public class JmsQueueNameFactory {

	private static final String NAME_FORMAT_WITH_LOCATION = "OpenNMS.%s.%s@%s";
	private static final String NAME_FORMAT_WITHOUT_LOCATION = "OpenNMS.%s.%s";

	private final String m_daemon;
	private final String m_endpoint;
	private final String m_location;

	public JmsQueueNameFactory(String daemon, String endpoint, String location) {
		m_daemon = daemon;
		m_endpoint = endpoint;
		m_location = location;
	}

	public JmsQueueNameFactory(String daemon, String endpoint) {
		this(daemon, endpoint, null);
	}

	public String getLocation() {
		return m_location;
	}

	/*
	public void setLocation(String location) {
		m_location = location;
	}
	*/

	public String getDaemon() {
		return m_daemon;
	}

	/*
	public void setDaemon(String daemon) {
		m_daemon = daemon;
	}
	*/

	public String getName() {
		if (m_location == null) {
			return getNameWithoutLocation();
		} else {
			return getNameWithLocation(m_location);
		}
	}

	public String getNameWithoutLocation() {
		return String.format(NAME_FORMAT_WITHOUT_LOCATION, m_daemon, m_endpoint);
	}

	public String getNameWithLocation(String location) {
		return String.format(NAME_FORMAT_WITH_LOCATION, m_daemon, m_endpoint, location);
	}

	/**
	 * This method will return both queue name variants in a {@link Properties}
	 * list so that you can easily use it with a {@link PropertiesComponent} in
	 * a Spring context.
	 */
	public Properties getProperties() {
		Properties retval = new Properties();
		retval.setProperty("queueName", getName());
		retval.setProperty("queueNameWithoutLocation", getNameWithoutLocation());
		return retval;
	}
}
