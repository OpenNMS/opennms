/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.capsd.plugins;

import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.jmx.connection.JmxConnectors;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/*
* This class enables the monitoring of MX4J enabled services.  Since there will potentially be several 
* MX4J services's being monitored the user needs to provide a "friendly name" in the poller-configuration file, 
* otherwise the port will be used.  
* 
* @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
* @author <A HREF="http://www.opennms.org/">OpenNMS </A>
*/
/**
 * <p>MX4JPlugin class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class MX4JPlugin extends JMXPlugin {
  
  /* The protocol name is used to...
   * @see org.opennms.netmgt.capsd.Plugin#getProtocolName()
   */
  /** {@inheritDoc} */
  @Override
  public String getProtocolName(Map<String, Object> map) {
      return ParameterMap.getKeyedString(map, "friendlyname", "mx4j");
  }

    @Override
    protected String getConnectionName() {
        return JmxConnectors.MX4J;
    }
  
  /* 
   * @see org.opennms.netmgt.capsd.Plugin#isProtocolSupported(java.net.InetAddress)
   */
  /** {@inheritDoc} */
  @Override
  public boolean isProtocolSupported(InetAddress address) {
      HashMap<String, Object> map = new HashMap<String, Object>();
      map.put("port",           "9004");
      map.put("factory",        "JMXRMI");
      map.put("friendlyname",   "mx4j");
  
      return isProtocolSupported(address, map);
  }
}
