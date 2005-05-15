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
// IfXTable.java,v 1.1.1.1 2001/11/11 17:34:36 ben Exp
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
 * The IfXTable uses a SnmpSession to collect the entries in the remote agent's
 * interface extensions table. It implements the SnmpHandler to receive
 * notifications and handle errors associated with the data collection. Data is
 * collected using a series of GETNEXT PDU request to walk multiple parts of the
 * interface table at once. The number of SNMP packets should not exceed the
 * number of interface + 1, assuming no lost packets or error conditions occur.
 * </P>
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 * @version 1.1.1.1
 * 
 * @see <A HREF="http://www.ietf.org/rfc/rfc2233.txt">RFC2233 </A>
 */
public final class IfXTable implements SnmpHandler {
    //
    // Used to convert decimal to hex
    //
    private static final char[] m_hexDigit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    /**
     * <P>
     * The list of interfaces from the remote's interface extensions table. The
     * list contains a set of IfXTableEntry objects that were collected from the
     * remote host.
     * </P>
     * 
     * @see IfXTableEntry
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
     * Used as a temporary storage space for all the data collected from the
     * SNMP response packets for SNMP v2. After receiving all the data, the
     * information will be sorted so that it mimics the SNMP v1 data storage;
     * one map per interface containing all the necessary MIB values.
     * </P>
     */
    private SnmpVarBind[] tempStorage = null;

