//
//This file is part of the OpenNMS(R) Application.
//
//OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.
//
//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.                                                            
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//  
//For more information contact: 
// OpenNMS Licensing       <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//

package org.opennms.netmgt.capsd.plugins;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.ParameterMap;
import org.opennms.protocols.jmx.connectors.ConnectionWrapper;
import org.opennms.protocols.jmx.connectors.Jsr160ConnectionFactory;

/*
 * This class enables the monitoring of Jsr160 service.  Since there will potentially be several 
 * Jsr160 services's being monitored the user needs to provide a "friendly name" in the poller-configuration file, 
 * otherwise the port will be used.  
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
/**
 * <p>Jsr160Plugin class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Jsr160Plugin extends JMXPlugin {
    
    /* The factory handles the creation of the connection and returns a CollectionWrapper which is used
     * in the JXMPlugin base class to determine whether this capability exists.  
     * 
     * @see org.opennms.netmgt.capsd.JMXPlugin#getMBeanServer(java.util.Map, java.net.InetAddress)
     */
    /** {@inheritDoc} */
    public ConnectionWrapper getMBeanServerConnection(Map<String, Object> parameterMap, InetAddress address) {
        return Jsr160ConnectionFactory.getMBeanServerConnection(parameterMap, address);
    }
    
    /* The protocol name is used to...
     * @see org.opennms.netmgt.capsd.Plugin#getProtocolName()
     */
    /** {@inheritDoc} */
    public String getProtocolName(Map<String, Object> map) {
        return ParameterMap.getKeyedString(map, "friendlyname", "jsr160");
    }
    
    /* 
     * @see org.opennms.netmgt.capsd.Plugin#isProtocolSupported(java.net.InetAddress)
     */
    /** {@inheritDoc} */
    public boolean isProtocolSupported(InetAddress address) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("port",           "9004");
        map.put("factory",        "JMXRMI");
        map.put("friendlyname",   "jsr160");
    
        return isProtocolSupported(address, map);
    }
}
