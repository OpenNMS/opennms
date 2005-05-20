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
import java.util.ArrayList;
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

public abstract class SnmpTableWalker implements SnmpHandler {

    private String m_tableName;

    protected SnmpTableWalker(InetAddress address, Signaler signal, int version, String tableName, NamedSnmpVar[] columns, String oid) {

        m_address = address;
        m_signal = signal;
        m_version = version;
        m_columns = columns;
        m_rootOid = oid;
        m_tableName = tableName;

        m_error = false;
        m_snmpVarBindList = new ArrayList();

    }

    protected abstract void processTableRow(SnmpVarBind[] vblist);

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
     * This list will hold each instance of the specific MIB variable listed
     * within IpAddrTableEntry. By keeping these separate, we can generate our
     * own usable map from the variables.
     */
    private List m_snmpVarBindList;
    private InetAddress m_address;
    private NamedSnmpVar[] m_columns;
    private String m_rootOid;

    protected void start(SnmpSession session) {
        log().debug("Walking "+getTableName()+" for "+m_address+" using version "+m_version);

        if (m_version == SnmpSMI.SNMPV1) {
            sendV1Pdu(session, null);
        } else {
            sendV2Pdu(session, null);
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
            
            log().debug(getTableName()+": snmpReceivedPdu: got SNMP response, current version: " + SnmpSMI.getVersionString(m_version));
            
            checkForResponsePdu(command);
            checkForPduError(pdu);
            
            boolean done;
            
            if (m_version == SnmpSMI.SNMPV1){
                done = onV1Response(session, pdu);
            } else {
                done = onV2Response(session, pdu);
            }
            if (done) {
                handleDone();
            }
            
        } catch (InvalidResponseException e) {
            handleError(e.getMessage());
        } catch (Throwable e) {
            handleFatalError(e);
        }
        
    }

    private void handleFatalError(Throwable e) {
        m_error = true;
        log().error(getTableName()+": Unexpected Error occurred processing "+getTableName()+" for "+m_address, e);
        signal();
    }

    public String getTableName() {
        return m_tableName;
    }

    private void handleDone() {
        signal();
    }

    private void handleError(String msg) {
        m_error = true;
        log().info(getTableName()+": Error retrieving "+getTableName()+" for "+m_address+": "+msg);
        signal();
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
        if (!processV1Pdu(pdu)) {
            sendV1Pdu(session, pdu);
            return false;
        } else {
            return true;
        }
    }

    private boolean onV2Response(SnmpSession session, SnmpPduPacket pdu) throws InvalidResponseException {
        
        // in case we did not receive all the data from the
        // first packet, must generate a new GETBULK packet
        // starting at the OID the previous one left off.
        //
        if (!processV2Pdu(pdu)) {
            sendV2Pdu(session, pdu);
            return false;
        } else {
            processV2Complete();
            return true;
        }
    }

    private boolean isV1Finished(SnmpPduPacket pdu) {
        return !(stop_oid().compare(pdu.getVarBindAt(pdu.getLength() - 1).getName()) > 0);
    }

    private boolean processV1Pdu(SnmpPduPacket pdu) {
        
        if (isV1Finished(pdu)) {
            return true;
        } else {
            if (log().isDebugEnabled())
                log().debug(getTableName()+": snmpReceivedPdu: got SNMPv1 response and still within "+getTableName()+", creating new entry.");
            processTableRow(pdu.toVarBindArray());
            return false;
        }
    }

    private void sendV1Pdu(SnmpSession session, SnmpPduPacket pdu) {
        
        if (pdu == null) {
            SnmpPduPacket firstPdu = getNextV1Pdu();
            log().debug(getTableName()+": initial pdu request id: " + firstPdu.getRequestId());
            session.send(firstPdu, this);
        } else {
            SnmpPduRequest nxt = new SnmpPduRequest(SnmpPduPacket.GETNEXT);
            for (int x = 0; x < pdu.getLength(); x++) {
                nxt.addVarBind(new SnmpVarBind(pdu.getVarBindAt(x).getName()));
            }
            nxt.setRequestId(SnmpPduPacket.nextSequence());
            session.send(nxt, this);
        }
    }

