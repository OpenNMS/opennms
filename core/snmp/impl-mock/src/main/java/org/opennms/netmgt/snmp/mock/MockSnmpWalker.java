package org.opennms.netmgt.snmp.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.netmgt.snmp.mock.MockSnmpStrategy.AgentAddress;

public class MockSnmpWalker extends SnmpWalker {

    public static final class MockPduBuilder extends WalkerPduBuilder {
        private List<SnmpObjId> m_oids = new ArrayList<SnmpObjId>();

        public MockPduBuilder(final int maxVarsPerPdu) {
            super(maxVarsPerPdu);
            reset();
        }

        @Override
        public void reset() {
            m_oids.clear();
        }

        public List<SnmpObjId> getOids() {
            return new ArrayList<SnmpObjId>(m_oids);
        }

        @Override
        public void addOid(final SnmpObjId snmpObjId) {
            m_oids.add(snmpObjId);
        }

        @Override
        public void setNonRepeaters(final int numNonRepeaters) {
        }

        @Override
        public void setMaxRepetitions(final int maxRepetitions) {
        }
    }

    private PropertyOidContainer m_container;
    private ExecutorService m_executor;

    public MockSnmpWalker(final AgentAddress agentAddress, final PropertyOidContainer container, final String name, final CollectionTracker tracker, int maxVarsPerPdu) {
        super(agentAddress.getAddress(), name, maxVarsPerPdu, 1, tracker);
        m_container = container;
        m_executor = Executors.newSingleThreadExecutor();
    }

    @Override
    protected WalkerPduBuilder createPduBuilder(final int maxVarsPerPdu) {
        return new MockPduBuilder(maxVarsPerPdu);
    }

    @Override
    protected void sendNextPdu(final WalkerPduBuilder pduBuilder) throws IOException {
        final MockPduBuilder builder = (MockPduBuilder)pduBuilder;
        final List<SnmpObjId> oids = builder.getOids();
        LogUtils.debugf(this, "'Sending' tracker PDU of size " + oids.size());

        final Map<SnmpObjId,SnmpValue> responses = new LinkedHashMap<SnmpObjId,SnmpValue>();
        for (final SnmpObjId oid : oids) {
            responses.put(m_container.findNextOidForOid(oid), m_container.findNextValueForOid(oid));
        }

        m_executor.submit(new Runnable() {
            @Override
            public void run() {
                handleResponses(responses);
            }
        });
    }

    protected void handleResponses(final Map<SnmpObjId, SnmpValue> responses) {
        try {
            if (!processErrors(0, 0)) {
                for (final Map.Entry<SnmpObjId,SnmpValue> entry : responses.entrySet()) {
                    processResponse(entry.getKey(), entry.getValue());
                }
            }
            buildAndSendNextPdu();
        } catch (final Throwable e) {
            handleFatalError(e);
        }
    }

    @Override
    protected void close() throws IOException {
        m_executor.shutdown();
    }

}
