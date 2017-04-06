/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp.mock;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.InetAddrUtils;
import org.opennms.netmgt.snmp.SnmpAgentAddress;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpStrategy;
import org.opennms.netmgt.snmp.SnmpTrapBuilder;
import org.opennms.netmgt.snmp.SnmpV1TrapBuilder;
import org.opennms.netmgt.snmp.SnmpV2TrapBuilder;
import org.opennms.netmgt.snmp.SnmpV3TrapBuilder;
import org.opennms.netmgt.snmp.SnmpV3User;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpValueFactory;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.netmgt.snmp.TrapNotificationListener;
import org.opennms.netmgt.snmp.TrapProcessorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

public class MockSnmpStrategy implements SnmpStrategy {
	private static final transient Logger LOG = LoggerFactory.getLogger(MockSnmpStrategy.class);
	
    public static final SnmpAgentAddress ALL_AGENTS = new SnmpAgentAddress(InetAddrUtils.addr("0.0.0.0"), 161);
    private static final SnmpValue[] EMPTY_SNMP_VALUE_ARRAY = new SnmpValue[0];

    // TOG's enterprise ID
    private static int s_enterpriseId = 5813;
    private static Map<SnmpAgentAddress,PropertyOidContainer> m_loaders = new HashMap<SnmpAgentAddress,PropertyOidContainer>();

    public MockSnmpStrategy() {
    }

    protected PropertyOidContainer getOidContainer(final SnmpAgentConfig agentConfig) {
        return getOidContainer(new SnmpAgentAddress(agentConfig.getAddress(), agentConfig.getPort()));
    }

    protected PropertyOidContainer getOidContainer(final SnmpAgentAddress aa) {
        if (m_loaders.containsKey(aa)) {
            return m_loaders.get(aa);
        } else {
            return m_loaders.get(ALL_AGENTS);
        }
    }

    @Override
    public SnmpWalker createWalker(final SnmpAgentConfig agentConfig, final String name, final CollectionTracker tracker) {
        LOG.debug("createWalker({}/{}, {}, {})", InetAddrUtils.str(agentConfig.getAddress()), agentConfig.getPort(), name, tracker.getClass().getName());
        final SnmpAgentAddress aa = new SnmpAgentAddress(agentConfig.getAddress(), agentConfig.getPort());
        final PropertyOidContainer oidContainer = getOidContainer(aa);
        return new MockSnmpWalker(aa, agentConfig.getVersion(), oidContainer, name, tracker, agentConfig.getMaxVarsPerPdu(), agentConfig.getRetries());
    }

    @Override
    public SnmpValue set(final SnmpAgentConfig agentConfig, final SnmpObjId oid, final SnmpValue value) {
        final PropertyOidContainer oidContainer = getOidContainer(agentConfig);
        if (oidContainer == null) return null;
        return oidContainer.set(oid, value);
    }

    @Override
    public SnmpValue[] set(final SnmpAgentConfig agentConfig, final SnmpObjId[] oids, final SnmpValue[] values) {
        final PropertyOidContainer oidContainer = getOidContainer(agentConfig);
        if (oidContainer == null) return new SnmpValue[values.length];
        return oidContainer.set(oids, values);
    }

    @Override
    public SnmpValue get(final SnmpAgentConfig agentConfig, final SnmpObjId oid) {
        final PropertyOidContainer oidContainer = getOidContainer(agentConfig);
        if (oidContainer == null) return null;

        SnmpValue val = oidContainer.findValueForOid(oid);
        if (val.isNull()) {
        	return null;
        }
		return val;
    }

    @Override
    public SnmpValue[] get(final SnmpAgentConfig agentConfig, final SnmpObjId[] oids) {
        final PropertyOidContainer container = getOidContainer(agentConfig);
        if (container == null) return new SnmpValue[oids.length];
        final List<SnmpValue> values = new ArrayList<SnmpValue>();

        for (final SnmpObjId oid : oids) {
    		values.add(container.findValueForOid(oid));
        }
        return values.toArray(EMPTY_SNMP_VALUE_ARRAY);
    }

    @Override
    public SnmpValue getNext(final SnmpAgentConfig agentConfig, final SnmpObjId oid) {
        final PropertyOidContainer oidContainer = getOidContainer(agentConfig);
        if (oidContainer == null) return null;
        return oidContainer.findNextValueForOid(oid);
    }

    @Override
    public SnmpValue[] getNext(final SnmpAgentConfig agentConfig, final SnmpObjId[] oids) {
        final PropertyOidContainer oidContainer = getOidContainer(agentConfig);
        if (oidContainer == null) return null;
        final List<SnmpValue> values = new ArrayList<SnmpValue>();

        for (final SnmpObjId oid : oids) {
            values.add(oidContainer.findNextValueForOid(oid));
        }
        return values.toArray(EMPTY_SNMP_VALUE_ARRAY);
    }

