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
package org.opennms.netmgt.capsd.snmp;

import java.net.InetAddress;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.PduBuilder;
import org.opennms.netmgt.snmp.ResponseProcessor;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.utils.Signaler;
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

public class SnmpWalker {
    
    private static abstract class JoeSnmpPduBuilder extends PduBuilder {
        public JoeSnmpPduBuilder(int maxVarsPerPdu) {
            super(maxVarsPerPdu);
        }

        public abstract SnmpPduPacket getPdu();

        public abstract void reset();
    }
    
    private static class GetNextBuilder extends JoeSnmpPduBuilder {
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

        public void setMaxRepititions(int maxRepititions) {
        }

    }
    
    private class GetBulkBuilder extends JoeSnmpPduBuilder {
        
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

        public void setMaxRepititions(int maxRepititions) {
            m_bulkPdu.setMaxRepititions(maxRepititions);
        }
        
    }
    
    private class JoeSnmpResponseHandler implements SnmpHandler {

        public void snmpReceivedPdu(SnmpSession session, int command, SnmpPduPacket pdu) {
            
            try {
                log().debug("Received a tracker pdu from "+m_address+" of size "+pdu.getLength());
                SnmpPduRequest response = (SnmpPduRequest)pdu;
                if (!m_responseProcessor.processErrors(response.getErrorStatus(), response.getErrorIndex())) {
                    for(int i = 0; i < response.getLength(); i++) {
                        SnmpVarBind vb = response.getVarBindAt(i);
                        SnmpObjId receivedOid = SnmpObjId.get(vb.getName().getIdentifiers());
                        Object val = vb.getValue();
                        m_responseProcessor.processResponse(receivedOid, val);
                    }
                }
                if (m_tracker.isFinished())
                    handleDone();
                else {
                    m_pduBuilder.reset();
                    m_responseProcessor = m_tracker.buildNextPdu(m_pduBuilder);
                    log().debug("Sending tracker pdu to "+m_address+" of size "+m_pduBuilder.getPdu().getLength());
                    session.send(m_pduBuilder.getPdu(), m_handler);
                }
            } catch (Throwable e) {
                handleFatalError(e);
            }
        }

        public void snmpInternalError(SnmpSession session, int err, SnmpSyntax pdu) {
            handleError(getName()+": snmpInternalError: " + err + " for: " + m_address);
        }

        public void snmpTimeoutError(SnmpSession session, SnmpSyntax pdu) {
            handleError(getName()+": snmpTimeoutError for: " + session.getPeer().getPeer());
        }
        
    }



    private String m_name;
    private CollectionTracker m_tracker;

    /**
     * <P>
     * Flag indicating if query was successful.
     * </P>
     */
    private boolean m_error;
    /**
     * <P>
     * Used to synchronize the class to ensure that the session has finished
     * collecting data before the value of success or failure is set, and
     * control is returned to the caller.
     * </P>
     */
    private Signaler m_signal;

    private InetAddress m_address;
    private JoeSnmpPduBuilder m_pduBuilder;
    private ResponseProcessor m_responseProcessor;
    private JoeSnmpResponseHandler m_handler;
    private SnmpPeer m_peer;
    private SnmpSession m_session;

    public SnmpWalker(final InetAddress address, Signaler signal, String name, CollectionTracker[] trackers) {
        this(address, signal, name, new AggregateTracker(trackers) {
            protected void reportTooBigErr(String msg) {
                log().info("Received tooBig response from "+address+". "+msg);
            }
        });
    }
    
    public SnmpWalker(InetAddress address, Signaler signal, String name, CollectionTracker tracker) {
        m_address = address;
        m_signal = signal;
        
        m_peer = SnmpPeerFactory.getInstance().getPeer(m_address);

        m_name = name;

        m_error = false;
        
        m_tracker = tracker;
        
        m_pduBuilder = (getVersion() == SnmpSMI.SNMPV1 
                ? (JoeSnmpPduBuilder)new GetNextBuilder(50) 
                : (JoeSnmpPduBuilder)new GetBulkBuilder(50));
        
        m_handler = new JoeSnmpResponseHandler();

    }
    
    public void start() {
        try {
            log().debug("Walking "+getName()+" for "+m_address+" using version "+SnmpSMI.getVersionString(getVersion()));
            
            
            if (m_tracker.isFinished())
                handleDone();
            else {
                m_session = new SnmpSession(m_peer);
                m_responseProcessor = m_tracker.buildNextPdu(m_pduBuilder);
                log().debug("Sending tracker pdu of size "+m_pduBuilder.getPdu().getLength());
                m_session.send(m_pduBuilder.getPdu(), m_handler);
            }
        } catch (Throwable e) {
            handleFatalError(e);
        }
        
        
    }

    /**
     * <P>
     * Returns the success or failure code for collection of the data.
     * </P>
     */
    public boolean failed() {
        return m_error;
    }

    private void handleFatalError(Throwable e) {
        m_error = true;
        m_tracker.setFailed(true);
        log().error(getName()+": Unexpected Error occurred processing "+getName()+" for "+m_address, e);
        finish();
    }

    private void finish() {
        if (m_session != null) {
            m_session.close();
            m_session = null;
        }
        signal();
    }

    public String getName() {
        return m_name;
    }

    private void handleDone() {
        finish();
    }

    private void handleError(String msg) {
        m_error = true;
        m_tracker.setFailed(true);
        log().info(getName()+": Error retrieving "+getName()+" for "+m_address+": "+msg);
        finish();
    }

    private void signal() {
        synchronized (this) {
            notifyAll();
        }
        if (m_signal != null) {
            m_signal.signalAll();
        }
    }

    private final Category log() {
        return ThreadCategory.getInstance(SnmpWalker.class);
    }

    private int getVersion() {
        return m_peer.getParameters().getVersion();
    }



}
