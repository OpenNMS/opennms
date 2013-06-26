/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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
 * <p>EndPointImpl class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
package org.opennms.netmgt.provision.adapters.link;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

import org.opennms.netmgt.icmp.PingerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
public class EndPointImpl implements EndPoint {
    private static final Logger LOG = LoggerFactory.getLogger(EndPointImpl.class);
    private SnmpAgentConfig m_agentConfig;
    private InetAddress m_address;
    private String m_sysOid;

    /**
     * <p>Constructor for EndPointImpl.</p>
     */
    public EndPointImpl() {
    }

    /**
     * <p>Constructor for EndPointImpl.</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     */
    public EndPointImpl(InetAddress address, SnmpAgentConfig agentConfig) {
        m_address = address;
        m_agentConfig = agentConfig;
    }

    /** {@inheritDoc} */
    @Override
    public SnmpValue get(String oid) {
        SnmpObjId objId = SnmpObjId.get(oid);
        return SnmpUtils.get(m_agentConfig, objId);
    }

    /**
     * <p>getAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    @Override
    public InetAddress getAddress() {
        return m_address;
    }

    /**
     * <p>setAddress</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     */
    public void setAddress(InetAddress address) {
        m_address = address;
    }
    
    /**
     * <p>getSysOid</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getSysOid() {
        return m_sysOid;
    }

    /**
     * <p>setSysOid</p>
     *
     * @param sysOid a {@link java.lang.String} object.
     */
    public void setSysOid(String sysOid) {
        m_sysOid = sysOid;
    }
    
    /**
     * <p>ping</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean ping() {
        try {
            Number result = PingerFactory.getInstance().ping(getAddress());
            if (result != null) {
                return true;
            }
        } catch (Throwable e) {
            LOG.debug("Ping failed for address {}", getAddress(), e);
        }
        return false;
    }
    
}
