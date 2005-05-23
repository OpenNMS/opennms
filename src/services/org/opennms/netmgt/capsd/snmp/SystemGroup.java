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

import org.apache.log4j.Priority;
import org.opennms.netmgt.utils.Signaler;
import org.opennms.protocols.snmp.SnmpHandler;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpOctetString;
import org.opennms.protocols.snmp.SnmpPduPacket;
import org.opennms.protocols.snmp.SnmpPduRequest;
import org.opennms.protocols.snmp.SnmpSession;
import org.opennms.protocols.snmp.SnmpSyntax;
import org.opennms.protocols.snmp.SnmpVarBind;

/**
 * <P>
 * SystemGroup holds the system group properties It implements the SnmpHandler
 * to receive notifications when a reply is received/error occurs in the
 * SnmpSession used to send requests /recieve replies.
 * </P>
 * 
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213 </A>
 */
public final class SystemGroup extends SnmpStore implements SnmpHandler {
    //
    // Lookup strings for specific table entries
    //
    public final static String SYS_OBJECTID = "sysObjectID";

    public final static String SYS_UPTIME = "sysUptime";

    public final static String SYS_NAME = "sysName";

    public final static String SYS_DESCR = "sysDescr";

    public final static String SYS_LOCATION = "sysLocation";

    public final static String SYS_CONTACT = "sysContact";
    
    /**
     * <P>
     * The keys that will be supported by default from the TreeMap base class.
     * Each of the elements in the list are an instance of the SNMP Interface
     * table. Objects in this list should be used by multiple instances of this
     * class.
     * </P>
     */
    public static NamedSnmpVar[] ms_elemList = null;

    /**
     * <P>
     * Initialize the element list for the class. This is class wide data, but
     * will be used by each instance.
     * </P>
     */
    static {
        // Changed array size from 7 to 6 because we are no longer going after
        // sysServices...sysServices is not currently being used and it causes
        // the entire SystemGroup collection to fail on at least one version
        // of Linux where it does not exist in the SNMP agent.
        //
        ms_elemList = new NamedSnmpVar[6];
        int ndx = 0;

        /**
         * <P>
         * A description of the remote entity. For example this may include
         * hardware, opererating system, and various version information. This
         * should be a US-ASCII display string.
         * </P>
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, SYS_DESCR, ".1.3.6.1.2.1.1.1");

        /**
         * <P>
         * The vendor's authoritative identification of the network management
         * subsystem. This can often be used to identify the vendor, and often
         * times the specific vendor's hardware platform.
         * </P>
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOBJECTID, SYS_OBJECTID, ".1.3.6.1.2.1.1.2");

        /**
         * <P>
         * The time since the network management portion of the system was last
         * initialized. This will be in 1/100th of a second increments.
         * </P>
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPTIMETICKS, SYS_UPTIME, ".1.3.6.1.2.1.1.3");

        /**
         * <P>
         * The identification and contact information for the person that is
         * managing this node. While the contact information is often used to
         * store contact information about the person managing the node, it is a
         * free form US-ASCII field that may contain additional information
         * depending on the environment.
         * </P>
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, SYS_CONTACT, ".1.3.6.1.2.1.1.4");

        /**
         * <P>
         * The administratively assigned name for this particular node. This may
         * often be the same as the hostname, but it can differ depending on the
         * site's implementation.
         * </P>
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, SYS_NAME, ".1.3.6.1.2.1.1.5");

        /**
         * <P>
         * The physical location of the node. This field, like many others, is a
         * free formed US-ASCII field that can contain any type of location
         * string. Some sites might acutually use a special encoding that
         * designates the state, city, building, floor, and room that contains
         * the equipment.
         * </P>
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, SYS_LOCATION, ".1.3.6.1.2.1.1.6");

        /**
         * <P>
         * A value that indicates the set of services that this entity provides.
         * This is a bit encode integer that allows the management entity to
         * determ if the agent supports the following standards
         * </P>
         * 
         * <UL>
         * <LI>physical (e.g. repeaters)</LI>
         * <LI>datalink/subnetwork (e.g. bridges)</LI>
         * <LI>internet (e.g. routers)</LI>
         * <LI>end-to-end (e.g. IP hosts)</LI>
         * <LI>applications (e.g. mail relays)</LI>
         * </UL>
         * 
         * <P>
         * To get more information about the encoding see Page 123 of "SNMP,
         * SNMPv2, SNMPv3 and RMON 1 and 2 3rd Ed." by William Stallings [ISBN
         * 0-201-48534-6]
         * </P>
         */
        // ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
        // "sysServices", ".1.3.6.1.2.1.1.7");
    }

    /**
     * <P>
     * Flag indicating the success or failure of the informational query. If the
     * flag is set to false then either part of all of the information was
     * unable to be retreived. If it is set to true then all of the data was
     * received from the remote host.
     * </P>
     */
    public boolean m_error;

    /**
     * <P>
     * The SYSTEM_OID is the object identifier that represents the root of the
     * system information in the MIB forest. Each of the system elements can be
     * retreived by adding their specific index to the string, and an additional
     * Zero(0) to signify the single instance item.
     * </P>
     */
    public static final String SYSTEM_OID = ".1.3.6.1.2.1.1";

