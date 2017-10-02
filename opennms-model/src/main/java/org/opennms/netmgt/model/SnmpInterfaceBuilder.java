/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.NetworkBuilder.InterfaceBuilder;

/**
 * <p>SnmpInterfaceBuilder class.</p>
 */
public class SnmpInterfaceBuilder {

    private final OnmsSnmpInterface m_snmpIf;

    /**
     * <p>Constructor for SnmpInterfaceBuilder.</p>
     *
     * @param snmpIf a {@link org.opennms.netmgt.model.OnmsSnmpInterface} object.
     */
    public SnmpInterfaceBuilder(OnmsSnmpInterface snmpIf) {
        m_snmpIf = snmpIf;
    }

    /**
     * <p>setIfSpeed</p>
     *
     * @param ifSpeed a long.
     * @return a {@link org.opennms.netmgt.model.SnmpInterfaceBuilder} object.
     */
    public SnmpInterfaceBuilder setIfSpeed(long ifSpeed) {
        m_snmpIf.setIfSpeed(new Long(ifSpeed));
        return this;
    }

    /**
     * <p>setIfDescr</p>
     *
     * @param ifDescr a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.SnmpInterfaceBuilder} object.
     */
    public SnmpInterfaceBuilder setIfDescr(String ifDescr) {
        m_snmpIf.setIfDescr(ifDescr);
        return this;
    }
    
    /**
     * <p>setIfAlias</p>
     *
     * @param ifAlias a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.SnmpInterfaceBuilder} object.
     */
    public SnmpInterfaceBuilder setIfAlias(String ifAlias) {
        m_snmpIf.setIfAlias(ifAlias);
        return this;
    }
    
    /**
     * <p>setIfName</p>
     *
     * @param ifName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.SnmpInterfaceBuilder} object.
     */
    public SnmpInterfaceBuilder setIfName(String ifName) {
        m_snmpIf.setIfName(ifName);
        return this;
    }
    
    /**
     * <p>setIfType</p>
     *
     * @param ifType a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.netmgt.model.SnmpInterfaceBuilder} object.
     */
    public SnmpInterfaceBuilder setIfType(Integer ifType) {
        m_snmpIf.setIfType(ifType);
        return this;
    }

    /**
     * <p>getSnmpInterface</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsSnmpInterface} object.
     */
    public OnmsSnmpInterface getSnmpInterface() {
        return m_snmpIf;
    }
    
    /**
     * <p>setIfOperStatus</p>
     *
     * @param ifOperStatus a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.netmgt.model.SnmpInterfaceBuilder} object.
     */
    public SnmpInterfaceBuilder setIfOperStatus(Integer ifOperStatus) {
        m_snmpIf.setIfOperStatus(ifOperStatus);
        return this;
    }

    /**
     * <p>setCollectionEnabled</p>
     *
     * @param collect a boolean.
     * @return a {@link org.opennms.netmgt.model.SnmpInterfaceBuilder} object.
     */
    public SnmpInterfaceBuilder setCollectionEnabled(boolean collect) {
        m_snmpIf.setCollectionEnabled(collect);
        return this;
    }

    /**
     * <p>setPhysAddr</p>
     *
     * @param physAddr a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.SnmpInterfaceBuilder} object.
     */
    public SnmpInterfaceBuilder setPhysAddr(String physAddr) {
        m_snmpIf.setPhysAddr(physAddr);
        return this;
    }

    public InterfaceBuilder addIpInterface(final String ipAddress) {
    	final OnmsIpInterface iface = new OnmsIpInterface(InetAddressUtils.addr(ipAddress), m_snmpIf.getNode());
    	m_snmpIf.addIpInterface(iface);
    	return new InterfaceBuilder(iface);
    }
}
