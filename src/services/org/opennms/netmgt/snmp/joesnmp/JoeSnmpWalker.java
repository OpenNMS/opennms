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
package org.opennms.netmgt.snmp.joesnmp;

import java.net.InetAddress;
import java.net.SocketException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.protocols.snmp.SnmpCounter64;
import org.opennms.protocols.snmp.SnmpEndOfMibView;
import org.opennms.protocols.snmp.SnmpHandler;
import org.opennms.protocols.snmp.SnmpIPAddress;
import org.opennms.protocols.snmp.SnmpInt32;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpPduBulk;
import org.opennms.protocols.snmp.SnmpPduPacket;
import org.opennms.protocols.snmp.SnmpPduRequest;
import org.opennms.protocols.snmp.SnmpPeer;
import org.opennms.protocols.snmp.SnmpSMI;
import org.opennms.protocols.snmp.SnmpSession;
import org.opennms.protocols.snmp.SnmpSyntax;
import org.opennms.protocols.snmp.SnmpUInt32;
import org.opennms.protocols.snmp.SnmpVarBind;

public class JoeSnmpWalker extends SnmpWalker {
    
    private static class JoeSnmpValue implements SnmpValue {
        SnmpSyntax m_value;
        
        JoeSnmpValue(SnmpSyntax value) {
            m_value = value;
        }

        public boolean isEndOfMib() {
            return m_value instanceof SnmpEndOfMibView;
        }

        public boolean isNumeric() {
            switch (m_value.typeId()) {
            case SnmpSMI.SMI_INTEGER:
            case SnmpSMI.SMI_COUNTER32:
            case SnmpSMI.SMI_COUNTER64:
            case SnmpSMI.SMI_TIMETICKS:
            case SnmpSMI.SMI_UNSIGNED32:
                return true;
            default:
                return false;
            }
        }
        
        public int toInt() {
            switch (m_value.typeId()) {
            case SnmpSMI.SMI_COUNTER64:
                return ((SnmpCounter64)m_value).getValue().intValue();
            case SnmpSMI.SMI_INTEGER:
                return ((SnmpInt32)m_value).getValue();
            case SnmpSMI.SMI_COUNTER32:
            case SnmpSMI.SMI_TIMETICKS:
            case SnmpSMI.SMI_UNSIGNED32:
                return (int)((SnmpUInt32)m_value).getValue();
            default:
                return Integer.parseInt(m_value.toString());
            }
        }
        
        public long toLong() {
            switch (m_value.typeId()) {
            case SnmpSMI.SMI_COUNTER64:
                return ((SnmpCounter64)m_value).getValue().longValue();
            case SnmpSMI.SMI_INTEGER:
                return ((SnmpInt32)m_value).getValue();
            case SnmpSMI.SMI_COUNTER32:
            case SnmpSMI.SMI_TIMETICKS:
            case SnmpSMI.SMI_UNSIGNED32:
                return ((SnmpUInt32)m_value).getValue();
            default:
                return Long.parseLong(m_value.toString());
            }
        }

        public String toDisplayString() {
            return m_value.toString();
        }

        public InetAddress toInetAddress() {
            switch (m_value.typeId()) {
                case SnmpSMI.SMI_IPADDRESS:
                    return SnmpIPAddress.toInetAddress((SnmpIPAddress)m_value);
                default:
                    throw new IllegalArgumentException("cannot convert "+m_value+" to an InetAddress"); 
            }
        }

        public String toHexString() {
            // TODO Auto-generated method stub
            return null;
        }
        
        public String toString() {
            return toDisplayString();
        }
    }
    
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
                log().debug("Received a tracker pdu from "+getAddress()+" of size "+pdu.getLength());
                SnmpPduRequest response = (SnmpPduRequest)pdu;
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

    public JoeSnmpWalker(InetAddress address, String name, int maxVarsPerPdu, CollectionTracker tracker) {
        super(address, name, maxVarsPerPdu, tracker);
        m_peer = SnmpPeerFactory.getInstance().getPeer(address);
        m_handler = new JoeSnmpResponseHandler();
    }
    
    public void start() {
        log().debug("Walking "+getName()+" for "+getAddress()+" using version "+SnmpSMI.getVersionString(getVersion()));
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
        log().debug("Sending tracker pdu of size "+joePduBuilder.getPdu().getLength());
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
    
    private final Category log() {
        return ThreadCategory.getInstance(getClass());
    }


}