    @Override
    public SnmpValue[] getBulk(final SnmpAgentConfig agentConfig, final SnmpObjId[] oids) {
        return getNext(agentConfig, oids);
    }

    @Override
    public void registerForTraps(final TrapNotificationListener listener, final TrapProcessorFactory processorFactory, final InetAddress address, final int snmpTrapPort) throws IOException {
        LOG.warn("Can't register for traps.  No network in the MockSnmpStrategy!");
    }

    @Override
    public void registerForTraps(final TrapNotificationListener listener, final TrapProcessorFactory processorFactory, final int snmpTrapPort) throws IOException {
        LOG.warn("Can't register for traps.  No network in the MockSnmpStrategy!");
    }

    @Override
    public void registerForTraps(TrapNotificationListener listener, TrapProcessorFactory processorFactory, InetAddress address, int snmpTrapPort, List<SnmpV3User> snmpv3Users) throws IOException {
        LOG.warn("Can't register for traps.  No network in the MockSnmpStrategy!");
    }

    @Override
    public void unregisterForTraps(final TrapNotificationListener listener, final InetAddress address, final int snmpTrapPort) throws IOException {
    }

    @Override
    public void unregisterForTraps(final TrapNotificationListener listener, final int snmpTrapPort) throws IOException {
    }

    @Override
    public SnmpValueFactory getValueFactory() {
    	return new MockSnmpValueFactory();
    }

    @Override
    public SnmpV1TrapBuilder getV1TrapBuilder() {
    	throw new UnsupportedOperationException("Not yet implemented!");
//        return new NullSnmpV1TrapBuilder();
    }

    @Override
    public SnmpTrapBuilder getV2TrapBuilder() {
    	throw new UnsupportedOperationException("Not yet implemented!");
//        return new NullSnmpTrapBuilder();
    }

    @Override
    public SnmpV3TrapBuilder getV3TrapBuilder() {
    	throw new UnsupportedOperationException("Not yet implemented!");
//        return new NullSnmpV3TrapBuilder();
    }

    @Override
    public SnmpV2TrapBuilder getV2InformBuilder() {
    	throw new UnsupportedOperationException("Not yet implemented!");
//        return new NullSnmpV2TrapBuilder();
    }

    @Override
    public SnmpV3TrapBuilder getV3InformBuilder() {
    	throw new UnsupportedOperationException("Not yet implemented!");
//        return new NullSnmpV3TrapBuilder();
    }

    @Override
    public byte[] getLocalEngineID() {
        // lovingly stolen from SNMP4J
        final byte[] engineID = new byte[5];
        engineID[0] = (byte) (0x80 | ((s_enterpriseId >> 24) & 0xFF));
        engineID[1] = (byte) ((s_enterpriseId >> 16) & 0xFF);
        engineID[2] = (byte) ((s_enterpriseId >> 8) & 0xFF);
        engineID[3] = (byte) (s_enterpriseId & 0xFF);
        final byte[] ip = InetAddrUtils.getLocalHostAddress().getAddress();

        if (ip.length == 4) {
            // IPv4
            engineID[4] = 1;
        } else if (ip.length == 16) {
            // IPv6
            engineID[4] = 2;
        } else {
            // Text
            engineID[4] = 4;
        }
        
        final byte[] bytes = new byte[engineID.length+ip.length];
        System.arraycopy(engineID, 0, bytes, 0, engineID.length);
        System.arraycopy(ip, 0, bytes, engineID.length, ip.length);
        
        return bytes;
    }

    public static void setDataForAddress(final SnmpAgentAddress agentAddress, final Resource resource) throws IOException {
        m_loaders.put(agentAddress, new PropertyOidContainer(resource));
    }

    public static void updateIntValue(final SnmpAgentAddress agentAddress, String oid, int value) {
        m_loaders.get(agentAddress).set(SnmpObjId.get(oid), new MockSnmpValueFactory().getInt32(value));
    }

    public static void updateStringValue(final SnmpAgentAddress agentAddress, String oid, String value) {
        try {
            m_loaders.get(agentAddress).set(SnmpObjId.get(oid), new MockSnmpValueFactory().getOctetString(value.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            // Should be impossible
        }
    }

    public static void updateCounter32Value(final SnmpAgentAddress agentAddress, String oid, long value) {
        m_loaders.get(agentAddress).set(SnmpObjId.get(oid), new MockSnmpValueFactory().getCounter32(value));
    }

    public static void updateCounter64Value(final SnmpAgentAddress agentAddress, String oid, BigInteger value) {
        m_loaders.get(agentAddress).set(SnmpObjId.get(oid), new MockSnmpValueFactory().getCounter64(value));
    }

    public static void removeHost(final SnmpAgentAddress agentAddr) {
        m_loaders.remove(agentAddr);
    }

    public static void resetData() {
        m_loaders.clear();
    }

}