    /**
     * <P>
     * The SnmpObjectId that represents the root of the system tree. It is
     * created at initilization time and is the converted value of SYSTEM_OID.
     * </P>
     * 
     * @see #SYSTEM_OID
     */
    public static final SnmpObjectId ROOT = new SnmpObjectId(SYSTEM_OID);

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
     * The class constructor is used to initialize the collector and send out
     * the initial SNMP packet requesting data. The data is then received and
     * store by the object. When all the data has been collected the passed
     * signaler object is <EM>notified</em> using the notifyAll() method.
     * </P>
     * @param session
     *            The SNMP session with the remote agent.
     * @param address TODO
     * @param signaler
     *            The object signaled when data collection is done.
     * 
     */
    public SystemGroup(SnmpSession session, InetAddress address, Signaler signaler) {
        super(ms_elemList);

        m_error = false;

        m_signal = signaler;
        
        SnmpPduPacket pdu = getPdu();
        pdu.setRequestId(SnmpPduPacket.nextSequence());
        session.send(pdu, this);
    }

    /**
     * <P>
     * This method is used to update the map with the current information from
     * the agent.
     * 
     * </P>
     * This does not clear out any column in the actual row that does not have a
     * definition.
     * </P>
     * 
     * @param vars
     *            The variables in the interface row.
     * 
     */
    public void update(SnmpVarBind[] vars) {
        //
        // iterate through the variable bindings
        // and set the members appropiately.
        //
        for (int x = 0; x < ms_elemList.length; x++) {
            SnmpObjectId id = new SnmpObjectId(ms_elemList[x].getOid());
            for (int y = 0; y < vars.length; y++) {
                if (id.isRootOf(vars[y].getName())) {
                    try {
                        //
                        // Retrieve the class object of the expected SNMP data
                        // type for this element
                        //
                        Class classObj = ms_elemList[x].getTypeClass();

                        //
                        // If the classes match then it is the type we expected
                        // so
                        // go ahead and store the information.
                        //
                        if (classObj.isInstance(vars[y].getValue())) {
                            put(ms_elemList[x].getAlias(), vars[y].getValue());
                            put(ms_elemList[x].getOid(), vars[y].getValue());
                        } else {
                            //
                            // reset the values
                            //
                            put(ms_elemList[x].getAlias(), null);
                            put(ms_elemList[x].getOid(), null);
                        }
                    } catch (ClassNotFoundException e) {
                        log().error("Failed retrieving SNMP type class for element: " + ms_elemList[x].getAlias(), e);
                    } catch (NullPointerException e) {
                        log().error("Invalid reference", e);
                    }

                    break;
                }
            }
        }
    }

    /**
     * <P>
     * This method is used to build the initial SNMP PDU that is sent to the
     * remote host. The PDU contains as many variable bindings as needed by the
     * object. The varbinds are SnmpNull objects that have been initialized each
     * with one instance of a required variable. The PDU type is marked as GET
     * so that only a single Request/Response is required to get all the data.
     * </P>
     * 
     * @return An SnmpPduRequest with the command GET and a predefined varbind
     *         list.
     * 
     * @see org.opennms.protocols.snmp.SnmpNull SnmpNull
     * @see org.opennms.protocols.snmp.SnmpPduRequest SnmpPduRequest
     */
    public static SnmpPduRequest getPdu() {
        SnmpPduRequest pdu = new SnmpPduRequest(SnmpPduRequest.GET);
        for (int x = 1; x <= ms_elemList.length; x++) {
            SnmpObjectId oid = new SnmpObjectId(SYSTEM_OID + "." + x + ".0");
            pdu.addVarBind(new SnmpVarBind(oid));
        }
        return pdu;
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
        if (command == SnmpPduPacket.RESPONSE) {
            //
            // Check for SNMPv1 error stored in request pdu
            //
            int errStatus = ((SnmpPduRequest) pdu).getErrorStatus();
            if (errStatus != SnmpPduPacket.ErrNoError) {
                int errIndex = ((SnmpPduRequest) pdu).getErrorIndex();
                //
                // If first varbind had error (sysDescription) then we will
                // assume
                // that nothing was collected for system group. If the error
                // occurred
                // later in the varbind list lets proceed since this information
                // is
                // useful (older SNMP agents won't have sysServices implemented
                // for example).
                //
                if (errIndex == 1)
                    m_error = true;
            }

            if (!m_error) {
                SnmpVarBind[] vars = pdu.toVarBindArray();
                update(vars);
            }

        } else // It was an invalid PDU
        {
            m_error = true;
        }

        signal();
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
        if (log().isEnabledFor(Priority.WARN)) {
            log().warn("snmpInternalError: The session experienced an internal error, error = " + error);
        }

        m_error = true;

        signal();
    }

    public void setSignaler(Signaler sig) {
        m_signal = sig;
    }
    
    private void signal() {
        synchronized (this) {
            this.notifyAll();
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
        if (log().isEnabledFor(Priority.WARN)) {
            log().warn("snmpTimeoutError: The session timed out communicating with the agent.");
        }

        m_error = true;

        signal();
    }

    /**
     * <P>
     * Returns the success or failure code for collection of the data.
     * </P>
     */
    public boolean failed() {
        return m_error;
    }

    public String getSysName() {
        return SnmpOctetString.toDisplayString((SnmpOctetString) get(SystemGroup.SYS_NAME));
    }

    public String getSysObjectID() {
        return (get(SystemGroup.SYS_OBJECTID) == null ? null : get(SystemGroup.SYS_OBJECTID).toString());
    }

    public String getSysDescr() {
        return SnmpOctetString.toDisplayString((SnmpOctetString) get(SystemGroup.SYS_DESCR));
    }

    public String getSysLocation() {
        return SnmpOctetString.toDisplayString((SnmpOctetString) get(SystemGroup.SYS_LOCATION));
    }

    public String getSysContact() {
        return SnmpOctetString.toDisplayString((SnmpOctetString) get(SystemGroup.SYS_CONTACT));
    }
    
}
