//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//    
// For more information contact: 
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.capsd.plugins;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.netmgt.capsd.Plugin;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.utils.ParameterMap;
/**
 * <p>LoopPlugin class.</p>
 *
 * @author david
 * @version $Id: $
 */
public class LoopPlugin implements Plugin {

    private String m_protocolName = "LOOP";

    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.Plugin#getProtocolName()
     */
    /**
     * <p>getProtocolName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getProtocolName() {
        return m_protocolName;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.Plugin#isProtocolSupported(java.net.InetAddress)
     */
    /** {@inheritDoc} */
    public boolean isProtocolSupported(InetAddress address) {
        return isProtocolSupported(address, null);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.Plugin#isProtocolSupported(java.net.InetAddress, java.util.Map)
     */
    /** {@inheritDoc} */
    public boolean isProtocolSupported(InetAddress address, Map<String, Object> qualifiers) {
        
        if (qualifiers == null)
            return false;
        
        String ipMatch = getIpMatch(qualifiers);
        if (SnmpPeerFactory.verifyIpMatch(address.getHostAddress(), ipMatch)) {
            return isSupported(qualifiers);
        } else {
            return false;
        }
        
    }

    private boolean isSupported(Map<String, Object> parameters) {
        return ParameterMap.getKeyedString(parameters, "is-supported", "false").equalsIgnoreCase("true");
    }

    private String getIpMatch(Map<String, Object> parameters) {
        return ParameterMap.getKeyedString(parameters, "ip-match", "*.*.*.*");
    }

}
