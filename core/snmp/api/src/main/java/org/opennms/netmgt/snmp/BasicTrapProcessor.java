/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A basic TrapProcessor that stores all values as fields that can
 * be retrieved with getter methods.
 * 
 * @author Seth
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class BasicTrapProcessor implements TrapProcessor {

    private String m_systemId;
    private String m_location;
    private String m_community;
    private long m_creationTime;
    private long m_timeStamp;
    private String m_version;
    private InetAddress m_agentAddress;
    private Map<SnmpObjId,SnmpValue> m_varBinds = new ConcurrentHashMap<>();
    private InetAddress m_trapAddress;
    private TrapIdentity m_trapIdentity;

    public String getSystemId() {
        return m_systemId;
    }

    @Override
    public void setSystemId(String systemId) {
        m_systemId = systemId;
    }

    public String getLocation() {
        return m_location;
    }

    @Override
    public void setLocation(String location) {
        m_location = location;
    }

    public String getCommunity() {
        return m_community;
    }

    @Override
    public void setCommunity(String community) {
        m_community = community;
    }

    public long getCreationTime() {
        return m_creationTime;
    }

    @Override
    public void setCreationTime(long creationTime) {
        m_creationTime = creationTime;
    }

    public long getTimeStamp() {
        return m_timeStamp;
    }

    @Override
    public void setTimeStamp(long timeStamp) {
        m_timeStamp = timeStamp;
    }

    public String getVersion() {
        return m_version;
    }

    @Override
    public void setVersion(String version) {
        m_version = version;
    }

    public InetAddress getAgentAddress() {
        return m_agentAddress;
    }

    @Override
    public void setAgentAddress(InetAddress agentAddress) {
        m_agentAddress = agentAddress;
    }

    public Map<SnmpObjId,SnmpValue> getVarBinds() {
        return m_varBinds;
    }

    @Override
    public void processVarBind(SnmpObjId name, SnmpValue value) {
        // TODO: Do we need to preserve ordering here? Probably.
        m_varBinds.put(name, value);
    }

    public InetAddress getTrapAddress() {
        return m_trapAddress;
    }

    @Override
    public void setTrapAddress(InetAddress trapAddress) {
        m_trapAddress = trapAddress;
    }

    public TrapIdentity getTrapIdentity() {
        return m_trapIdentity;
    }

    @Override
    public void setTrapIdentity(TrapIdentity trapIdentity) {
        m_trapIdentity = trapIdentity;
    }
}