    /**
     * <P>
     * For SNMPv1 used to keep track of the number of SNMP response packets
     * received.
     * 
     * For SNMPv2 used to keep track of the total number of varbinds received in
     * SNMP response packets.
     * 
     * For both SNMPv1 and SNMPv2 this value is used to determine when all the
     * necessary SNMP data has been retrieved.
     * </P>
     */
    private int m_responses = 0;

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
     * @see #IfXTable(SnmpSession, Signaler)
     * 
     */
    private IfXTable() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Default constructor not supported!");
    }

    /**
     * <P>
     * Constructs an IfXTable object that is used to collect the interface
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
     * @see IfXTableEntry
     */
    public IfXTable(SnmpSession session, Signaler signaler) {
        m_signal = signaler;
        m_entries = new ArrayList(2); // not synchronized.
        m_error = false;

        m_version = SnmpSMI.SNMPV1;
        m_stopAt = IfXTableEntry.stop_oid();

        // first process, attain ifNumber.
        SnmpPduRequest pdu = IfXTableEntry.getIfNumberPdu();
        m_ifNumberRequestId = pdu.getRequestId();
        ThreadCategory.getInstance(getClass()).debug("IfXTable: ifNumber retrieval pdu request id: " + m_ifNumberRequestId);

        session.send(pdu, this);
    }

    /**
     * <P>
     * Constructs an IfXTable object that is used to collect the interface
     * elements from the remote agent. Once all the interfaces are collected, or
     * there is an error in the collection the signaler object is <EM>notified
     * </EM> to inform other threads.
     * </P>
     * @param session
     *            The session with the remote agent.
     * @param address TODO
     * @param signaler
     *            The object to notify waiters.
     * @param version
     *            SNMP version to use
     * 
     * @see IfXTableEntry
     */
    public IfXTable(SnmpSession session, InetAddress address, Signaler signaler, int version) {
        m_signal = signaler;
        m_entries = new ArrayList(2); // not synchronized.
        m_error = false;

        m_version = version;
        m_stopAt = IfXTableEntry.stop_oid();

        // first process, attain ifNumber.
        SnmpPduRequest pdu = IfXTableEntry.getIfNumberPdu();
        m_ifNumberRequestId = pdu.getRequestId();
        ThreadCategory.getInstance(getClass()).debug("IfXTable: ifNumber retrieval pdu request id: " + m_ifNumberRequestId);
        session.send(pdu, this);
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
     * information about the interface extensions table.
     * </P>
     * 
     * @return The list of ifXTableEntry maps.
     */
    public List getEntries() {
        return m_entries;
    }

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
        boolean doNotify = true;

        // lookup the category
        //
        Category log = ThreadCategory.getInstance(getClass());

        if (command != SnmpPduPacket.RESPONSE) {
            m_error = true;
        } else {
            //
            // Check for error stored in request pdu
            //
            int errStatus = ((SnmpPduRequest) pdu).getErrorStatus();
            if (errStatus != SnmpPduPacket.ErrNoError) {
                m_error = true;
            } else {
                // Is this the response to our request to retrieve ifNumber?
                // If so, begin gathering all the MIB data for the device
                if (pdu.getRequestId() == m_ifNumberRequestId) {
                    // Check for v2 error in varbind
                    SnmpVarBind vb = pdu.getVarBindAt(0);
                    SnmpObjectId ifNumberOid = new SnmpObjectId(".1.3.6.1.2.1.2.1");
                    if (!ifNumberOid.isRootOf(vb.getName())) {
                        m_error = true;
                        log.warn("snmpReceivedPDI: agent does not support interfaces mib!");
                    } else if (vb.getValue() instanceof org.opennms.protocols.snmp.SnmpV2Error) {
                        m_error = true;
                        if (log.isDebugEnabled())
                            log.debug("snmpReceivedPDU: varbind: " + vb.getName() + "  error: '" + vb.getValue() + "'");
                    }

                    if (!m_error) {
                        SnmpInt32 temp = (SnmpInt32) vb.getValue();
                        m_ifNumber = temp.getValue();
                        if (log.isDebugEnabled())
                            log.debug("snmpReceivedPdu: Number of interfaces = " + m_ifNumber);

                        // 
                        // Now that we know the number of interfaces we can can
                        // allocate
                        // the temp storage to hold all the response variable
                        // bindings
                        // 
                        tempStorage = new SnmpVarBind[m_ifNumber * IfXTableEntry.getElementListSize()];
                        SnmpPduPacket nxt = null;
                        if (m_version == SnmpSMI.SNMPV2) {
                            nxt = IfXTableEntry.getBulkPdu(m_ifNumber);
                        } else {
                            nxt = IfXTableEntry.getNextPdu();
                        }

                        session.send(nxt, this);
                        doNotify = false;
                    }
                }
                // Handle SNMPv2 GetBulk responses...
                else if (m_version == SnmpSMI.SNMPV2) {
                    if (log.isDebugEnabled()) {
                        log.debug("snmpReceivedPdu: Handling GETBULK packet");
                    }
                    int length = pdu.getLength();

                    for (int y = 0; y < pdu.getLength(); y++) {
                        // Check for v2 error in each returned varbind
                        SnmpVarBind vb = pdu.getVarBindAt(y);

                        if (vb.getValue() instanceof org.opennms.protocols.snmp.SnmpV2Error) {
                            m_error = true;
                            if (log.isDebugEnabled())
                                log.debug("snmpReceivedPDU: varbind: " + vb.getName() + "  error: '" + vb.getValue() + "'");
                            break;
                        }

                        tempStorage[m_responses] = vb;
                        m_responses++;
                    }

                    if (!m_error) {
                        //
                        // in case we did not receive all the data from the
                        // first packet, must generate a new GETBULK packet
                        // starting at the OID the previous one left off.
                        //

                        // Calculate maxRepetitions for next GETBULK packet
                        int maxReps = (m_ifNumber * IfXTableEntry.getElementListSize()) - m_responses;
                        if (log.isDebugEnabled())
                            log.debug("snmpReceivedPdu: calculated number of maxRepetitions = " + maxReps);

                        if (maxReps > 0 && m_stopAt.compare(pdu.getVarBindAt(length - 1).getName()) > 0) {
                            SnmpObjectId id = new SnmpObjectId(pdu.getVarBindAt(length - 1).getName());
                            SnmpVarBind[] newvblist = { new SnmpVarBind(id) };
                            SnmpPduPacket nxt = new SnmpPduBulk(0, maxReps, newvblist);
                            nxt.setRequestId(SnmpPduPacket.nextSequence());
                            if (log.isDebugEnabled())
                                log.debug("smnpReceivedPDU: Starting new GETBULK packet at OID = " + id.toString() + ", with request ID: " + nxt.getRequestId());
                            session.send(nxt, this);
                            doNotify = false;
                        } else {
                            if (log.isDebugEnabled())
                                log.debug("smnpReceivedPDU: All SNMPv2 data received, processing.");

                            // all the data has been retrieved from the MIB, so
                            // now
                            // we must enter it into our maps. Each map will
                            // hold all
                            // the MIB variable values per interface.

                            /*
                             * DEBUG ONLY if (log.isDebugEnabled()) { // Dump
                             * content of tempStorage for (int x=0; x
                             * <m_responses; x++) { log.debug("snmpReceivedPdu:
                             * tempStorage[" + x + "] oid: " +
                             * tempStorage[x].getName().toString()); }
                             * log.debug("snmpReceivedPdu: done dumping temp
                             * storage!!!");
                             *  }
                             */

                            // 
                            // Create an IfXTableEntry for each interface using
                            // the variable
                            // names and values stored in the temporary storage
                            // array.
                            //
                            // Unlike the ifTable, the ifXTable doesn't have the
                            // benefit of the
                            // ifIndex object...so use the variable name of the
                            // first ifXTableEntry
                            // (ifName) to derive the ifIndex as each interface
                            // is processed.
                            // The last decimal value (the instance id) in the
                            // variable name will be
                            // extracted and used as the ifIndex for the
                            // interface. For example, given
                            // a variable name of '.1.3.6.1.2.1.31.1.1.1.5', the
                            // last decimal value '5'
                            // is parsed out and used as the ifIndex.
                            if (log.isDebugEnabled())
                                log.debug("snmpReceivedPdu: processing temp storage array...");

                            for (int x = 0; x < m_ifNumber; x++) {
                                SnmpVarBind[] templist = new SnmpVarBind[IfXTableEntry.NUM_OIDS];

                                // Extract the ifIndex from the current
                                // SnmpVarBind's object id
                                String from_oid = tempStorage[x].getName().toString();
                                SnmpObjectId id = new SnmpObjectId(from_oid);
                                int[] ids = id.getIdentifiers();
                                int ifIndex = ids[ids.length - 1];

                                // parse each oid to get index
                                int tempcount = 0;

                                for (int j = 0; j < m_responses && tempcount < IfXTableEntry.NUM_OIDS; j++) {
                                    // Extract the "instance" id from the
                                    // current SnmpVarBind's object id
                                    from_oid = tempStorage[j].getName().toString();
                                    id = new SnmpObjectId(from_oid);
                                    ids = id.getIdentifiers();
                                    int instance_id = ids[ids.length - 1];

                                    // if the indexes match, store it within
                                    // templist
                                    if (instance_id == ifIndex) {
                                        templist[tempcount++] = tempStorage[j];
                                    }
                                }

                                // create VarBind list from templist.
                                SnmpVarBind[] vblist = new SnmpVarBind[tempcount];
                                for (int a = 0; a < tempcount; a++) {
                                    /*
                                     * DEBUG if (log.isDebugEnabled())
                                     * log.debug("snmpReceivedPdu: oid = " +
                                     * templist[a].getName().toString() + "
                                     * value = " +
                                     * templist[a].getValue().toString());
                                     */
                                    vblist[a] = templist[a];
                                }

                                // create new IfXTableEntry with all variables
                                // for a
                                // particular index.
                                IfXTableEntry ent = new IfXTableEntry(vblist);
                                m_entries.add(ent);
                            } // end for()
                        }
                    } // end if (!m_error)
                } // end if (m_version == SnmpSMI.SNMPV2)

                // Handle SNMPv1 GetNext responses
                else if (m_version == SnmpSMI.SNMPV1) {
                    if (log.isDebugEnabled())
                        log.debug("snmpReceivedPdu: Handling GETNEXT packet, response count: " + m_responses);

                    // if the response count is less than the number of
                    // interfaces, continue to
                    // store info and generate packets for gathering data.
                    if (m_responses < m_ifNumber) {
                        SnmpVarBind[] vblist = pdu.toVarBindArray();
                        IfXTableEntry ent = new IfXTableEntry(vblist);
                        m_entries.add(ent);

                        SnmpPduRequest nxt = new SnmpPduRequest(SnmpPduPacket.GETNEXT);
                        for (int x = 0; x < pdu.getLength(); x++) {
                            nxt.addVarBind(new SnmpVarBind(pdu.getVarBindAt(x).getName()));
                        }
                        nxt.setRequestId(SnmpPduPacket.nextSequence());
                        session.send(nxt, this);
                        doNotify = false;
                        m_responses++;
                    }
                } // end if (m_version == SnmpSMI.SNMPV1
            } // end if (errStatus != SnmpPduPacket.ErrNoError)
        } // end if (command != SnmpPduPacket.RESPONSE)

        //
        // call the notifyAll() method on self, and
        // the signalAll() method on the signaler
        //
        if (doNotify) {
            signal();
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
        Category log = ThreadCategory.getInstance(getClass());

        if (log.isDebugEnabled()) {
            log.debug("snmpInternal: error " + error + " for: " + session.getPeer().getPeer());
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
        Category log = ThreadCategory.getInstance(getClass());

        if (log.isDebugEnabled()) {
            log.debug("snmpTimeoutError: for " + session.getPeer().getPeer());
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

    /**
     * <P>
     * This method converts the physical address, normally six bytes, into a
     * hexidecimal string. The string is not prefixed with the traditional <EM>
     * "0x"</EM>, but is the raw hexidecimal string in upper case.
     * </P>
     * 
     * <P>
     * <EM>NOTICE</EM>: The string is converted based on the starndard
     * US-ASCII table. Each NIBBLE is converted to an integer and added to the
     * character '0' (Zero).
     * </P>
     * 
     * @param physAddr
     *            The physical address to convert to a string.
     * 
     * @return The converted physical address as a hexidecimal string.
     * 
     */
    public static String toHexString(byte[] physAddr) {
        //
        // Check to make sure that there
        // is enough data.
        //
        if (physAddr == null || physAddr.length == 0) {
            return null;
        }

        //
        // Convert the actual data
        //
        StringBuffer buf = new StringBuffer(12);
        for (int i = 0; i < physAddr.length; i++) {
            int b = (int) physAddr[i];
            buf.append(m_hexDigit[(b >> 4) & 0xf]); // based upon US-ASCII
            buf.append(m_hexDigit[(b & 0xf)]); // based upon US-ASCII
        }
        return buf.toString().toUpperCase();
    }

    public String getIfName(int ifIndex) {
    
        // Find ifXTable entry with matching ifIndex
        //
        Iterator iter = getEntries().iterator();
        while (iter.hasNext()) {
            IfXTableEntry ifXEntry = (IfXTableEntry) iter.next();
    
            int ifXIndex = -1;
            Integer snmpIfIndex = ifXEntry.getIfIndex();
            if (snmpIfIndex != null)
                ifXIndex = snmpIfIndex.intValue();
    
            // compare with passed ifIndex
            if (ifXIndex == ifIndex) {
                // Found match! Get the ifName
                return ifXEntry.getIfName();
            }
    
        }
        return null;
    }

    public String getIfIndex(int ifIndex) {
        // Find ifXTable entry with matching ifIndex
        //
        Iterator iter = getEntries().iterator();
        while (iter.hasNext()) {
            IfXTableEntry ifXEntry = (IfXTableEntry) iter.next();
    
            int ifXIndex = -1;
            Integer snmpIfIndex = ifXEntry.getIfIndex();
            if (snmpIfIndex != null)
                ifXIndex = snmpIfIndex.intValue();
    
            // compare with passed ifIndex
            if (ifXIndex == ifIndex) {
                // Found match! Get the ifAlias
                return ifXEntry.getIfAlias();
            }
    
        }
        return null;
    }
}
