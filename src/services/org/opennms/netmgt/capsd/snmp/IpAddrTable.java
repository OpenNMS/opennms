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
// 2003 Sep 29: Modifications to allow for OpenNMS to handle duplicate IP Addresses.
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
 * IpAddrTable uses a SnmpSession to collect the ipAddrTable entries It
 * implements the SnmpHandler to receive notifications when a reply is
 * received/error occurs in the SnmpSession used to send requests /recieve
 * replies.
 * </P>
 * 
 * @author <A HREF="mailto:jamesz@opennms.org">James Zuo </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213 </A>
 */
public class IpAddrTable implements SnmpHandler {
    /**
     * The interface to IP address table by ifIndex.
     */
    private static String IP_ADDR_IF_INDEX = ".1.3.6.1.2.1.4.20.1.2";

    /**
     * <P>
     * The list of collected IpAddrTableEntries built from the infomation
     * collected from the remote agent.
     * </P>
     */
    private List m_entries;

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

    /**
     * <P>
     * Used to generate the proper command for fetching the SNMP data from the
     * agent (via GETBULK for version 2 or GETNEXT for version 1.
     */
    private int m_version;

    /**
     * <P>
     * This will be the OID where the information should cut off from the return
     * packet from the GETBULK command.
     */
    private SnmpObjectId m_stopAt = null;

    /**
     * <P>
     * This list will hold each instance of the specific MIB variable listed
     * within IpAddrTableEntry. By keeping these separate, we can generate our
     * own usable map from the variables.
     */
    private List m_snmpVarBindList;

