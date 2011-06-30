package org.opennms.netmgt.snmp.mock;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpStrategy;
import org.opennms.netmgt.snmp.SnmpTrapBuilder;
import org.opennms.netmgt.snmp.SnmpV1TrapBuilder;
import org.opennms.netmgt.snmp.SnmpV2TrapBuilder;
import org.opennms.netmgt.snmp.SnmpV3TrapBuilder;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpValueFactory;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.netmgt.snmp.TrapNotificationListener;
import org.opennms.netmgt.snmp.TrapProcessorFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public class MockSnmpStrategy implements SnmpStrategy {
    private static final SnmpValue[] EMPTY_SNMP_VALUE_ARRAY = new SnmpValue[0];

    public static final class AgentAddress {
        private final InetAddress m_address;
        private final Integer m_port;
        private int m_hashCode = 0;

        public AgentAddress(final InetAddress agentAddress, final Integer agentPort) {
            Assert.notNull(agentPort);
            m_address = agentAddress;
            m_port = agentPort;
            
            m_hashCode = new HashCodeBuilder(7, 15)
                .append(m_address)
                .append(m_port)
                .toHashCode();
        }

        public InetAddress getAddress() {
            return m_address;
        }
        
        public Integer getPort() {
            return m_port;
        }
        
        public boolean equals(final Object obj) {
            if (!(obj instanceof AgentAddress)) return false;
            final AgentAddress that = (AgentAddress)obj;
            return new EqualsBuilder()
                .append(this.getAddress(), that.getAddress())
                .append(this.getPort(), that.getPort())
                .isEquals();
        }
        
        public int hashCode() {
            return m_hashCode;
        }
        
        public String toString() {
        	return InetAddressUtils.str(m_address) + ":" + m_port;
        }
    }

    // TOG's enterprise ID
    private static int s_enterpriseId = 5813;
    private static Map<AgentAddress,PropertyOidContainer> m_loaders = new HashMap<AgentAddress,PropertyOidContainer>();

    public MockSnmpStrategy() {
    }
    
    @Override
    public SnmpWalker createWalker(final SnmpAgentConfig agentConfig, final String name, final CollectionTracker tracker) {
        LogUtils.debugf(this, "createWalker(%s/%d, %s, %s)", InetAddressUtils.str(agentConfig.getAddress()), agentConfig.getPort(), name, tracker.getClass().getName());
        final AgentAddress aa = new AgentAddress(agentConfig.getAddress(), agentConfig.getPort());
        return new MockSnmpWalker(aa, m_loaders.get(aa), name, tracker, agentConfig.getMaxVarsPerPdu());
    }

    @Override
    public SnmpValue set(final SnmpAgentConfig agentConfig, final SnmpObjId oid, final SnmpValue value) {
        final AgentAddress aa = new AgentAddress(agentConfig.getAddress(), agentConfig.getPort());
        if (!m_loaders.containsKey(aa)) {
            return null;
        }
        return m_loaders.get(aa).set(oid, value);
    }

    @Override
    public SnmpValue[] set(final SnmpAgentConfig agentConfig, final SnmpObjId[] oids, final SnmpValue[] values) {
        final AgentAddress aa = new AgentAddress(agentConfig.getAddress(), agentConfig.getPort());
        if (!m_loaders.containsKey(aa)) {
            return new SnmpValue[values.length];
        }
        return m_loaders.get(aa).set(oids, values);
    }

    @Override
    public SnmpValue get(final SnmpAgentConfig agentConfig, final SnmpObjId oid) {
        final AgentAddress aa = new AgentAddress(agentConfig.getAddress(), agentConfig.getPort());
        if (!m_loaders.containsKey(aa)) {
            return null;
        }

        return m_loaders.get(aa).findValueForOid(oid);
    }

    @Override
    public SnmpValue[] get(final SnmpAgentConfig agentConfig, final SnmpObjId[] oids) {
        final AgentAddress aa = new AgentAddress(agentConfig.getAddress(), agentConfig.getPort());

        final PropertyOidContainer container = m_loaders.get(aa);
        if (container == null) return new SnmpValue[oids.length];
        final List<SnmpValue> values = new ArrayList<SnmpValue>();

        for (final SnmpObjId oid : oids) {
    		values.add(container.findValueForOid(oid));
        }
        return values.toArray(EMPTY_SNMP_VALUE_ARRAY);
    }

    @Override
    public SnmpValue getNext(final SnmpAgentConfig agentConfig, final SnmpObjId oid) {
        final AgentAddress aa = new AgentAddress(agentConfig.getAddress(), agentConfig.getPort());
        if (!m_loaders.containsKey(aa)) {
            return null;
        }

        final PropertyOidContainer container = m_loaders.get(aa);
        return container.findNextValueForOid(oid);
    }

    @Override
    public SnmpValue[] getNext(final SnmpAgentConfig agentConfig, final SnmpObjId[] oids) {
        final AgentAddress aa = new AgentAddress(agentConfig.getAddress(), agentConfig.getPort());
        if (!m_loaders.containsKey(aa)) {
            return null;
        }

        final PropertyOidContainer container = m_loaders.get(aa);
        final List<SnmpValue> values = new ArrayList<SnmpValue>();

        for (final SnmpObjId oid : oids) {
            values.add(container.findNextValueForOid(oid));
        }
        return values.toArray(EMPTY_SNMP_VALUE_ARRAY);
    }

    @Override
    public SnmpValue[] getBulk(final SnmpAgentConfig agentConfig, final SnmpObjId[] oids) {
        return getNext(agentConfig, oids);
    }

    @Override
    public void registerForTraps(final TrapNotificationListener listener, final TrapProcessorFactory processorFactory, final InetAddress address, final int snmpTrapPort) throws IOException {
        LogUtils.warnf(this, "Can't register for traps.  No network in the MockSnmpStrategy!");
    }

    @Override
    public void registerForTraps(final TrapNotificationListener listener, final TrapProcessorFactory processorFactory, final int snmpTrapPort) throws IOException {
        LogUtils.warnf(this, "Can't register for traps.  No network in the MockSnmpStrategy!");
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
        return new NullSnmpV1TrapBuilder();
    }

    @Override
    public SnmpTrapBuilder getV2TrapBuilder() {
        return new NullSnmpTrapBuilder();
    }

    @Override
    public SnmpV3TrapBuilder getV3TrapBuilder() {
        return new NullSnmpV3TrapBuilder();
    }

    @Override
    public SnmpV2TrapBuilder getV2InformBuilder() {
        return new NullSnmpV2TrapBuilder();
    }

    @Override
    public SnmpV3TrapBuilder getV3InformBuilder() {
        return new NullSnmpV3TrapBuilder();
    }

    @Override
    public byte[] getLocalEngineID() {
        // lovingly stolen from SNMP4J
        final byte[] engineID = new byte[5];
        engineID[0] = (byte) (0x80 | ((s_enterpriseId >> 24) & 0xFF));
        engineID[1] = (byte) ((s_enterpriseId >> 16) & 0xFF);
        engineID[2] = (byte) ((s_enterpriseId >> 8) & 0xFF);
        engineID[3] = (byte) (s_enterpriseId & 0xFF);
        byte[] ip = new byte[0];

        try {
            ip = InetAddress.getLocalHost().getAddress();
        } catch (final UnknownHostException ex) {
            LogUtils.debugf(this, "Local host cannot be determined for creation of local engine ID");
            ip = "OpenNMS".getBytes();
        }
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
        return ArrayUtils.addAll(engineID, ip);
    }

    public static void addHost(final InetAddress agentAddress, final int agentPort, final Resource resource) throws IOException {
        final AgentAddress addr = new AgentAddress(agentAddress, agentPort);
        final PropertyOidContainer loader = new PropertyOidContainer(resource);
        m_loaders.put(addr, loader);
    }
    
    public static void removeHost(final InetAddress agentAddress, final int agentPort) {
        final AgentAddress addr = new AgentAddress(agentAddress, agentPort);
        m_loaders.remove(addr);
    }
}
