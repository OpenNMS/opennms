/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.model;

import java.net.InetAddress;

import org.opennms.netmgt.model.NetworkBuilder.InterfaceBuilder;

import static org.opennms.core.utils.InetAddressUtils.addr;

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
        return addIpInterface(addr(ipAddress));
    }

    public InterfaceBuilder addIpInterface(final InetAddress ipAddress) {
    	final OnmsIpInterface iface = new OnmsIpInterface(ipAddress, m_snmpIf.getNode());
    	m_snmpIf.addIpInterface(iface);
    	return new InterfaceBuilder(iface);
    }
}