    private boolean processV2Pdu(SnmpPduPacket pdu) throws InvalidResponseException {
        checkForErrorVarbinds(pdu);
        for (int x = 0; x < pdu.getLength(); x++) {
            SnmpVarBind vb = pdu.getVarBindAt(x);
            m_snmpVarBindList.add(vb);
        }
        return isV2Finished(pdu);
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
        int numRows = 0;
        SnmpObjectId firstCol = new SnmpObjectId(getColumns()[0].getOid());
        for(int i = 0; i < tempStorage.length; i++) {
            if (firstCol.isRootOf(tempStorage[i].getName()))
                numRows++;
        }
        
        // store the IP Address Table data for each
        // interface into a map.
        //
        int numCols = getColumns().length;
        for (int row = 0; row < numRows; row++) {
            SnmpVarBind[] rowVarbinds = new SnmpVarBind[numCols];
            for (int col = 0; col < numCols; col++) {
                rowVarbinds[col] = tempStorage[row + (col * numRows)];
            }
            processTableRow(rowVarbinds);
        }
    }

    private boolean isV2Finished(SnmpPduPacket pdu) {
        return !(stop_oid().compare(pdu.getVarBindAt(pdu.getLength() - 1).getName()) > 0);
    }

    private void sendV2Pdu(SnmpSession session, SnmpPduPacket pdu) {
        if (pdu == null) {
            SnmpPduPacket firstPdu = getNextV2Pdu();
            log().debug(getTableName()+": initial pdu request id: " + firstPdu.getRequestId());
            session.send(firstPdu, this);
        } else {
            SnmpObjectId id = new SnmpObjectId(pdu.getVarBindAt(pdu.getLength() - 1).getName());
            SnmpVarBind[] newvblist = { new SnmpVarBind(id) };
            SnmpPduPacket nxt = new SnmpPduBulk(0, getMaxRepeaters(), newvblist);
            nxt.setRequestId(SnmpPduPacket.nextSequence());
            session.send(nxt, this);
        }
    }

    private int getMaxRepeaters() {
        return Math.min(50, getColumns().length * 3);
    }

    protected void checkForPduError(SnmpPduPacket pdu) throws InvalidResponseException {
        //
        // Check for error stored in request pdu
        //
        int errStatus = ((SnmpPduRequest) pdu).getErrorStatus();
        if (errStatus != SnmpPduPacket.ErrNoError) {
            throw new InvalidResponseException(getTableName()+": Error received from "+m_address+" errStatus="+errStatus);
        }
    }

    private void checkForResponsePdu(int command) throws InvalidResponseException {
        if (command != SnmpPduPacket.RESPONSE) {
            throw new InvalidResponseException(getTableName()+": Unexpected packet from "+m_address+" command = "+command);
        }
    }

    protected int checkForErrorVarbinds(SnmpPduPacket pdu) throws InvalidResponseException {
        int numVarBinds = pdu.getLength();
        for (int x = 0; x < numVarBinds; x++) {
            SnmpVarBind vb = pdu.getVarBindAt(x);
    
            if (vb.getValue() instanceof org.opennms.protocols.snmp.SnmpV2Error) {
                throw new InvalidResponseException(getTableName()+": snmpReceivedPDU: varbind: " + vb.getName() + "  error: '" + vb.getValue() + "'");
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
        handleError(getTableName()+": snmpInternalError: " + error + " for: " + m_address);
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
        handleError(getTableName()+": snmpTimeoutError for: " + session.getPeer().getPeer());
    }

    public SnmpPduPacket getNextV1Pdu() {
        SnmpPduPacket pdu;
        pdu = new SnmpPduRequest(SnmpPduPacket.GETNEXT);
        pdu.setRequestId(SnmpPduPacket.nextSequence());
        NamedSnmpVar[] elements = getColumns();
        for (int x = 0; x < elements.length; x++) {
            SnmpObjectId oid = new SnmpObjectId(elements[x].getOid());
            pdu.addVarBind(new SnmpVarBind(oid));
        }
        return pdu;
    }

    public SnmpPduPacket getNextV2Pdu() {
        SnmpPduPacket pdu;
        pdu = new SnmpPduBulk();
        ((SnmpPduBulk) pdu).setMaxRepititions(getMaxRepeaters());
        pdu.setRequestId(SnmpPduPacket.nextSequence());
        SnmpObjectId oid = getRootOfTable();
        pdu.addVarBind(new SnmpVarBind(oid));
        return pdu;
    }

    private SnmpObjectId getRootOfTable() {
        return new SnmpObjectId(m_rootOid);
    }

    /**
     * <P>
     * This method will determine where the cut off point will be for valid data
     * from the response to the GETBULK packet. By using the size of the element
     * list, listed above, we can determine the proper index for this task.
     * <P>
     */
    public SnmpObjectId stop_oid() {
         SnmpObjectId id = getRootOfTable();
         id.append(Integer.toString(getColumns().length+1));
         return id;
    }

    private NamedSnmpVar[] getColumns() {
        return m_columns;
    }
    
    private final Category log() {
        return ThreadCategory.getInstance(SnmpTableWalker.class);
    }



}
