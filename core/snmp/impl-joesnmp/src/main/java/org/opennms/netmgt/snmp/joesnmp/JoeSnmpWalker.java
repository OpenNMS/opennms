/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp.joesnmp;

import java.net.SocketException;

import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.protocols.snmp.SnmpHandler;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpPduBulk;
import org.opennms.protocols.snmp.SnmpPduPacket;
import org.opennms.protocols.snmp.SnmpPduRequest;
import org.opennms.protocols.snmp.SnmpPeer;
import org.opennms.protocols.snmp.SnmpSMI;
import org.opennms.protocols.snmp.SnmpSession;
import org.opennms.protocols.snmp.SnmpSyntax;
import org.opennms.protocols.snmp.SnmpVarBind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JoeSnmpWalker extends SnmpWalker {
	
	private static final transient Logger LOG = LoggerFactory.getLogger(JoeSnmpWalker.class);
	
	static public abstract class JoeSnmpPduBuilder extends WalkerPduBuilder {
        public JoeSnmpPduBuilder(int maxVarsPerPdu) {
            super(maxVarsPerPdu);
        }

        public abstract SnmpPduPacket getPdu();

    }
    
    public static class GetNextBuilder extends JoeSnmpPduBuilder {
        private SnmpPduRequest m_nextPdu = null;

        private GetNextBuilder(int maxVarsPerPdu) {
            super(maxVarsPerPdu);
            reset();
        }
        
        public void reset() {
            m_nextPdu = new SnmpPduRequest(SnmpPduRequest.GETNEXT);
            m_nextPdu.setRequestId(SnmpPduPacket.nextSequence());
        }

        public SnmpPduPacket getPdu() {
            return m_nextPdu;
        }
        
        public void addOid(SnmpObjId snmpObjId) {
            SnmpVarBind varBind = new SnmpVarBind(new SnmpObjectId(snmpObjId.getIds()));
            m_nextPdu.addVarBind(varBind);
        }

        public void setNonRepeaters(int numNonRepeaters) {
        }

        public void setMaxRepetitions(int maxRepetitions) {
        }

    }
    
    public class GetBulkBuilder extends JoeSnmpPduBuilder {
        
        private SnmpPduBulk m_bulkPdu;

        public GetBulkBuilder(int maxVarsPerPdu) {
            super(maxVarsPerPdu);
            reset();
        }
        
        public void reset() {
            m_bulkPdu = new SnmpPduBulk();
            m_bulkPdu.setRequestId(SnmpPduPacket.nextSequence());
        }

        public SnmpPduPacket getPdu() {
            return m_bulkPdu;
        }

        public void addOid(SnmpObjId snmpObjId) {
            SnmpVarBind varBind = new SnmpVarBind(new SnmpObjectId(snmpObjId.getIds()));
            m_bulkPdu.addVarBind(varBind);
        }

        public void setNonRepeaters(int numNonRepeaters) {
            m_bulkPdu.setNonRepeaters(numNonRepeaters);
        }

        public void setMaxRepetitions(int maxRepetitions) {
            m_bulkPdu.setMaxRepititions(maxRepetitions);
        }
        
    }
    
    public class JoeSnmpResponseHandler implements SnmpHandler {

        public void snmpReceivedPdu(SnmpSession session, int command, SnmpPduPacket pdu) {
            
            try {
                SnmpPduRequest response = (SnmpPduRequest)pdu;
                LOG.debug("Received a tracker pdu from "+getAddress()+" of size "+pdu.getLength()+" errorStatus = "+response.getErrorStatus()+", errorIndex = "+response.getErrorIndex());
                if (!processErrors(response.getErrorStatus(), response.getErrorIndex())) {
                    for(int i = 0; i < response.getLength(); i++) {
                        SnmpVarBind vb = response.getVarBindAt(i);
                        SnmpObjId receivedOid = SnmpObjId.get(vb.getName().getIdentifiers());
                        SnmpValue val = new JoeSnmpValue(vb.getValue());
                        processResponse(receivedOid, val);
                    }
                }
                buildAndSendNextPdu();
            } catch (Throwable e) {
                handleFatalError(e);
            }
        }

        public void snmpInternalError(SnmpSession session, int err, SnmpSyntax pdu) {
            handleError(getName()+": snmpInternalError: " + err + " for: " + getAddress());
        }

        public void snmpTimeoutError(SnmpSession session, SnmpSyntax pdu) {
            handleTimeout(getName()+": snmpTimeoutError for: " + getAddress());
        }
        
    }



    private JoeSnmpResponseHandler m_handler;
    private SnmpPeer m_peer;
    private SnmpSession m_session;
	private JoeSnmpAgentConfig m_agentConfig;

    public JoeSnmpWalker(JoeSnmpAgentConfig agentConfig, String name, CollectionTracker tracker) {
        super(agentConfig.getAddress(), name, agentConfig.getMaxVarsPerPdu(), agentConfig.getMaxRepetitions(), tracker);
        m_agentConfig = agentConfig;
        m_peer = getPeer(agentConfig);
        m_handler = new JoeSnmpResponseHandler();
    }
    
    private SnmpPeer getPeer(JoeSnmpAgentConfig agentConfig) {
        SnmpPeer peer = new SnmpPeer(agentConfig.getAddress());
        peer.getParameters().setVersion(agentConfig.getVersion());
        peer.getParameters().setReadCommunity(agentConfig.getReadCommunity());
        peer.getParameters().setVersion(agentConfig.getVersion());
        peer.setPort(agentConfig.getPort());
        peer.setRetries(agentConfig.getRetries());
        peer.setTimeout(agentConfig.getTimeout());
        return peer;        
    }

    public void start() {
        LOG.info("Walking "+getName()+" for "+getAddress()+" using version "+SnmpSMI.getVersionString(getVersion())+" with config: "+m_agentConfig);
        super.start();
    }

    protected WalkerPduBuilder createPduBuilder(int maxVarsPerPdu) {
        return (getVersion() == SnmpSMI.SNMPV1 
                ? (JoeSnmpPduBuilder)new GetNextBuilder(maxVarsPerPdu) 
                : (JoeSnmpPduBuilder)new GetBulkBuilder(maxVarsPerPdu));
    }

    protected void sendNextPdu(WalkerPduBuilder pduBuilder) throws SocketException {
        JoeSnmpPduBuilder joePduBuilder = (JoeSnmpPduBuilder)pduBuilder;
        if (m_session == null) m_session = new SnmpSession(m_peer);
        LOG.debug("Sending tracker pdu of size "+joePduBuilder.getPdu().getLength());
        m_session.send(joePduBuilder.getPdu(), m_handler);
    }
    
    protected int getVersion() {
        return m_peer.getParameters().getVersion();
    }

    protected void close() {
        if (m_session != null) {
            m_session.close();
            m_session = null;
        }
    }
}
