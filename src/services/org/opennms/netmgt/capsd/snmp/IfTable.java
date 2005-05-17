//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.capsd.snmp;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.utils.Signaler;
import org.opennms.protocols.snmp.SnmpHandler;
import org.opennms.protocols.snmp.SnmpInt32;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpPduBulk;
import org.opennms.protocols.snmp.SnmpPduPacket;
import org.opennms.protocols.snmp.SnmpPduRequest;
import org.opennms.protocols.snmp.SnmpSMI;
import org.opennms.protocols.snmp.SnmpSession;
import org.opennms.protocols.snmp.SnmpSyntax;
import org.opennms.protocols.snmp.SnmpVarBind;

/**
 * <P>
 * The IfTable uses a SnmpSession to collect the entries in the remote agent's
 * interface table. It implements the SnmpHandler to receive notifications and
 * handle errors associated with the data collection. Data is collected using a
 * series of GETNEXT PDU request to walk multiple parts of the interface table
 * at once. The number of SNMP packets should not exceed the number of interface +
 * 1, assuming no lost packets or error conditions occur.
 * </P>
 * 
 * <p>
 * <em>Addition by Jon Whetzel</em>
 * </p>
 * <p>
 * IfTable has an extra class variable for the SNMP version setting. If this is
 * set for SNMPv2, then a GETBULK command will be used for retrieving the
 * necessary data. Otherwise, the method will resort to its previous
 * implementation with GETNEXT commands.
 * </p>
 * 
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213 </A>
 */
public final class IfTable implements SnmpHandler {
    
    /**
     * Used to convert decimal to hex
     */
    private static final char[] m_hexDigit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    /**
     * <P>
     * The list of interfaces from the remote's interface table. The list
     * contains a set of IfTableEntry objects that were collected from the
     * remote host.
     * </P>
     * 
     * @see IfTableEntry
     */
    private List m_entries;

    /**
     * <P>
     * Flag indicating if query was successful or if the collection failed.
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

    /**
     * <P>
     * Used to generate the proper command for fetching the SNMP data from the
     * agent (via GETBULK for version 2 or GETNEXT for version 1.
     * </P>
     */
    private int m_version;

    /**
     * <P>
     * The request id associated with the GetNext PDU generated to retrieve the
     * number of interfaces associated with the remote host.
     * </P>
     */
    private int m_ifNumberRequestId;

    /**
     * <P>
     * This will be the OID where the information should cut off from the return
     * packet from the GETBULK command.
     * </P>
     */
    private SnmpObjectId m_stopAt = null;

    /**
     * <P>
     * Used for storing the ifNumber variable from the MIB, the number of
     * interfaces a device possesses.
     * </P>
     */
    private int m_ifNumber;



