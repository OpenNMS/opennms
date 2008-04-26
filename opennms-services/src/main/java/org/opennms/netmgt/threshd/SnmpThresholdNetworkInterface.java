/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

/**
 * Container for all state needed by the SnmpThresholder for a
 * ThresholdNetworkInterface.
 */
package org.opennms.netmgt.threshd;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.poller.NetworkInterface;

public class SnmpThresholdNetworkInterface {
    private ThresholdNetworkInterface m_networkInterface;
    private SnmpThresholdConfiguration m_thresholdConfiguration;
    private Map<String, Map<String, Set<ThresholdEntity>>> m_allInterfaceMap;
    
    public SnmpThresholdNetworkInterface(ThresholdsDao thresholdsDao, ThresholdNetworkInterface iface, Map parms) {
        setNetworkInterface(iface);
        setThresholdConfiguration(new SnmpThresholdConfiguration(thresholdsDao, parms));

        setAllInterfaceMap(new HashMap<String, Map<String, Set<ThresholdEntity>>>());
    }

    public SnmpThresholdConfiguration getThresholdConfiguration() {
        return m_thresholdConfiguration;
    }

    public void setThresholdConfiguration(SnmpThresholdConfiguration thresholdConfiguration) {
        m_thresholdConfiguration = thresholdConfiguration;
    }
    
    public void setAllInterfaceMap(Map<String, Map<String, Set<ThresholdEntity>>> allInterfaceMap) {
        m_allInterfaceMap = allInterfaceMap;
    }
    
    public Map<String, Map<String, Set<ThresholdEntity>>> getAllInterfaceMap() {
        return m_allInterfaceMap;
    }
    
    public ThresholdNetworkInterface getNetworkInterface() {
        return m_networkInterface;
    }

    public void setNetworkInterface(ThresholdNetworkInterface networkInterface) {
        m_networkInterface = networkInterface;
    }

    public boolean isIPV4() {
        return getNetworkInterface().getType() == NetworkInterface.TYPE_IPV4;
    }

    public InetAddress getInetAddress() {
        return (InetAddress) getNetworkInterface().getAddress();
    }

    public String getIpAddress() {
        return getInetAddress().getHostAddress();
    }

    public Integer getNodeId() {
        return m_networkInterface.getNodeId();
    }
    
    @Override
    public String toString() {
        return getNodeId() + "/" + getIpAddress() + "/" + m_thresholdConfiguration.getGroupName();
    }

    public Map<String, Set<ThresholdEntity>> getNodeThresholdMap() {
    	ThresholdResourceType resourceType = getThresholdConfiguration().getNodeResourceType();
    	return resourceType.getThresholdMap();
    }

    public Map<String, Set<ThresholdEntity>> getInterfaceThresholdMap(String ifLabel) {
        ThresholdResourceType resourceType = getThresholdConfiguration().getIfResourceType();

        // Attempt to retrieve the threshold map for this interface using the ifLabel for the interface
        Map<String, Set<ThresholdEntity>> thresholdMap = getAllInterfaceMap().get(ifLabel);
        if (thresholdMap == null) {
            // Doesn't exist yet, go ahead and create it.  Must maintain a separate threshold map for each interface.
            thresholdMap = SnmpThresholder.getAttributeMap(resourceType);

            // Add the new threshold map for this interface to the all interfaces map.
            getAllInterfaceMap().put(ifLabel, thresholdMap);
        }
        
        return thresholdMap;
    }

}