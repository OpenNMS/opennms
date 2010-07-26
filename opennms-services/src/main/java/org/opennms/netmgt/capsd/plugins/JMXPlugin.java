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
import java.util.Map;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.AbstractPlugin;
import org.opennms.protocols.jmx.connectors.ConnectionWrapper;


/*
 * This is the base class that handles the actual capability test.  The subclass is responsible 
 * for getting the ConnectionWrapper that is used to attempt to contact the remote resource.
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
/**
 * <p>Abstract JMXPlugin class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class JMXPlugin extends AbstractPlugin {
    
    private String protocolName = null;

    /* The subclass can use set the protocol name from the parameters map
     * 
     * @see org.opennms.netmgt.capsd.Plugin#getProtocolName()
     */
    /**
     * <p>Getter for the field <code>protocolName</code>.</p>
     *
     * @param parameterMap a {@link java.util.Map} object.
     * @return a {@link java.lang.String} object.
     */
    public abstract String getProtocolName(Map<String, Object> parameterMap);
    
    /*
     * The subclass is responsible for getting the connection.
     */
    /**
     * <p>getMBeanServerConnection</p>
     *
     * @param parameterMap a {@link java.util.Map} object.
     * @param address a {@link java.net.InetAddress} object.
     * @return a {@link org.opennms.protocols.jmx.connectors.ConnectionWrapper} object.
     */
    public abstract ConnectionWrapper getMBeanServerConnection(Map<String, Object> parameterMap, InetAddress address);
    
    /*
     * @see org.opennms.netmgt.capsd.Plugin#getProtocolName()
     */
    /**
     * <p>Getter for the field <code>protocolName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getProtocolName() {
        return protocolName.toUpperCase();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.Plugin#isProtocolSupported(java.net.InetAddress, java.util.Map)
     */

    /** {@inheritDoc} */
    public boolean isProtocolSupported(InetAddress address, Map<String, Object> map) {
        
        if (protocolName == null) {
            protocolName = getProtocolName(map);
        }

        ThreadCategory log = ThreadCategory.getInstance(getClass());
        boolean res = false;
        ConnectionWrapper connection = null;
        try {
            
            connection = getMBeanServerConnection(map, address);
            
            Integer result = connection.getMBeanServer().getMBeanCount();
            log.debug("isProtocolSupported? " + getProtocolName() + " " + result + " " + connection);
            if (result != null) {
                res = true;
            }
        } catch (Exception e) {
            log.debug(getProtocolName(map) + " - isProtocolSupported - failed! " + address.getHostAddress());
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        
        return res;
    }
}