    /**
     * <P>
     * The default constructor is marked as private and will always throw an
     * exception. This is done to disallow the constructor to be called. Since
     * any instance of this class must have an SnmpSession and Signaler to
     * properly work, the correct constructor must be used.
     * </P>
     * 
     * @exception java.lang.UnsupportedOperationException
     *                Always thrown from this method since it is not supported.
     * 
     * @see #IfTable(SnmpSession, Signaler)
     * 
     */
    private IfTable() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Default constructor not supported!");
    }

    /**
     * <P>
     * Constructs an IfTable object that is used to collect the interface
     * elements from the remote agent. Once all the interfaces are collected, or
     * there is an error in the collection the signaler object is <EM>notified
     * </EM> to inform other threads.
     * </P>
     * 
     * @param session
     *            The session with the remote agent.
     * @param signaler
     *            The object to notify waiters.
     * 
     * @see IfTableEntry
     */
    public IfTable(SnmpSession session, Signaler signaler) {
        m_signal = signaler;
        m_entries = new ArrayList(2); // not synchronized.
        m_error = false;

        m_version = SnmpSMI.SNMPV1;
        m_stopAt = IfTableEntry.stop_oid();

        // first process, attain ifNumber.
        SnmpPduRequest pdu = IfTableEntry.getIfNumberPdu();
        setIfNumberRequestId(pdu.getRequestId());

        session.send(pdu, this);
    }

    /**
     * <P>
     * Constructs an IfTable object that is used to collect the interface
     * elements from the remote agent. Once all the interfaces are collected, or
     * there is an error in the collection the signaler object is <EM>notified
     * </EM> to inform other threads.
     * </P>
     * @param session
     *            The session with the remote agent.
     * @param address TODO
     * @param signaler
     *            The object to notify waiters.
     * 
     * @see IfTableEntry
     */
    public IfTable(SnmpSession session, InetAddress address, Signaler signaler, int version) {
        m_signal = signaler;
        m_entries = new ArrayList(2); // not synchronized.
        m_error = false;

        m_version = version;
        m_stopAt = IfTableEntry.stop_oid();

        // first process, attain ifNumber.
        m_currentWalkState = new V1WalkState();
        m_currentWalkState.send(this, session);
    }

    /**
     * <P>
     * Returns the success or failure code for collection of the data.
     * </P>
     */
    public boolean failed() {
        return m_error;
    }

    /**
     * <P>
     * Returns the list of entry maps that can be used to access all the
     * information about the interface table.
     * </P>
     * 
     * @return The list of ifTableEntry maps.
     */
    public List getEntries() {
        return m_entries;
    }


    private void done() {
        // release the storage since we are not going
        // to be using it any further.
        //
        m_currentWalkState = null;
        signal();
    }

    private void checkReponseErrorStatus(SnmpPduPacket pdu) throws InvalidResponseException {
        // Check for error stored in request pdu
        //
        int errStatus = ((SnmpPduRequest) pdu).getErrorStatus();
        if (errStatus != SnmpPduPacket.ErrNoError) {
            throw new InvalidResponseException("SNMP errorStatus in packet.  Error status:"+errStatus);
        }
    }

    private void checkResponseType(int command) throws InvalidResponseException {
        // handle the command.
        //
        if (command != SnmpPduPacket.RESPONSE) {
            throw new InvalidResponseException("Unexpected SNMP packet."+command);
        }
    }

    static class V2WalkState implements WalkState {

        /**
         * <P>
         * Used as a temporary storage space for all the data collected from the
         * SNMP response packets for SNMP v2. After receiving all the data, the
         * information will be sorted so that it mimics the SNMP v1 data storage;
         * one map per interface containing all the necessary MIB values.
         * </P>
         */
        // private SnmpVarBind[] m_tempStorage = new SnmpVarBind[20000];
        private SnmpVarBind[] m_tempStorage = null;

        private SnmpPduPacket m_lastPdu;
        private int m_responses = 0;
        private IfNumberWalkState m_ifNumberWS;
        
        public V2WalkState() {
            m_ifNumberWS = new IfNumberWalkState(null);

        }

        public WalkState processVarbinds(IfTable table, SnmpPduPacket pdu) throws InvalidResponseException {
            
            checkVarbindError(pdu);
            
            m_lastPdu = pdu;

            for (int y = 0; y < pdu.getLength(); y++) {
                // Check for v2 error in each returned varbind
                SnmpVarBind vb = pdu.getVarBindAt(y);
                
                table.log().debug("storing varbind with value: "+vb.getValue()+" of type: "+vb.getValue().getClass()+" at index: "+m_responses);
                m_tempStorage[m_responses] = vb;
                m_responses++;
            }
            
            if (!isDone(table, pdu))
                return this;
            else {
                processTempResults(table);
                return null;
            }
        }

        public void sendFirst(IfTable table, SnmpSession session) {

            m_tempStorage = new SnmpVarBind[table.m_ifNumber * IfTableEntry.getElementListSize()];

            
            SnmpPduPacket nxt = null;
            nxt = IfTableEntry.getBulkPdu(table.m_ifNumber);
            session.send(nxt, table);
        }

        public void send(IfTable table, SnmpSession session) {
            if (m_lastPdu == null)
                sendFirst(table, session);
            else
                sendNext(table, session, m_lastPdu);
        }

        public void sendNext(IfTable table, SnmpSession session, SnmpPduPacket pdu) {
            SnmpObjectId id = new SnmpObjectId(pdu.getVarBindAt(pdu.getLength() - 1).getName());
            SnmpVarBind[] newvblist = { new SnmpVarBind(id) };
            SnmpPduPacket nxt = new SnmpPduBulk(0, getMaxReps(table), newvblist);
            nxt.setRequestId(SnmpPduPacket.nextSequence());
            if (table.log().isDebugEnabled())
                table.log().debug("smnpReceivedPDU: Starting new GETBULK packet at OID = " + id.toString() + ", with request ID: " + nxt.getRequestId());
            session.send(nxt, table);
        }

        public boolean isDone(IfTable table, SnmpPduPacket pdu) {
            if (table.log().isDebugEnabled())
                table.log().debug("snmpReceivedPdu: calculated number of maxRepetitions = " + getMaxReps(table));
            return !(getMaxReps(table) > 0 && table.m_stopAt.compare(pdu.getVarBindAt(pdu.getLength() - 1).getName()) > 0);
        }

        private int getMaxReps(IfTable table) {
            return (table.m_ifNumber * IfTableEntry.getElementListSize()) - m_responses;
        }

        void processTempResults(IfTable table) {
            if (table.log().isDebugEnabled())
                table.log().debug("smnpReceivedPDU: All SNMPv2 data received, processing...");
            // all the data has been retrieved from the MIB, so
            // now
            // we must enter it into our maps. Each map will
            // hold all
            // the MIB variable values per interface.
            //
            // get the next possible index value from the
            // temporary storage
            // array, since the first variable is the ifIndex
            // value. After
            // scan through the entire temporary array,
            // comparing the
            // index of each OID to the index stored as
            // 'ifIndex'.
            for (int x = 0; x < table.m_ifNumber; x++) {
                SnmpVarBind[] templist = new SnmpVarBind[22];
                
                table.log().debug("Processing index: "+x+" value is: "+m_tempStorage[x].getValue());
                
                SnmpInt32 ifIndex = (SnmpInt32) m_tempStorage[x].getValue();
        
                // parse each oid to get index
                int tempcount = 0;
        
                for (int j = 0; j < m_responses && tempcount < 22; j++) {
                    // Extract the "instance" id from the
                    // current SnmpVarBind's object id
                    //
                    String from_oid = m_tempStorage[j].getName().toString();
                    SnmpObjectId id = new SnmpObjectId(from_oid);
                    int[] ids = id.getIdentifiers();
                    int instance_id = ids[ids.length - 1];
                    String temp_index = Integer.toString(instance_id);
        
                    try {
                        Integer check = Integer.valueOf(temp_index);
        
                        // if the indexes match, store it within
                        // templist
                        if (check.intValue() == ifIndex.getValue()) {
                            templist[tempcount++] = m_tempStorage[j];
                        }
                    } catch (NumberFormatException nfE) {
                        table.log().warn("snmpReceivedPdu: unable to convert last decimal of object identifier '" + m_tempStorage[j].getName().toString() + "' to integer for ifIndex comparison.", nfE);
                    }
                }
        
                // create VarBind list from templist.
                SnmpVarBind[] vblist = new SnmpVarBind[tempcount];
                for (int a = 0; a < tempcount; a++) {
                    vblist[a] = templist[a];
                }
        
                processVarbinds(table, vblist);
            } // end for()
            
            m_tempStorage = null;
        }

        private void processVarbinds(IfTable table, SnmpVarBind[] vblist) {
            // create new IfTableEntry with all variables
            // for a
            // particular index.
            IfTableEntry ent = new IfTableEntry(vblist);
            table.m_entries.add(ent);
        }

        void checkVarbindError(SnmpPduPacket pdu) throws InvalidResponseException {
            for (int y = 0; y < pdu.getLength(); y++) {
                // Check for v2 error in each returned varbind
                SnmpVarBind vb = pdu.getVarBindAt(y);
        
                if (vb.getValue() instanceof org.opennms.protocols.snmp.SnmpV2Error) {
                    throw new InvalidResponseException("snmpReceivedPDU: varbind: " + vb.getName() + "  error: '" + vb.getValue() + "'");
                }
            }
        }};
    
    static class V1WalkState implements WalkState {
        
        private SnmpPduPacket m_lastPdu = null;
        private int m_responses = 0;
        private IfNumberWalkState m_ifNumberWS;
        
        public V1WalkState() {
            m_ifNumberWS = new IfNumberWalkState(null);
        }

        public void sendFirst(IfTable table, SnmpSession session) {
            SnmpPduPacket nxt = null;
            nxt = IfTableEntry.getNextPdu();
            session.send(nxt, table);
        }

        public void send(IfTable table, SnmpSession session) {
            if (m_ifNumberWS != null)
                m_ifNumberWS.send(table, session);
            else if (m_lastPdu == null)
                sendFirst(table, session);
            else
                sendNext(table, session, m_lastPdu);
        }

        /* (non-Javadoc)
         * @see org.opennms.netmgt.capsd.snmp.WalkState#sendNext(org.opennms.netmgt.capsd.snmp.IfTable, org.opennms.protocols.snmp.SnmpSession, org.opennms.protocols.snmp.SnmpPduPacket)
         */
        public void sendNext(IfTable table, SnmpSession session, SnmpPduPacket pdu) {
            SnmpPduRequest nxt = new SnmpPduRequest(SnmpPduPacket.GETNEXT);
            for (int x = 0; x < pdu.getLength(); x++) {
                nxt.addVarBind(new SnmpVarBind(pdu.getVarBindAt(x).getName()));
            }
            nxt.setRequestId(SnmpPduPacket.nextSequence());
            session.send(nxt, table);
        }

        /* (non-Javadoc)
         * @see org.opennms.netmgt.capsd.snmp.WalkState#isDone(org.opennms.netmgt.capsd.snmp.IfTable, org.opennms.protocols.snmp.SnmpPduPacket)
         */
        public boolean isDone(IfTable table, SnmpPduPacket pdu) {
            return !(m_responses < table.m_ifNumber);
        }

        /* (non-Javadoc)
         * @see org.opennms.netmgt.capsd.snmp.WalkState#processVarbinds(org.opennms.netmgt.capsd.snmp.IfTable, org.opennms.protocols.snmp.SnmpPduPacket)
         */
        public WalkState processVarbinds(IfTable table, SnmpPduPacket pdu) throws InvalidResponseException {
            if (m_ifNumberWS != null) {
                m_ifNumberWS = (IfNumberWalkState)m_ifNumberWS.processVarbinds(table, pdu);
                return this;
            } else {
                
                SnmpVarBind[] vblist = pdu.toVarBindArray();
                processVarbinds(table, vblist);
                m_responses++;
                
                m_lastPdu  = pdu;
                if (!isDone(table, pdu)) {
                    return this;
                } else {
                    return null;
                }
            }
        }

        private void processVarbinds(IfTable table, SnmpVarBind[] vblist) {
            IfTableEntry ent = new IfTableEntry(vblist);
            table.m_entries.add(ent);
        }};
        
    static class IfNumberWalkState implements WalkState {

        private SnmpPduPacket m_lastPdu;
        private WalkState m_next;

        public IfNumberWalkState(WalkState next) {
            m_next = next;
        }

        public void send(IfTable table, SnmpSession session) {
            if (m_lastPdu == null) {
                sendFirst(table, session);
            } else {
                sendNext(table, session, m_lastPdu);
            }
        }

        private void sendNext(IfTable table, SnmpSession session, SnmpPduPacket lastPdu) {
        }

        private void sendFirst(IfTable table, SnmpSession session) {
            SnmpPduRequest pdu = IfTableEntry.getIfNumberPdu();
            table.setIfNumberRequestId(pdu.getRequestId());
            Category log = table.log();
            if (log.isDebugEnabled()) {
                log.debug("<ctor>: ifNumber retrieval pdu request id: " + table.getIfNumberRequestId());
            }
            session.send(pdu, table);
        }


        public boolean isDone(IfTable table, SnmpPduPacket pdu) {
            return !(table.m_ifNumber > 0);
        }

        public WalkState processVarbinds(IfTable table, SnmpPduPacket pdu) throws InvalidResponseException {
            SnmpVarBind vb = pdu.getVarBindAt(0);
            
            checkForErrors(table, vb);
            
            m_lastPdu = pdu;
            
           processVarbind(table, vb);
            // 
            // Now that we know the number of interfaces we can can
            // allocate
            // the temp storage to hold all the response variable
            // bindings
            // 
            
            if (!isDone(table, pdu)) {
                return m_next;
            } else {
                return null;
            }
            
        }

        private void processVarbind(IfTable table, SnmpVarBind vb) {
            SnmpInt32 temp = (SnmpInt32) vb.getValue();
            table.m_ifNumber = temp.getValue();
            if (table.log().isDebugEnabled())
                table.log().debug("snmpReceivedPdu: got response to ifNumber request: " + table.m_ifNumber);
        }

        private void checkForErrors(IfTable table, SnmpVarBind vb) throws InvalidResponseException {
            SnmpObjectId ifNumberOid = new SnmpObjectId(".1.3.6.1.2.1.2.1");
            checkVarbindPrefix(ifNumberOid, vb);
            checkVarbindError(table, vb);
        }

        private void checkVarbindPrefix(SnmpObjectId ifNumberOid, SnmpVarBind vb) throws InvalidResponseException {
            if (!ifNumberOid.isRootOf(vb.getName())) {
                throw new InvalidResponseException("snmpReceivedPdu: agent does not support interfaces mib");
            }
        }


        void checkVarbindError(IfTable table, SnmpVarBind vb) throws InvalidResponseException {
            if (table.m_version == SnmpSMI.SNMPV2) {
                if (vb.getValue() instanceof org.opennms.protocols.snmp.SnmpV2Error) {
                    throw new InvalidResponseException("snmpReceivedPDU: varbind: " + vb.getName() + "  error: '" + vb.getValue() + "'");
                }
            }
        }

    };

    private WalkState m_currentWalkState;

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    public class SnmpWalker {
    }
    
    SnmpWalker m_walker = new SnmpWalker();
    
        
        /**
         * <P>
         * This method is used to process received SNMP PDU packets from the remote
         * agent. The method is part of the SnmpHandler interface and will be
         * invoked when a PDU is successfully decoded. The method is passed the
         * receiving session, the PDU command, and the actual PDU packet.
         * </P>
         * 
         * <P>
         * When all the data has been received from the session the signaler object,
         * initialized in the constructor, is signaled. In addition, the receiving
         * instance will call notifyAll() on itself at the same time.
         * </P>
         * 
         * <P>
         * For SNMP version 2 devices, all the received data enters a temporary
         * array. After the collecting process, the method sorts the data so that
         * each interface has its own map.
         * </P>
         * 
         * @param session
         *            The SNMP Session that received the PDU
         * @param command
         *            The command contained in the received pdu
         * @param pdu
         *            The actual received PDU.
         * 
         */
        public void snmpReceivedPdu(SnmpSession session, int command, SnmpPduPacket pdu) {
            
            try {
                
                if (log().isDebugEnabled()) {
                    log().debug("snmpReceivedPdu: got SNMP response, current version: " + ((m_version == SnmpSMI.SNMPV1) ? "SNMPv1" : "SNMPv2"));
                }
                
                checkResponseType(command); 
                checkReponseErrorStatus(pdu);
                
                m_currentWalkState = m_currentWalkState.processVarbinds(this, pdu);
                if (m_currentWalkState != null) {
                    m_currentWalkState.send(this, session);
                } else {
                    done();
                } 
                
            } catch (InvalidResponseException e) {
                m_error = true;
                log().debug(e.getMessage());
                done();
            }
        }
        
        
        /**
         * <P>
         * This method is part of the SnmpHandler interface and called when an
         * internal error happens in a session. This is usually the result of an I/O
         * error. This method will not be called if the session times out sending a
         * packet, see snmpTimeoutError for timeout handling.
         * </P>
         * 
         * @param session
         *            The session that had an unexpected error
         * @param error
         *            The error condition
         * @param pdu
         *            The PDU being sent when the error occured
         * 
         * @see #snmpTimeoutError
         * @see org.opennms.protocols.snmp.SnmpHandler SnmpHandler
         */
        public void snmpInternalError(SnmpSession session, int error, SnmpSyntax pdu) {
            Category log = log();
            if (log.isDebugEnabled()) {
                log.debug("snmpInternal error: " + error + " for: " + session.getPeer().getPeer());
            }
            
            m_error = true;
            signal();
        }
        
        /**
         * <P>
         * This method is part of the SnmpHandler interface and is invoked when the
         * SnmpSession does not receive a reply after exhausting the retransmission
         * attempts.
         * </P>
         * 
         * @param session
         *            The session invoking the error handler
         * @param pdu
         *            The PDU that the remote failed to respond to.
         * 
         * @see org.opennms.protocols.snmp.SnmpHandler SnmpHandler
         * 
         */
        public void snmpTimeoutError(SnmpSession session, SnmpSyntax pdu) {
            Category log = log();
            if (log.isDebugEnabled()) {
                log.debug("snmpTimeoutError for: " + session.getPeer().getPeer());
            }
            
            m_error = true;
            signal();
        }
        
    

    private void signal() {
        synchronized (this) {
            notifyAll();
        }
        if (m_signal != null) {
            m_signal.signalAll();
        }
    }

    public int getAdminStatus(int ifIndex) {
        if (getEntries() == null)
            return -1;
        Iterator i = getEntries().iterator();
        while (i.hasNext()) {
            IfTableEntry entry = (IfTableEntry) i.next();
            Integer ndx = entry.getIfIndex();
            if (ndx != null && ndx.intValue() == ifIndex) {
                // found it
                // extract the admin status
                //
                Integer ifStatus = entry.getIfAdminStatus();
                if (ifStatus != null)
                    return ifStatus.intValue();
            }
        }
        return -1;
    }

    public int getIfType(int ifIndex) {
        if (getEntries() == null)
            return -1;
        Iterator i = getEntries().iterator();
        while (i.hasNext()) {
            IfTableEntry entry = (IfTableEntry) i.next();
            Integer ndx = entry.getIfIndex();
            if (ndx != null && ndx.intValue() == ifIndex) {
                // found it
                // extract the ifType
                //
                Integer ifType = entry.getIfType();
                if (ifType != null)
                    return ifType.intValue();
            }
        }
        return -1;
    }

    private void setIfNumberRequestId(int ifNumberRequestId) {
        m_ifNumberRequestId = ifNumberRequestId;
    }

    private int getIfNumberRequestId() {
        return m_ifNumberRequestId;
    }
}
