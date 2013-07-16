/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.protocols.jmx.connectors.ConnectionWrapper;
import org.opennms.protocols.jmx.connectors.MX4JConnectionFactory;

/*
* The class is responsible for getting the connection to the remote jmx server.  The
* super class (JMXMonitor) performs the checking to see if the service exists and 
* how long it took to make the connection.
*  
* @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
* @author <A HREF="http://www.opennms.org/">OpenNMS </A>
*/
/**
 * <p>MX4JMonitor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class MX4JMonitor extends JMXMonitor {

  /* (non-Javadoc)
   * @see org.opennms.netmgt.poller.monitors.JMXMonitor#getMBeanServerConnection(java.util.Map, java.net.InetAddress)
   */
  /** {@inheritDoc} */
  @Override
  public ConnectionWrapper getMBeanServerConnection(Map<String, Object> parameterMap, InetAddress address) {
      return MX4JConnectionFactory.getMBeanServerConnection(parameterMap, address);
  }

}
