/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.protocols.jmx.connectors.ConnectionWrapper;
import org.opennms.protocols.jmx.connectors.JMXSecureConnectionFactory;

/**
 * <p>JMXSecureCollector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class JMXSecureCollector extends JMXCollector
{

	/**
	 * <p>Constructor for JMXSecureCollector.</p>
	 */
	public JMXSecureCollector()
	{
		//The value of serviceName will be assumed as a collection name
		//from jmx-datacollection-config.xml if no "collection"
		//parameter will be specified in collectd-configuration.xml.
		//Service name also will be the relative directory to store RRD data in
		//if "useFriendlyName" variable set to false.
		//If "useFriendlyName" variable set to true, RRD data will be stored in
		//directory specified by "friendly-name" parameter in collectd-configuration.xml.
		//If "friendly-name" is not specified in collectd-configuration.xml, then directory
		//name will be the "port" parameter.
		setServiceName("ssl-jmxmp");
		setUseFriendlyName(true);
	}

    /** {@inheritDoc} */
        @Override
    public ConnectionWrapper getMBeanServerConnection(Map<String,Object> parameterMap, InetAddress address)
	{
		return JMXSecureConnectionFactory.getMBeanServerConnection(parameterMap, address);
	}
}
