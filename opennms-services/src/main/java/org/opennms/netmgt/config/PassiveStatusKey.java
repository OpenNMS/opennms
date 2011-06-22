/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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
