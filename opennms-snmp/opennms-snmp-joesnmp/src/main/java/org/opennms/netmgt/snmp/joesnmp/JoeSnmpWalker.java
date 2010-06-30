//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jun 23: Organize imports. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.snmp.joesnmp;

import java.net.SocketException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
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

/**
 * <p>JoeSnmpWalker class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class JoeSnmpWalker extends SnmpWalker {
    
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
                log().debug("Received a tracker pdu from "+getAddress()+" of size "+pdu.getLength()+" errorStatus = "+response.getErrorStatus()+", errorIndex = "+response.getErrorIndex());
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

    /**
     * <p>Constructor for JoeSnmpWalker.</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.joesnmp.JoeSnmpAgentConfig} object.
     * @param name a {@link java.lang.String} object.
     * @param tracker a {@link org.opennms.netmgt.snmp.CollectionTracker} object.
     */
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

    /**
     * <p>start</p>
     */
    public void start() {
        log().info("Walking "+getName()+" for "+getAddress()+" using version "+SnmpSMI.getVersionString(getVersion())+" with config: "+m_agentConfig);
        super.start();
    }

    /** {@inheritDoc} */
    protected WalkerPduBuilder createPduBuilder(int maxVarsPerPdu) {
        return (getVersion() == SnmpSMI.SNMPV1 
                ? (JoeSnmpPduBuilder)new GetNextBuilder(maxVarsPerPdu) 
                : (JoeSnmpPduBuilder)new GetBulkBuilder(maxVarsPerPdu));
    }

    /**
     * <p>sendNextPdu</p>
     *
     * @param pduBuilder a WalkerPduBuilder object.
     * @throws java.net.SocketException if any.
     */
    protected void sendNextPdu(WalkerPduBuilder pduBuilder) throws SocketException {
        JoeSnmpPduBuilder joePduBuilder = (JoeSnmpPduBuilder)pduBuilder;
        if (m_session == null) m_session = new SnmpSession(m_peer);
        log().debug("Sending tracker pdu of size "+joePduBuilder.getPdu().getLength());
        m_session.send(joePduBuilder.getPdu(), m_handler);
    }
    
    /**
     * <p>getVersion</p>
     *
     * @return a int.
     */
    protected int getVersion() {
        return m_peer.getParameters().getVersion();
    }

    /**
     * <p>close</p>
     */
    protected void close() {
        if (m_session != null) {
            m_session.close();
            m_session = null;
        }
    }
    
    private final Category log() {
        return ThreadCategory.getInstance(getClass());
    }


}
