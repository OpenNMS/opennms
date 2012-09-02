/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
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


/**
 * Container for all state needed by the SnmpThresholder for a
 * ThresholdNetworkInterface.
 *
 * @author ranger
 * @version $Id: $
 */
package org.opennms.netmgt.threshd;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.poller.NetworkInterface;
public class SnmpThresholdNetworkInterface {
    private ThresholdNetworkInterface m_networkInterface;
    private SnmpThresholdConfiguration m_thresholdConfiguration;
    private Map<String, Map<String, Set<ThresholdEntity>>> m_allInterfaceMap;
    
    /**
     * <p>Constructor for SnmpThresholdNetworkInterface.</p>
     *
     * @param thresholdsDao a {@link org.opennms.netmgt.threshd.ThresholdsDao} object.
     * @param iface a {@link org.opennms.netmgt.threshd.ThresholdNetworkInterface} object.
     * @param parms a {@link java.util.Map} object.
     */
    public SnmpThresholdNetworkInterface(ThresholdsDao thresholdsDao, ThresholdNetworkInterface iface, Map<?,?> parms) {
        setNetworkInterface(iface);
        setThresholdConfiguration(new SnmpThresholdConfiguration(thresholdsDao, parms));

        setAllInterfaceMap(new HashMap<String, Map<String, Set<ThresholdEntity>>>());
    }

    /**
     * <p>getThresholdConfiguration</p>
     *
     * @return a {@link org.opennms.netmgt.threshd.SnmpThresholdConfiguration} object.
     */
    public SnmpThresholdConfiguration getThresholdConfiguration() {
        return m_thresholdConfiguration;
    }

    /**
     * <p>setThresholdConfiguration</p>
     *
     * @param thresholdConfiguration a {@link org.opennms.netmgt.threshd.SnmpThresholdConfiguration} object.
     */
    public void setThresholdConfiguration(SnmpThresholdConfiguration thresholdConfiguration) {
        m_thresholdConfiguration = thresholdConfiguration;
    }
    
    /**
     * <p>setAllInterfaceMap</p>
     *
     * @param allInterfaceMap a {@link java.util.Map} object.
     */
    public void setAllInterfaceMap(Map<String, Map<String, Set<ThresholdEntity>>> allInterfaceMap) {
        m_allInterfaceMap = allInterfaceMap;
    }
    
    /**
     * <p>getAllInterfaceMap</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, Map<String, Set<ThresholdEntity>>> getAllInterfaceMap() {
        return m_allInterfaceMap;
    }
    
    /**
     * <p>getNetworkInterface</p>
     *
     * @return a {@link org.opennms.netmgt.threshd.ThresholdNetworkInterface} object.
     */
    public ThresholdNetworkInterface getNetworkInterface() {
        return m_networkInterface;
    }

    /**
     * <p>setNetworkInterface</p>
     *
     * @param networkInterface a {@link org.opennms.netmgt.threshd.ThresholdNetworkInterface} object.
     */
    public void setNetworkInterface(ThresholdNetworkInterface networkInterface) {
        m_networkInterface = networkInterface;
    }

    /**
     * <p>isIPV4</p>
     *
     * @return a boolean.
     */
    public boolean isIPV4() {
        return getNetworkInterface().getType() == NetworkInterface.TYPE_INET;
    }

    /**
     * <p>getInetAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getInetAddress() {
        return (InetAddress) getNetworkInterface().getAddress();
    }

    /**
     * <p>getIpAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddress() {
        return InetAddressUtils.str(getInetAddress());
    }

    /**
     * <p>getNodeId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getNodeId() {
        return m_networkInterface.getNodeId();
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getNodeId() + "/" + getIpAddress() + "/" + m_thresholdConfiguration.getGroupName();
    }

    /**
     * <p>getNodeThresholdMap</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, Set<ThresholdEntity>> getNodeThresholdMap() {
    	ThresholdResourceType resourceType = getThresholdConfiguration().getNodeResourceType();
    	return Collections.unmodifiableMap(resourceType.getThresholdMap());
    }

    /**
     * <p>getInterfaceThresholdMap</p>
     *
     * @param ifLabel a {@link java.lang.String} object.
     * @return a {@link java.util.Map} object.
     */
    @SuppressWarnings("deprecation")
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
        
        return Collections.unmodifiableMap(thresholdMap);
    }

}
