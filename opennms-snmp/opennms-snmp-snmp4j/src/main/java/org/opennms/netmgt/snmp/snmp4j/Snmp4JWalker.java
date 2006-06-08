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
package org.opennms.netmgt.snmp.snmp4j;

import java.io.IOException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;

public class Snmp4JWalker extends SnmpWalker {
    
    static public abstract class Snmp4JPduBuilder extends WalkerPduBuilder {
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
        
        public void reset() {
            m_nextPdu = SnmpHelpers.createPDU(getVersion());
            m_nextPdu.setType(PDU.GETNEXT);
        }

        public PDU getPdu() {
            return m_nextPdu;
        }
        
        public void addOid(SnmpObjId snmpObjId) {
            VariableBinding varBind = new VariableBinding(new OID(snmpObjId.getIds()));
            m_nextPdu.add(varBind);
        }

        public void setNonRepeaters(int numNonRepeaters) {
        }

        public void setMaxRepetitions(int maxRepititions) {
        }
        
    }
    
    public class GetBulkBuilder extends Snmp4JPduBuilder {

        private PDU m_bulkPdu;

        public GetBulkBuilder(int maxVarsPerPdu) {
            super(maxVarsPerPdu);
            reset();
        }
        
        public void reset() {
            m_bulkPdu = SnmpHelpers.createPDU(getVersion());
            m_bulkPdu.setType(PDU.GETBULK);
        }

        public PDU getPdu() {
            return m_bulkPdu;
        }

        public void addOid(SnmpObjId snmpObjId) {
            VariableBinding varBind = new VariableBinding(new OID(snmpObjId.getIds()));
            m_bulkPdu.add(varBind);
        }

        public void setNonRepeaters(int numNonRepeaters) {
            m_bulkPdu.setNonRepeaters(numNonRepeaters);
        }

        public void setMaxRepetitions(int maxRepetitions) {
            m_bulkPdu.setMaxRepetitions(maxRepetitions);
        }
        
    }
    
    public class Snmp4JResponseListener implements ResponseListener {

        public void processResponse(PDU response) {
            
            try {
                log().debug("Received a tracker pdu of type "+PDU.getTypeString(response.getType())+" from "+getAddress()+" of size "+response.size()+" errorStatus = "+response.getErrorStatusText()+" errorIndex = "+response.getErrorIndex());
                if (response.getType() != PDU.REPORT) {
                    if (!processErrors(response.getErrorStatus(), response.getErrorIndex())) {
                        for(int i = 0; i < response.size(); i++) {
                            VariableBinding vb = response.get(i);
                            SnmpObjId receivedOid = SnmpObjId.get(vb.getOid().getValue());
                            SnmpValue val = new Snmp4JValue(vb.getVariable());
                            Snmp4JWalker.this.processResponse(receivedOid, val);
                        }
                    }
                    buildAndSendNextPdu();
                } else {
                    handleAuthError("A REPORT Pdu was returned from the agent.  This is most likely an authentication problem.  Please check the config");
                }
            } catch (Throwable e) {
                handleFatalError(e);
            }
        }

        public void onResponse(ResponseEvent responseEvent) {
            if (responseEvent.getError() instanceof InterruptedException) {
                log().debug("Interruption event.  We have probably tried to close the session due to an error");
            } 
            else if (responseEvent.getResponse() == null) {
                handleTimeout(getName()+": snmpTimeoutError for: " + getAddress());
            } else if (responseEvent.getError() != null){
                handleError(getName()+": snmpInternalError: " + responseEvent.getError() + " for: " + getAddress());
            } else {
                processResponse(responseEvent.getResponse());
            }
            
        }
        
        
    }
    
    private Snmp m_session;
    private Target m_tgt;
    private ResponseListener m_listener;
    private Snmp4JAgentConfig m_agentConfig;

    public Snmp4JWalker(Snmp4JAgentConfig agentConfig, String name, CollectionTracker tracker) {
        super(agentConfig.getInetAddress(), name, agentConfig.getMaxVarsPerPdu(), tracker);
        
        m_agentConfig = agentConfig;
        
        m_tgt = agentConfig.getTarget();
        m_listener = new Snmp4JResponseListener();
    }
    
    public void start() {
        
        if (log().isDebugEnabled())
            log().debug("Walking "+getName()+" for "+getAddress()+" using version "+SnmpHelpers.versionString(getVersion())+" with config: "+m_agentConfig);
            
        super.start();
    }

    protected WalkerPduBuilder createPduBuilder(int maxVarsPerPdu) {
        return (getVersion() == SnmpConstants.version1 
                ? (WalkerPduBuilder)new GetNextBuilder(maxVarsPerPdu) 
                : (WalkerPduBuilder)new GetBulkBuilder(maxVarsPerPdu));
    }

    protected void sendNextPdu(WalkerPduBuilder pduBuilder) throws IOException {
        Snmp4JPduBuilder snmp4JPduBuilder = (Snmp4JPduBuilder)pduBuilder;
        if (m_session == null) {
            m_session = SnmpHelpers.createSnmpSession(m_agentConfig);
            m_session.listen();
        }
        
        log().debug("Sending tracker pdu of size "+snmp4JPduBuilder.getPdu().size());
        m_session.send(snmp4JPduBuilder.getPdu(), m_tgt, null, m_listener);
    }
    
    protected int getVersion() {
        return m_tgt.getVersion();
    }

    protected void close() throws IOException {
        if (m_session != null) {
            m_session.close();
            m_session = null;
        }
    }

    private final Category log() {
        return ThreadCategory.getInstance(getClass());
    }

}
