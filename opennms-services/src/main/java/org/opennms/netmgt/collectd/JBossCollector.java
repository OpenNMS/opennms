/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2005-2006, 2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.netmgt.collectd;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.protocols.jmx.connectors.ConnectionWrapper;
import org.opennms.protocols.jmx.connectors.JBossConnectionFactory;

/*
 * The JBossCollector class manages the querying and storage of data into RRD files.  The list of 
 * MBeans to be queried is read from the jmx-datacollection-config.xml file using the "jboss" service name.
 * The super class, JMXCollector, performs all the work.
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class JBossCollector  extends JMXCollector {

    public JBossCollector() {
        setServiceName("jboss");
    }

    /* Returns the CollectionWrapper object from the factory using the parameterMap and ip address.
     * 
     * @see org.opennms.netmgt.collectd.JMXCollector#getMBeanServer(java.util.Map)
     */
    public ConnectionWrapper getMBeanServerConnection(Map<String, String> parameterMap, InetAddress address) {
        return  JBossConnectionFactory.getMBeanServerConnection(parameterMap, address);
    }
    
}
