/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.discovery;

import java.util.Properties;

import org.opennms.netmgt.config.DiscoveryConfigFactory;

/**
 * This class is used to generate a {@link Properties} object that contains
 * scalar values from a {@link DiscoveryConfigFactory} instance.
 */
public abstract class DiscoveryConfigurationFactoryPropertiesConverter {

	public static final String INITIAL_SLEEP_TIME = "initialSleepTime";
	public static final String RESTART_SLEEP_TIME = "restartSleepTime";
	public static final String PACKETS_PER_SECOND = "packetsPerSecond";

	public static Properties getProperties(final DiscoveryConfigFactory factory) {
		final Properties retval = new Properties();
		retval.setProperty(INITIAL_SLEEP_TIME, String.valueOf(factory.getInitialSleepTime()));
		retval.setProperty(RESTART_SLEEP_TIME, String.valueOf(factory.getRestartSleepTime()));
		retval.setProperty(PACKETS_PER_SECOND, String.valueOf(factory.getPacketsPerSecond()));
		return retval;
	}
}
