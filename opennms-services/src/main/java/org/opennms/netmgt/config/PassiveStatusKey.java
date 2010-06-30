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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.config;

/**
 * <p>PassiveStatusKey class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class PassiveStatusKey {
    
    private String m_nodeLabel;
    private String m_ipAddr;
    private String m_serviceName;

    /**
     * <p>Constructor for PassiveStatusKey.</p>
     *
     * @param nodeLabel a {@link java.lang.String} object.
     * @param ipAddr a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     */
    public PassiveStatusKey(String nodeLabel, String ipAddr, String serviceName) {
        m_nodeLabel = nodeLabel;
        m_ipAddr = ipAddr;
        m_serviceName = serviceName;
    }

    /**
     * <p>getIpAddr</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddr() {
        return m_ipAddr;
    }

    /**
     * <p>getNodeLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNodeLabel() {
        return m_nodeLabel;
    }

    /**
     * <p>getServiceName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName() {
        return m_serviceName;
    }

    /** {@inheritDoc} */
    public boolean equals(Object o) {
        if (o instanceof PassiveStatusKey) {
            PassiveStatusKey key = (PassiveStatusKey) o;
            return getNodeLabel().equals(key.getNodeLabel()) &&
                    getIpAddr().equals(key.getIpAddr()) &&
                    getServiceName().equals(key.getServiceName());
        }
        return false;
    }

    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    public int hashCode() {
        return getNodeLabel().hashCode() ^ getIpAddr().hashCode() ^ getServiceName().hashCode();
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return getNodeLabel()+':'+getIpAddr()+':'+getServiceName();
    }


}
