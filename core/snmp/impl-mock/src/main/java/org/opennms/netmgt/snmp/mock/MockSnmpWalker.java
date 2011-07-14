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
import org.opennms.netmgt.snmp.SnmpAgentAddress;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpWalker;

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

	private final SnmpAgentAddress m_agentAddress;
    private final PropertyOidContainer m_container;
    private final ExecutorService m_executor;

    public MockSnmpWalker(final SnmpAgentAddress agentAddress, final PropertyOidContainer container, final String name, final CollectionTracker tracker, int maxVarsPerPdu) {
        super(agentAddress.getAddress(), name, maxVarsPerPdu, 1, tracker);
        m_agentAddress = agentAddress;
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

        m_executor.submit(new ResponseHandler(oids));
    }

    @Override
    protected void handleDone() {
    	LogUtils.debugf(this, "handleDone()");
    	super.handleDone();
    }

    @Override
    protected void handleAuthError(final String msg) {
    	LogUtils.debugf(this, "handleAuthError(%s)", msg);
    	super.handleAuthError(msg);
    }
    
    @Override
    protected void handleError(final String msg) {
    	LogUtils.debugf(this, "handleError(%s)", msg);
    	super.handleError(msg);
    }
    
    @Override
    protected void handleError(final String msg, final Throwable t) {
    	LogUtils.debugf(this, t, "handleError(%s, %s)", msg, t.getLocalizedMessage());
    	super.handleError(msg, t);
    }
    
    @Override
    protected void handleFatalError(final Throwable e) {
    	LogUtils.debugf(this, e, "handleFatalError(%s)", e.getLocalizedMessage());
    	super.handleFatalError(e);
    }

    @Override
    protected void handleTimeout(final String msg) {
    	LogUtils.debugf(this, "handleTimeout(%s)", msg);
    	super.handleTimeout(msg);
    }

    @Override
    protected void close() throws IOException {
        m_executor.shutdown();
    }

    @Override
    protected void buildAndSendNextPdu() throws IOException {
    	LogUtils.debugf(this, "buildAndSendNextPdu()");
    	super.buildAndSendNextPdu();
    }

    private final class ResponseHandler implements Runnable {
		private final List<SnmpObjId> m_oids;

		private ResponseHandler(final List<SnmpObjId> oids) {
			m_oids = oids;
		}

		@Override
		public void run() {
		    handleResponses();
		}

	    protected void handleResponses() {
	    	LogUtils.debugf(this, "handleResponses(%s)", m_oids);
	        try {
	            if (m_container == null) {
	            	LogUtils.infof(this, "No SNMP response data configured for %s; pretending we've timed out.", m_agentAddress);
	            	Thread.sleep(100);
	            	handleTimeout("No MockSnmpAgent data configured for '" + m_agentAddress + "'.");
	            	return;
	            }

	            final Map<SnmpObjId,SnmpValue> responses = new LinkedHashMap<SnmpObjId,SnmpValue>();
	            for (final SnmpObjId oid : m_oids) {
	                responses.put(m_container.findNextOidForOid(oid), m_container.findNextValueForOid(oid));
	            }

	            if (processErrors(0, 0)) {
	            	LogUtils.debugf(this, "Errors while handling responses... Whaaaat?");
	            } else {
	            	LogUtils.debugf(this, "Handling %d responses.", responses.size());
	                for (final Map.Entry<SnmpObjId,SnmpValue> entry : responses.entrySet()) {
	                	processResponse(entry.getKey(), entry.getValue());
	                }
	            }
				buildAndSendNextPdu();
				/*
	            m_executor.submit(new Runnable() {
					@Override
					public void run() {
			            try {
							buildAndSendNextPdu();
						} catch (final Exception e) {
							LogUtils.debugf(this, e, "Failed to build and send next PDU.");
				            handleFatalError(e);
						}
					}
	            });
	            */
	        } catch (final Throwable t) {
	            handleFatalError(t);
	        }
	    }
    }
}