    private InetAddress m_address;

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
     * @see #IpAddrTable(SnmpSession, Signaler)
     * 
     */
    private IpAddrTable() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Default constructor not supported!");
    }

    /**
     * <P>
     * Constructs an IpAddrTable object that is used to collect the address
     * elements from the remote agent. Once all the elements are collected, or
     * there is an error in the collection the signaler object is <EM>notified
     * </EM> to inform other threads.
     * </P>
     * 
     * @param session
     *            The session with the remote agent.
     * @param signaler
     *            The object to notify waiters.
     * 
     * @see IpAddrTableEntry
     */
    public IpAddrTable(SnmpSession session, Signaler signaler) {
        m_signal = signaler;
        m_entries = new ArrayList();
        m_error = false;
        m_version = SnmpSMI.SNMPV1;
        m_stopAt = IpAddrTableEntry.stop_oid();
        m_snmpVarBindList = new ArrayList();

        SnmpPduPacket pdu = IpAddrTableEntry.getNextPdu(m_version);

        session.send(pdu, this);
    }

    /**
     * <P>
     * Constructs an IpAddrTable object that is used to collect the address
     * elements from the remote agent. Once all the elements are collected, or
     * there is an error in the collection the signaler object is <EM>notified
     * </EM> to inform other threads.
     * </P>
     * @param session
     *            The session with the remote agent.
     * @param address TODO
     * @param signaler
     *            The object to notify waiters.
     * 
     * @see IpAddrTableEntry
     */
    public IpAddrTable(SnmpSession session, InetAddress address, Signaler signaler, int version) {
        m_signal = signaler;
        m_entries = new ArrayList();
        m_error = false;
        m_version = version;
        m_stopAt = IpAddrTableEntry.stop_oid();
        m_snmpVarBindList = new ArrayList();
        m_address = address;

        SnmpPduPacket pdu = IpAddrTableEntry.getNextPdu(version);
        log().debug("IpAddrTable: initial pdu request id: " + pdu.getRequestId());
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
     * information about the interface table.
     * </P>
     * 
     * @return The list of ifTableEntry maps.
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
            
            log().debug("snmpReceivedPdu: got SNMP response, current version: " + ((m_version == SnmpSMI.SNMPV1) ? "SNMPv1" : "SNMPv2"));
            
            checkForResponsePdu(command);
            checkForPduError(pdu);
            
            boolean done;
            
            if (m_version == SnmpSMI.SNMPV1){
                done = onV1Response(session, pdu);
            } else {
                done = onV2Response(session, pdu);
            }
            if (done) {
                signal();
            }
            
        } catch (InvalidResponseException e) {
            m_error = true;
            log().info("Error retrieving IpAddrTable for "+m_address+": "+e.getMessage());
            signal();
            
        }
        
    }

    private boolean onV1Response(SnmpSession session, SnmpPduPacket pdu) throws InvalidResponseException {
        
        // Create a new map of the interface entry
        //
        // The last varbind will be the first one to walk off the
        // end
        // of the ipAddrTable. So verify that the last entry in the
        // received pdu is still within the scope of the
        // IpAddrTable...
        // if it is then create a new entry.
        //
        if (!isV1Finished(pdu)) {
            processV1Pdu(pdu);
            
            // next pdu
            //
            sendV1Pdu(session, pdu);
            return false;
        } else {
            return true;
        }
    }

    private boolean isV1Finished(SnmpPduPacket pdu) {
        return !(IpAddrTableEntry.ROOT.isRootOf(pdu.getVarBindAt(pdu.getLength() - 1).getName()));
    }

    private void processV1Pdu(SnmpPduPacket pdu) {
        if (log().isDebugEnabled())
            log().debug("snmpReceivedPdu: got SNMPv1 response and still within IpAddrTable, creating new entry.");
        processTableRow(pdu.toVarBindArray());
    }

    private void sendV1Pdu(SnmpSession session, SnmpPduPacket pdu) {
        SnmpPduRequest nxt = new SnmpPduRequest(SnmpPduPacket.GETNEXT);
        for (int x = 0; x < pdu.getLength(); x++) {
            nxt.addVarBind(new SnmpVarBind(pdu.getVarBindAt(x).getName()));
        }
        nxt.setRequestId(SnmpPduPacket.nextSequence());
        session.send(nxt, this);
    }

    private boolean onV2Response(SnmpSession session, SnmpPduPacket pdu) throws InvalidResponseException {
        
        processV2Pdu(pdu);

        // in case we did not receive all the data from the
        // first packet, must generate a new GETBULK packet
        // starting at the OID the previous one left off.
        //
        if (!isV2Finished(pdu)) {
            sendV2Pdu(session, pdu);
            return false;
        } else {
            processV2Complete();
            return true;
        }
    }

    private void processV2Pdu(SnmpPduPacket pdu) throws InvalidResponseException {
        checkForErrorVarbinds(pdu);
        for (int x = 0; x < pdu.getLength(); x++) {
            SnmpVarBind vb = pdu.getVarBindAt(x);
            m_snmpVarBindList.add(vb);
        }
    }

    private void processV2Complete() {
        // Convert SNMP variable binding list to an array
        // for processing
        //
        SnmpVarBind[] tempStorage = new SnmpVarBind[m_snmpVarBindList.size()];
        tempStorage = (SnmpVarBind[]) m_snmpVarBindList.toArray(tempStorage);
        // since the MIB does not store the number of
        // interfaces that have
        // IP addresses, the method must resort to an
        // alternative. By
        // counting the number of values found for the
        // ipAddrIfIndex variable,
        // we'll have the number of interfaces with IP's.
        // Each IP-bound
        // interface will have a value for each MIB variable
        // listed
        //
        int numInterfaces = 0;
        SnmpObjectId ipAddrIfIndex = new SnmpObjectId(IP_ADDR_IF_INDEX);
        while (ipAddrIfIndex.compare(tempStorage[numInterfaces].getName()) > 0)
            numInterfaces++;
        // store the IP Address Table data for each
        // interface into a map.
        //
        int numEntries = IpAddrTableEntry.getElementListSize();
        for (int if_index = 0; if_index < numInterfaces; if_index++) {
            SnmpVarBind[] vblist = new SnmpVarBind[numEntries];
            for (int vb_index = 0; vb_index < numEntries; vb_index++) {
                vblist[vb_index] = tempStorage[if_index + (vb_index * numInterfaces)];
            }
            processTableRow(vblist);
        }
    }

    private boolean isV2Finished(SnmpPduPacket pdu) {
        return !(m_stopAt.compare(pdu.getVarBindAt(pdu.getLength() - 1).getName()) > 0);
    }

    private void sendV2Pdu(SnmpSession session, SnmpPduPacket pdu) {
        SnmpObjectId id = new SnmpObjectId(pdu.getVarBindAt(pdu.getLength() - 1).getName());
        SnmpVarBind[] newvblist = { new SnmpVarBind(id) };
        SnmpPduPacket nxt = new SnmpPduBulk(0, 10, newvblist);
        nxt.setRequestId(SnmpPduPacket.nextSequence());
        session.send(nxt, this);
    }

    private void processTableRow(SnmpVarBind[] vblist) {
        IpAddrTableEntry ent = new IpAddrTableEntry(vblist);
        m_entries.add(ent);
    }

    private void checkForPduError(SnmpPduPacket pdu) throws InvalidResponseException {
        //
        // Check for error stored in request pdu
        //
        int errStatus = ((SnmpPduRequest) pdu).getErrorStatus();
        if (errStatus != SnmpPduPacket.ErrNoError) {
            throw new InvalidResponseException("Error received from "+m_address+" errStatus="+errStatus);
        }
    }

    private void checkForResponsePdu(int command) throws InvalidResponseException {
        if (command != SnmpPduPacket.RESPONSE) {
            throw new InvalidResponseException("Unexpected packet from "+m_address+" command = "+command);
        }
    }

    private int checkForErrorVarbinds(SnmpPduPacket pdu) throws InvalidResponseException {
        int numVarBinds = pdu.getLength();
        for (int x = 0; x < numVarBinds; x++) {
            SnmpVarBind vb = pdu.getVarBindAt(x);

            if (vb.getValue() instanceof org.opennms.protocols.snmp.SnmpV2Error) {
                throw new InvalidResponseException("snmpReceivedPDU: varbind: " + vb.getName() + "  error: '" + vb.getValue() + "'");
            }
        }
        return numVarBinds;
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
        if (log().isDebugEnabled()) {
            log().debug("snmpInternalError: " + error + " for: " + session.getPeer().getPeer());
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
        if (log().isDebugEnabled()) {
            log().debug("snmpTimeoutError for: " + session.getPeer().getPeer());
        }

        m_error = true;

        signal();
    }

    /**
     * <P>
     * This method is used to find the corresponding IP Address for the indexed
     * interface. The list of IP Address entries are searched until <EM>the
     * first</EM> IP Address is found for the interface. The IP Address is then
     * returned as a string. If there is no interface corresponding to the index
     * then a null is returned to the caller.
     * </P>
     * 
     * @param ipAddrEntries
     *            List of IpAddrTableEntry objects to search
     * @param ifIndex
     *            The interface index to search for
     * 
     * @return IP Address for the indexed interface.
     */
    public static InetAddress getIpAddress(List ipAddrEntries, int ifIndex) {
        if (ifIndex == -1 || ipAddrEntries == null) {
            return null;
        }

        Iterator iter = ipAddrEntries.iterator();
        while (iter.hasNext()) {
            IpAddrTableEntry ipAddrEntry = (IpAddrTableEntry) iter.next();

            Integer snmpIpAddrIndex = ipAddrEntry.getIpAdEntIfIndex();

            if (snmpIpAddrIndex == null) {
                continue;
            }

            int ipAddrIndex = snmpIpAddrIndex.intValue();

            if (ipAddrIndex == ifIndex) {
                return ipAddrEntry.getIpAdEntAddr();
            }

        }

        return null;
    }

    /**
     * Returns all Internet addresses at the corresponding index. If the address
     * cannot be resolved then a null reference is returned.
     * 
     * @param ipAddrEntries
     *            List of IpAddrTableEntry objects to search
     * @param ifIndex
     *            The index to search for.
     * 
     * @return list of InetAddress objects representing each of the interfaces
     *         IP addresses.
     */
    public static List getIpAddresses(List ipAddrEntries, int ifIndex) {
        if (ifIndex == -1 || ipAddrEntries == null) {
            return null;
        }

        List addresses = new ArrayList();

        Iterator i = ipAddrEntries.iterator();
        while (i.hasNext()) {
            IpAddrTableEntry entry = (IpAddrTableEntry) i.next();
            Integer ndx = entry.getIpAdEntIfIndex();
            if (ndx != null && ndx.intValue() == ifIndex) {
                
                InetAddress ifAddr = entry.getIpAdEntAddr();
                if (ifAddr != null) {
                    addresses.add(ifAddr);
                }
            }
        }
        return addresses;
    }

    /**
     * Returns all Internet addresses in the ipAddrEntry list. If the address
     * cannot be resolved then a null reference is returned.
     * 
     * @param ipAddrEntries
     *            List of IpAddrTableEntry objects to search
     * 
     * @return list of InetAddress objects representing each of the interfaces
     *         IP addresses.
     */
    public static List getIpAddresses(List ipAddrEntries) {
        if (ipAddrEntries == null) {
            return null;
        }

        List addresses = new ArrayList();

        Iterator i = ipAddrEntries.iterator();
        while (i.hasNext()) {
            IpAddrTableEntry entry = (IpAddrTableEntry) i.next();
            Integer ndx = entry.getIpAdEntIfIndex();
            if (ndx != null) {

                InetAddress ifAddr = entry.getIpAdEntAddr();
                if (ifAddr != null) {
                    addresses.add(ifAddr);
                }

            }
        }
        return addresses;
    }

    /**
     * <P>
     * This method is used to find the ifIndex of an interface given the
     * interface's IP address. The list of ipAddrTable entries are searched
     * until an interface is found which has a matching IP address. The ifIndex
     * of that interface is then returned. If no match is found -1 is returned.
     * 
     * @param ipAddrEntries
     *            List of IpAddrTableEntry objects to search
     * @param ipAddress
     *            The IP address to search for
     * 
     * @return ifIndex of the interface with the specified IP address
     */
    public static int getIfIndex(List ipAddrEntries, String ipAddress) {
        if (ipAddress == null) {
            return -1;
        }

        Iterator iter = ipAddrEntries.iterator();
        while (iter.hasNext()) {
            IpAddrTableEntry ipAddrEntry = (IpAddrTableEntry) iter.next();
            InetAddress snmpAddr = ipAddrEntry.getIpAdEntAddr();
            if (ipAddress.equals(snmpAddr.toString())) {
                Integer snmpIpAddrIndex = ipAddrEntry.getIpAdEntIfIndex();
                return snmpIpAddrIndex.intValue();
            } else
                continue;
        }

        return -1;
    }

    /**
     * <P>
     * This method is used to find the corresponding netmask for the indexed
     * interface. The list of IP Address table entries are searched until <EM>
     * the first</EM> netmask address is found for the interface. The netmask
     * is then returned as a string. If there is no interface corresponding to
     * the index then a null is returned.
     * </P>
     * 
     * @param ipAddrEntries
     *            List of IpAddrTableEntry objects to search
     * @param ifIndex
     *            The interface index to search for.
     * 
     * @return The netmask for the interface.
     */
    public static String getNetmask(List ipAddrEntries, int ifIndex) {
        if (ifIndex == -1) {
            return null;
        }

        Iterator iter = ipAddrEntries.iterator();
        while (iter.hasNext()) {
            IpAddrTableEntry ipAddrEntry = (IpAddrTableEntry) iter.next();
            Integer snmpIpAddrIndex = ipAddrEntry.getIpAdEntIfIndex();
            if (snmpIpAddrIndex == null) {
                continue;
            }

            int ipAddrIndex = snmpIpAddrIndex.intValue();
            if (ipAddrIndex == ifIndex) {
                InetAddress snmpAddr = ipAddrEntry.getIpAdEntNetMask();
                return (snmpAddr == null ? null : snmpAddr.getHostAddress());
            }
        }

        return null;
    }

    public InetAddress[] getIfAddressAndMask(int ifIndex) {
        if (getEntries() == null)
            return null;
        
        Iterator i = getEntries().iterator();
        while (i.hasNext()) {
            IpAddrTableEntry entry = (IpAddrTableEntry) i.next();
            Integer ndx = entry.getIpAdEntIfIndex();
            if (ndx != null && ndx.intValue() == ifIndex) {
                // found it
                // extract the address
                //
                InetAddress[] pair = new InetAddress[2];
                pair[0] = entry.getIpAdEntAddr();
                pair[1] = entry.getIpAdEntNetMask();
                return pair;
            }
        }
        return null;
    }

    public int getIfIndex(InetAddress address) {
        if (getEntries() == null) {
            return -1;
        }
        if (log().isDebugEnabled())
            log().debug("getIfIndex: num ipAddrTable entries: " + getEntries().size());
        Iterator i = getEntries().iterator();
        while (i.hasNext()) {
            IpAddrTableEntry entry = (IpAddrTableEntry) i.next();
            InetAddress ifAddr = entry.getIpAdEntAddr();
            if (ifAddr != null && ifAddr.equals(address)) {
                // found it
                // extract the ifIndex
                //
                Integer ndx = entry.getIpAdEntIfIndex();
                log().debug("getIfIndex: got a match for address " + address.getHostAddress() + " index: " + ndx);
                if (ndx != null)
                    return ndx.intValue();
            }
        }
        log().debug("getIfIndex: no matching ipAddrTable entry for " + address.getHostAddress());
        return -1;
    }

    private static Category log() {
        return ThreadCategory.getInstance(IpAddrTable.class);
    }
}
