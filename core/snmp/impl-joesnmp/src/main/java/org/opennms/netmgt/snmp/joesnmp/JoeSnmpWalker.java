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
package org.opennms.netmgt.snmp.joesnmp;

import java.net.SocketException;

import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpException;
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
        
        @Override
        public void reset() {
            m_nextPdu = new SnmpPduRequest(SnmpPduRequest.GETNEXT);
            m_nextPdu.setRequestId(SnmpPduPacket.nextSequence());
        }

        @Override
        public SnmpPduPacket getPdu() {
            return m_nextPdu;
        }
        
        @Override
        public void addOid(SnmpObjId snmpObjId) {
            SnmpVarBind varBind = new SnmpVarBind(new SnmpObjectId(snmpObjId.getIds()));
            m_nextPdu.addVarBind(varBind);
        }

        @Override
        public void setNonRepeaters(int numNonRepeaters) {
        }

        @Override
        public void setMaxRepetitions(int maxRepetitions) {
        }

    }
    
    public class GetBulkBuilder extends JoeSnmpPduBuilder {
        
        private SnmpPduBulk m_bulkPdu;

        public GetBulkBuilder(int maxVarsPerPdu) {
            super(maxVarsPerPdu);
            reset();
        }
        
        @Override
        public void reset() {
            m_bulkPdu = new SnmpPduBulk();
            m_bulkPdu.setRequestId(SnmpPduPacket.nextSequence());
        }

        @Override
        public SnmpPduPacket getPdu() {
            return m_bulkPdu;
        }

        @Override
        public void addOid(SnmpObjId snmpObjId) {
            SnmpVarBind varBind = new SnmpVarBind(new SnmpObjectId(snmpObjId.getIds()));
            m_bulkPdu.addVarBind(varBind);
        }

        @Override
        public void setNonRepeaters(int numNonRepeaters) {
            m_bulkPdu.setNonRepeaters(numNonRepeaters);
        }

        @Override
        public void setMaxRepetitions(int maxRepetitions) {
            m_bulkPdu.setMaxRepititions(maxRepetitions);
        }
        
    }
    
    public class JoeSnmpResponseHandler implements SnmpHandler {

        @Override
        public void snmpReceivedPdu(SnmpSession session, int command, SnmpPduPacket pdu) {
            
            try {
                SnmpPduRequest response = (SnmpPduRequest)pdu;
                LOG.debug("Received a tracker pdu from {} of size {} errorStatus = {}, errorIndex = {}", getAddress(), pdu.getLength(), response.getErrorStatus(), response.getErrorIndex());
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

        @Override
        public void snmpInternalError(SnmpSession session, int err, SnmpSyntax pdu) {
            handleError(getName()+": snmpInternalError: " + err + " for: " + getAddress());
        }

        @Override
        public void snmpTimeoutError(SnmpSession session, SnmpSyntax pdu) {
            handleTimeout(getName()+": snmpTimeoutError for: " + getAddress());
        }
        
    }



    private JoeSnmpResponseHandler m_handler;
    private SnmpPeer m_peer;
    private SnmpSession m_session;
    private JoeSnmpAgentConfig m_agentConfig;

    public JoeSnmpWalker(JoeSnmpAgentConfig agentConfig, String name, CollectionTracker tracker) {
        super(agentConfig.getAddress(), name, agentConfig.getMaxVarsPerPdu(), agentConfig.getMaxRepetitions(), agentConfig.getRetries(), tracker);
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

        @Override
    public void start() {
        LOG.debug("Walking {} for {} using version {} with config: {}", getName(), getAddress(), SnmpSMI.getVersionString(getVersion()), m_agentConfig);
        super.start();
    }

        @Override
    protected WalkerPduBuilder createPduBuilder(int maxVarsPerPdu) {
        return (getVersion() == SnmpSMI.SNMPV1 
                ? (JoeSnmpPduBuilder)new GetNextBuilder(maxVarsPerPdu) 
                : (JoeSnmpPduBuilder)new GetBulkBuilder(maxVarsPerPdu));
    }

        @Override
    protected void sendNextPdu(WalkerPduBuilder pduBuilder) throws SnmpException {
        JoeSnmpPduBuilder joePduBuilder = (JoeSnmpPduBuilder)pduBuilder;
        if (m_session == null)
            try {
                m_session = new SnmpSession(m_peer);
            } catch (final SocketException e) {
                throw new SnmpException("Failed to create session to peer " + m_peer.toString(), e);
            }
        LOG.debug("Sending tracker pdu of size {}", joePduBuilder.getPdu().getLength());
        m_session.send(joePduBuilder.getPdu(), m_handler);
    }
    
    protected int getVersion() {
        return m_peer.getParameters().getVersion();
    }

    @Override
    public void close() {
        if (m_session != null) {
            m_session.close();
            m_session = null;
        }
    }
}
