//
// Copyright (C) 2000 Daniel Balmer <dba@elca.ch>
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
// Tab Size = 8
//
// snmpagent.java,v 1.1.1.1 2001/11/11 17:35:57 ben Exp
//
// Log:
//	05/14/01 - Shane O'Donnell <shaneo@opennms.org>
//		   Commented, got it to build, and  added file to CVS

package org.opennms.test;

// Imports from Java API
import java.net.InetAddress;

// Imports from JoeSNMP API
import org.opennms.protocols.snmp.SnmpHandler;
import org.opennms.protocols.snmp.SnmpIPAddress;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpOctetString;
import org.opennms.protocols.snmp.SnmpParameters;
import org.opennms.protocols.snmp.SnmpPduPacket;
import org.opennms.protocols.snmp.SnmpPduRequest;
import org.opennms.protocols.snmp.SnmpPduTrap;
import org.opennms.protocols.snmp.SnmpPeer;
import org.opennms.protocols.snmp.SnmpSession;
import org.opennms.protocols.snmp.SnmpSMI;
import org.opennms.protocols.snmp.SnmpSyntax;
import org.opennms.protocols.snmp.SnmpTimeTicks;
import org.opennms.protocols.snmp.SnmpVarBind;

/**
 * <code>snmpagent</code> is a class that manages SNMP functionalities. <P>
 *
 * Currently, it allows to send V1 or V2 traps to one (and only one) SNMP
 * manager defined by its IP address and port number.
 *
 */
 public class snmpagent implements SnmpHandler {

    /** SNMP parameter for community */
    private static final String PARAM_COMMUNITY = "public";

    /** SNMP parameter for number of retries */
    private static final int PARAM_RETRIES = 10;

    /** SNMP parameter for timeout */
    private static final int PARAM_TIMEOUT = 5000;

    /** SNMP parameter for generic type (V1 only) */
    private static final int PARAM_GENERIC_TYPE = 0;

    /** SNMP parameter for specific type (V1 only) */
    private static final int PARAM_SPECIFIC_TYPE = 0;

    /** SNMP OID of the sent trap */
    private String m_trapOid;

    /** Holds the reference to the SNMP session */
    private SnmpSession m_session;

    /**
     * Builds an snmpagent object for sending SNMP V1 or V2 traps. <P>
     *
     * Remark : The SNMP trap type (V1 or V2) will be set every time a trap is 
     *          sent in the respectie send method.
     *
     * @param managerAddress the IP address where the traps are sent
     * @param managerPort the port number where the traps are sent (e.g. 162)
     * @param trapOid the complete OID for the trap (e.g. "1.3.6.1.2.1.1232.1.1")
     * @exception Exception if an error occurs during the initialization
     *            of the object
     */
    public snmpagent(String managerAddress, int managerPort,
            String trapOid) throws Exception {

        // Memorize attributes
        m_trapOid = trapOid;

        // Build the SNMP session
        try {
            // Create the peer object
            SnmpPeer peer = new SnmpPeer(InetAddress.getByName(managerAddress));
			peer.setPort(managerPort);
            peer.setRetries(PARAM_RETRIES);
            peer.setTimeout(PARAM_TIMEOUT);
            SnmpParameters parameters = peer.getParameters();
            parameters.setReadCommunity(PARAM_COMMUNITY);
			
            // Create the session object
            m_session = new SnmpSession(peer);
            m_session.setDefaultHandler(this);
        } catch (Exception e) {
            System.out.println("SNMP agent creation error");
        }
    }
    
    /**
     * Closes the SNMP session.
     */
    public void close() {
        m_session.close();
    }

	public static void main(String[] args) {
		
	/**
	 *  Please note:  There is no functional "main".  But the
	 *                code "should" work if it had one...
	 *
	 */
    }
		

    /**
     * Sends a formatted message as SNMP V1 trap.
     *
     * @param genericType the generic type of the trap
     * @param specificType the specific type of the trap
     * @param message the formatted text to send
     * @exception Exception if an error occurs during the sending of the trap
     */
    public void sendV1Trap(String message)
            throws Exception {

        try {
            // Set SNMP version as V1
            SnmpPeer peer = m_session.getPeer();
            SnmpParameters parameters = peer.getParameters();
            parameters.setVersion(SnmpSMI.SNMPV1);

            // Create trap
            SnmpPduTrap trapPdu = new SnmpPduTrap();
            trapPdu.setAgentAddress(new SnmpIPAddress(InetAddress.getLocalHost().getAddress()));
            trapPdu.setGeneric(PARAM_GENERIC_TYPE);
            trapPdu.setSpecific(PARAM_SPECIFIC_TYPE);
            trapPdu.addVarBind(new SnmpVarBind(new SnmpObjectId(m_trapOid),
                    new SnmpOctetString(message.getBytes())));
			
            // Send trap
            m_session.send(trapPdu);

        } catch (Exception e) {
            System.out.println("SNMP send error");
        }
    }

    /**
     * Sends a formatted message as SNMP V2 trap.
     *
     * @param message the formatted text to send
     * @exception Exception if an error occurs during the sending of the trap
     */
    public void sendV2Trap(String message) {

        try {
            // Set SNMP version as V2
            SnmpPeer peer = m_session.getPeer();
            SnmpParameters parameters = peer.getParameters();
            parameters.setVersion(SnmpSMI.SNMPV2);

            // Create trap
            SnmpPduRequest trapPdu = new SnmpPduRequest(SnmpPduPacket.V2TRAP);
            trapPdu.addVarBind(new SnmpVarBind(new SnmpObjectId(m_trapOid),
                    new SnmpOctetString(message.getBytes())));
					
            // Send trap
            m_session.send(trapPdu);

        } catch (Exception e) {
            System.out.println("SNMP send error");
        }
    }

    //
    // All stubs since we are only sending
    //
    public void snmpInternalError(SnmpSession session, int err, SnmpSyntax pdu) {
        System.out.println("SNMP internal error, code: " + err);
    }
	
    public void snmpTimeoutError(SnmpSession session, SnmpSyntax pdu) {
        System.out.println("SNMP timeout");
    }
	
    public void snmpReceivedPdu(SnmpSession session, int cmd, SnmpPduPacket pdu) {
        // Do nothing (we're not interested in incoming SNMP messages)
    }
}
