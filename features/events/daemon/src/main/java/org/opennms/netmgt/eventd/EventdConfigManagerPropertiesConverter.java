/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd;

import java.util.Properties;

import org.opennms.netmgt.config.EventdConfigManager;

/**
 * @author <A HREF="mailto:seth@opennms.org">Seth Leger</A>
 */
public abstract class EventdConfigManagerPropertiesConverter {

	public static Properties getProperties(EventdConfigManager config) {
		final Properties m_props = new Properties();
		m_props.setProperty("eventIpcManagerHandlerPoolSize", String.valueOf(config.getReceivers()));
		m_props.setProperty("eventIpcManagerHandlerQueueLength", String.valueOf(config.getQueueLength()));
		m_props.setProperty("shouldLogEventSummaries", Boolean.toString(config.shouldLogEventSummaries()));
		m_props.setProperty("tcpIpAddress", config.getTCPIpAddress());
		m_props.setProperty("tcpPort", String.valueOf(config.getTCPPort()));
		m_props.setProperty("udpIpAddress", config.getUDPIpAddress());
		m_props.setProperty("udpPort", String.valueOf(config.getUDPPort()));
		return m_props;
	}

}
