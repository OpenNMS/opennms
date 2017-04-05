/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp.snmp4j;

import java.io.IOException;

import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;

public class Snmp4JWalker extends SnmpWalker {
	
	private static final transient Logger LOG = LoggerFactory.getLogger(Snmp4JWalker.class);
	
	public static abstract class Snmp4JPduBuilder extends WalkerPduBuilder {
        public Snmp4JPduBuilder(int maxVarsPerPdu) {
            super(maxVarsPerPdu);
        }
        
        public abstract PDU getPdu();
    }
    
    public class GetNextBuilder extends Snmp4JPduBuilder {

        private PDU m_nextPdu = null;

        private GetNextBuilder(int maxVarsPerPdu) {
            super(maxVarsPerPdu);
            reset();
        }
        
        @Override
        public void reset() {
            m_nextPdu = m_agentConfig.createPdu(PDU.GETNEXT);
        }

        @Override
        public PDU getPdu() {
            return m_nextPdu;
        }
        
        @Override
        public void addOid(SnmpObjId snmpObjId) {
            VariableBinding varBind = new VariableBinding(new OID(snmpObjId.getIds()));
            m_nextPdu.add(varBind);
        }

        @Override
        public void setNonRepeaters(int numNonRepeaters) {
        }

        @Override
        public void setMaxRepetitions(int maxRepititions) {
        }
        
    }
    
    public class GetBulkBuilder extends Snmp4JPduBuilder {

        private PDU m_bulkPdu;

        public GetBulkBuilder(int maxVarsPerPdu) {
            super(maxVarsPerPdu);
            reset();
        }
        
        @Override
        public void reset() {
            m_bulkPdu = m_agentConfig.createPdu(PDU.GETBULK);
        }

        @Override
        public PDU getPdu() {
            return m_bulkPdu;
        }

        @Override
        public void addOid(SnmpObjId snmpObjId) {
            VariableBinding varBind = new VariableBinding(new OID(snmpObjId.getIds()));
            m_bulkPdu.add(varBind);
        }

        @Override
        public void setNonRepeaters(int numNonRepeaters) {
            m_bulkPdu.setNonRepeaters(numNonRepeaters);
        }

        @Override
        public void setMaxRepetitions(int maxRepetitions) {
            m_bulkPdu.setMaxRepetitions(maxRepetitions);
        }
        
    }

    /**
     * TODO: Merge this logic with {@link org.opennms.netmgt.snmp.snmp4j.Snmp4JStrategy} processResponse()
     */
    public class Snmp4JResponseListener implements ResponseListener {

        private void processResponse(final PDU response) {
            try {
                LOG.debug("Received a tracker PDU of type {} from {} of size {}, errorStatus = {}, errorStatusText = {}, errorIndex = {}", PDU.getTypeString(response.getType()), getAddress(), response.size(), response.getErrorStatus(), response.getErrorStatusText(), response.getErrorIndex());
                if (response.getType() == PDU.REPORT) {
                    handleAuthError("A REPORT PDU was returned from the agent.  This is most likely an authentication problem.  Please check the config");
                } else {
                    if (!processErrors(response.getErrorStatus(), response.getErrorIndex())) {
                        if (response.size() == 0) { // NMS-6484
                            handleError("A PDU with no errors and 0 varbinds was returned from the agent at " + getAddress() + ". This seems to be related with a broken SNMP agent.");
                        } else {
                            for (int i = 0; i < response.size(); i++) {
                                final VariableBinding vb = response.get(i);
                                final SnmpObjId receivedOid = SnmpObjId.get(vb.getOid().getValue());
                                final SnmpValue val = new Snmp4JValue(vb.getVariable());
                                Snmp4JWalker.this.processResponse(receivedOid, val);
                            }
                        }
                    }
                    buildAndSendNextPdu();
                }
            } catch (final IOException e) {
                handleFatalError(e);
            } catch (final RuntimeException e) {
                handleFatalError(e);
            }
        }

        @Override
        public void onResponse(ResponseEvent responseEvent) {
            // need to cancel the request here otherwise SNMP4J Keeps it around forever... go figure
            m_session.cancel(responseEvent.getRequest(), this);

            // Check to see if we got an interrupted exception
            if (responseEvent.getError() instanceof InterruptedException) {
                LOG.debug("Interruption event.  We have probably tried to close the session due to an error", responseEvent.getError());
            // Check to see if the response is null, indicating a timeout
            } else if (responseEvent.getResponse() == null) {
                handleTimeout(getName()+": snmpTimeoutError for: " + getAddress());
            // Check to see if we got any kind of error
            } else if (responseEvent.getError() != null){
                handleError(getName()+": snmpInternalError: " + responseEvent.getError() + " for: " + getAddress(), responseEvent.getError());
            // If we have a PDU in the response, process it
            } else {
                processResponse(responseEvent.getResponse());
            }
            
        }
        
        
    }
    
    private Snmp m_session;
    private final Target m_tgt;
    private final ResponseListener m_listener;
    private final Snmp4JAgentConfig m_agentConfig;

    public Snmp4JWalker(Snmp4JAgentConfig agentConfig, String name, CollectionTracker tracker) {
        super(agentConfig.getInetAddress(), name, agentConfig.getMaxVarsPerPdu(), agentConfig.getMaxRepetitions(), agentConfig.getRetries(), tracker);
        
        m_agentConfig = agentConfig;
        
        m_tgt = agentConfig.getTarget();
        m_listener = new Snmp4JResponseListener();
    }
    
        @Override
    public void start() {
        
        LOG.debug("Walking {} for {} using version {} with config: {}", getName(), getAddress(), m_agentConfig.getVersionString(), m_agentConfig);
            
        super.start();
    }

        @Override
    protected WalkerPduBuilder createPduBuilder(int maxVarsPerPdu) {
        return (getVersion() == SnmpConstants.version1 
                ? (WalkerPduBuilder)new GetNextBuilder(maxVarsPerPdu) 
                : (WalkerPduBuilder)new GetBulkBuilder(maxVarsPerPdu));
    }

        @Override
    protected void sendNextPdu(WalkerPduBuilder pduBuilder) throws IOException {
        Snmp4JPduBuilder snmp4JPduBuilder = (Snmp4JPduBuilder)pduBuilder;
        if (m_session == null) {
            m_session = m_agentConfig.createSnmpSession();
            m_session.listen();
        }
        
        LOG.debug("Sending tracker pdu of size {}", snmp4JPduBuilder.getPdu().size());
        m_session.send(snmp4JPduBuilder.getPdu(), m_tgt, null, m_listener);
    }
    
    protected int getVersion() {
        return m_tgt.getVersion();
    }

    @Override
    public void close() throws IOException {
        if (m_session != null) {
            m_session.close();
            m_session = null;
        }
    }
}
