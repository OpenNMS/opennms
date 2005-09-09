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

package org.opennms.netmgt.linkd.snmp;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.opennms.core.utils.ThreadCategory;
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
 * <P>SystemGroup holds the system group properties
 * It implements the SnmpHandler to receive notifications when a reply is
 * received/error occurs in the SnmpSession used to send requests /recieve
 * replies.</P>
 *
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio Russo</A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya</A>
 * @author <A HREF="mailto:weave@oculan.com">Weave</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 */
public final class Dot1dStpGroup extends java.util.TreeMap implements
		SnmpHandler {
	//
	// Lookup strings for specific table entries
	//
	public final static String STP_PROTOCOL_SPEC = "dot1dStpProtocolSpecification";

	public final static String STP_PRIORITY = "dot1dStpPriority";

	public final static String STP_TIME_LASTTOPCH = "dot1dStpTimeSinceLastTopologyChange";

	public final static String STP_TOP_CHANGES = "dot1dStpTopChanges";

	public final static String STP_DESIGNATED_ROOT = "dot1dStpDesignatedRoot";

	public final static String STP_ROOT_COST = "dot1dStpRootCost";

	public final static String STP_ROOT_PORT = "dot1dStpRootPort";

	public final static String STP_MAX_AGE = "dot1dStpMaxAge";

	public final static String STP_HELLO_TIME = "dot1dStpHelloTime";

	public final static String STP_HOLD_TIME = "dot1dStpHoldTime";

	public final static String STP_FORW_DELAY = "dot1dStpForwardDelay";

	public final static String STP_BRDG_MAX_AGE = "dot1dStpBridgeMaxAge";

	public final static String STP_BRDG_HELLO_TIME = "dot1dStpBridgeHelloTime";

	public final static String STP_BRDG_FORW_DELAY = "dot1dStpBridgeForwardDelay";

	/**
	 * <P>The keys that will be supported by default from the 
	 * TreeMap base class. Each of the elements in the list
	 * are an instance of the SNMP Interface table. Objects
	 * in this list should be used by multiple instances of
	 * this class.</P>
	 */
	private static NamedSnmpVar[] ms_elemList = null;

	/**
	 * <P>Initialize the element list for the class. This
	 * is class wide data, but will be used by each instance.</P>
	 */
	static {
		// Array size 14 elements 
		//
		ms_elemList = new NamedSnmpVar[14];
		int ndx = 0;

		/**
		 * <P>An indication of what version of the Spanning
		 *  Tree Protocol is being run. The value
		 *  'decLb100(2)' indicates the DEC LANbridge 100
		 *  Spanning Tree protocol. IEEE 802.1d
		 *  implementations will return 'ieee8021d(3)'. If
		 *  future versions of the IEEE Spanning Tree Protocol
		 *  are released that are incompatible with the
		 *  current version a new value will be defined.</P>
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				STP_PROTOCOL_SPEC, ".1.3.6.1.2.1.17.2.1");

		/**
		 * <P> The value of the write-able portion of the Bridge
		 *  ID, i.e., the first two octets of the (8 octet
		 *  long) Bridge ID. The other (last) 6 octets of the
		 *  Bridge ID are given by the value of
		 *  dot1dBaseBridgeAddress.</P>
		 * 
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				STP_PRIORITY, ".1.3.6.1.2.1.17.2.2");

		/**
		 * <P>The time (in hundredths of a second) since the
		 *  last time a topology change was detected by the
		 * bridge entity</P>.
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPTIMETICKS,
				STP_TIME_LASTTOPCH, ".1.3.6.1.2.1.17.2.3");

		/**
		 * <P>The total number of topology changes detected by
		 *  this bridge since the management entity was last
		 *  reset or initialized.</P>
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,
				STP_TOP_CHANGES, ".1.3.6.1.2.1.17.2.4");
	
		/**
		 * <P>The bridge identifier of the root of the spanning
		 *  tree as determined by the Spanning Tree Protocol
		 *  as executed by this node. This value is used as
		 *  the Root Identifier parameter in all Configuration
		 *  Bridge PDUs originated by this node.</P>
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING,
				STP_DESIGNATED_ROOT, ".1.3.6.1.2.1.17.2.5");
	
		/**
		 * <P>The cost of the path to the root as seen from
         * this bridge.</P>
		 * 
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				STP_ROOT_COST, ".1.3.6.1.2.1.17.2.6");
	
		/**
		 * <P>The port number of the port which offers the
		 * lowest cost path from this bridge to the root
 		 * bridge.</P>
		 * 
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				STP_ROOT_PORT, ".1.3.6.1.2.1.17.2.7");

		/**
		 * <P>The maximum age of Spanning Tree Protocol
		 * information learned from the network on any port
		 * before it is discarded, in units of hundredths of
		 * a second. This is the actual value that this
		 * bridge is currently using.</P>
		 * 
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				STP_MAX_AGE, ".1.3.6.1.2.1.17.2.8");

		/**
		 * <P>The amount of time between the transmission of
 		 * Configuration bridge PDUs by this node on any port
 		 * when it is the root of the spanning tree or trying
 		 * to become so, in units of hundredths of a second.
 		 * This is the actual value that this bridge is
 		 * currently using.</P>
		 * 
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				STP_HELLO_TIME, ".1.3.6.1.2.1.17.2.9");
		
		/**
		 * <P>This time value determines the interval length
 		 * during which no more than two Configuration bridge
 		 * PDUs shall be transmitted by this node, in units
 		 * of hundredths of a second.</P>
		 * 
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				STP_HOLD_TIME, ".1.3.6.1.2.1.17.2.10");

		/**
		 * <P>This time value, measured in units of hundredths
 		 * of a second, controls how fast a port changes its
 		 * spanning state when moving towards the Forwarding
 		 * state. The value determines how long the port
 		 * stays in each of the Listening and Learning
 		 * states, which precede the Forwarding state. This
 		 * value is also used, when a topology change has
 		 * been detected and is underway, to age all dynamic
 		 * entries in the Forwarding Database. [Note that
 		 * this value is the one that this bridge is
 		 * currently using, in contrast to
 		 * dot1dStpBridgeForwardDelay which is the value that
 		 * this bridge and all others would start using
 		 * if/when this bridge were to become the root.]</P>
		 * 
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				STP_FORW_DELAY, ".1.3.6.1.2.1.17.2.11");

		/**
		 * <P>The value that all bridges use for MaxAge when
		 *  this bridge is acting as the root. Note that
 		 * 802.1D-1990 specifies that the range for this
 		 * parameter is related to the value of
 		 * dot1dStpBridgeHelloTime. The granularity of this
 		 * timer is specified by 802.1D-1990 to be 1 second.
 		 * An agent may return a badValue error if a set is
 		 * attempted to a value which is not a whole number
 		 * of seconds.</P>
		 * 
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				STP_BRDG_MAX_AGE, ".1.3.6.1.2.1.17.2.12");

		/**
		 * <P>The value that all bridges use for HelloTime when
 		 * this bridge is acting as the root. The
 		 * granularity of this timer is specified by 802.1D-
 		 * 1990 to be 1 second. An agent may return a
 		 * badValue error if a set is attempted to a value
 		 * which is not a whole number of seconds.</P>
		 * 
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				STP_BRDG_HELLO_TIME, ".1.3.6.1.2.1.17.2.13");
		
		/**
		 * <P>The value that all bridges use for ForwardDelay
		 *  when this bridge is acting as the root. Note that
 		 * 802.1D-1990 specifies that the range for this
 		 * parameter is related to the value of
 		 * dot1dStpBridgeMaxAge. The granularity of this
 		 * timer is specified by 802.1D-1990 to be 1 second.
 		 * An agent may return a badValue error if a set is
 		 * attempted to a value which is not a whole number
 		 * of seconds.</P>
		 * 
		 */
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				STP_BRDG_FORW_DELAY, ".1.3.6.1.2.1.17.2.14");
		

	}


	/**
	 * <P>Flag indicating the success or failure of the
	 * informational query. If the flag is set to false
	 * then either part of all of the information was
	 * unable to be retreived. If it is set to true then
	 * all of the data was received from the remote host.</P>
	 */
	public boolean m_error;

	/**
	 * <P>The SYSTEM_OID is the object identifier that represents the
	 * root of the system information in the MIB forest. Each of the
	 * system elements can be retreived by adding their specific index
	 * to the string, and an additional Zero(0) to signify the single 
	 * instance item.</P>
	 */
	public static final String SYSTEM_OID = ".1.3.6.1.2.1.17.2";

	/**
	 * <P>The SnmpObjectId that represents the root of the system
	 * tree. It is created at initilization time and is the converted
	 * value of SYSTEM_OID.</P>
	 *
	 * @see #SYSTEM_OID
	 */
	public static final SnmpObjectId ROOT = new SnmpObjectId(SYSTEM_OID);

	/**
	 * <P>Used to synchronize the class to ensure that the
	 * session has finished collecting data before the
	 * value of success or failure is set, and control
	 * is returned to the caller.</P>
	 */
	private Signaler m_signal;

	/**
	 * <P>The default constructor is marked private and will
	 * always throw an exception. This is done to disallow
	 * the default constructor. The reason is that this
	 * object requires an SNMP session and a synchronization
	 * object to perform it's duties.
	 *
	 * @exception java.lang.UnsupportedOperationException Always thrown from
	 *	this method since it is not supported.
	 *
	 * @see #Dot1dStpGroup(SnmpSession, Signaler)
	 */
	private Dot1dStpGroup() throws UnsupportedOperationException {
		throw new UnsupportedOperationException(
				"Default Constructor not supported");
	}

	/**
	 * <P>The class constructor is used to initialize the collector
	 * and send out the initial SNMP packet requesting data. The
	 * data is then received and store by the object. When all the
	 * data has been collected the passed signaler object is <EM>notified</em>
	 * using the notifyAll() method.</P>
	 *
	 * @param session	The SNMP session with the remote agent.
	 * @param signaler	The object signaled when data collection is done.
	 *
	 */
	public Dot1dStpGroup(SnmpSession session, Signaler signaler) {
		super();

		m_error = false;

		m_signal = signaler;
		SnmpPduPacket pdu = getPdu();
		pdu.setRequestId(SnmpPduPacket.nextSequence());
		session.send(pdu, this);
	}

	/**
	 * <P>This method is used to update the map
	 * with the current information from the agent.
	 *
	 * </P>This does not clear out any column in the
	 * actual row that does not have a definition.</P>
	 *
	 * @param vars	The variables in the interface row.
	 *
	 */
	public void update(SnmpVarBind[] vars) {
		//
		// iterate through the variable bindings
		// and set the members appropiately.
		//
		Category log = ThreadCategory.getInstance(getClass());

		for (int x = 0; x < ms_elemList.length; x++) {
			SnmpObjectId id = new SnmpObjectId(ms_elemList[x].getOid());
			for (int y = 0; y < vars.length; y++) {
				if (id.isRootOf(vars[y].getName())) {
					try {
						//
						// Retrieve the class object of the expected SNMP data type for this element
						//
						Class classObj = ms_elemList[x].getTypeClass();

						//
						// If the classes match then it is the type we expected so 
						// go ahead and store the information.
						//
						if (classObj.isInstance(vars[y].getValue())) {
							if (log.isDebugEnabled()) {
								log.debug("Dot1dStpGroup: Types match!  SNMP Alias: "
										+ ms_elemList[x].getAlias()
										+ "  Vars[y]: " + vars[y].toString());
							}
							put(ms_elemList[x].getAlias(), vars[y].getValue());
							put(ms_elemList[x].getOid(), vars[y].getValue());
						} else {
							if (log.isDebugEnabled()) {
								log.debug("Dot1dStpGroup: variable '"
										+ vars[y].toString()
										+ "' does NOT match expected type '"
										+ ms_elemList[x].getType() + "'");
							}
							//
							// reset the values
							//
							put(ms_elemList[x].getAlias(), null);
							put(ms_elemList[x].getOid(), null);
						}
					} catch (ClassNotFoundException e) {
						log.error(
								"Failed retrieving SNMP type class for element: "
										+ ms_elemList[x].getAlias(), e);
					} catch (NullPointerException e) {
						log.error("Invalid reference", e);
					}

					break;
				}
			}
		}
	}

	/**
	 * <P>This method is used to build the initial SNMP PDU
	 * that is sent to the remote host. The PDU contains 
	 * as many variable bindings as needed by the object.
	 * The varbinds are SnmpNull objects that have been initialized
	 * each with one instance of a required variable. The
	 * PDU type is marked as GET so that only a single Request/Response
	 * is required to get all the data.</P>
	 *
	 * @return An SnmpPduRequest with the command GET and a 
	 *	predefined varbind list.
	 *
	 * @see org.opennms.protocols.snmp.SnmpNull		SnmpNull
	 * @see org.opennms.protocols.snmp.SnmpPduRequest 	SnmpPduRequest
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
	 * <P>This method is used to process received SNMP PDU packets from
	 * the remote agent. The method is part of the SnmpHandler interface
	 * and will be invoked when a PDU is successfully decoded. The method
	 * is passed the receiving session, the PDU command, and the actual
	 * PDU packet.</P>
	 *
	 * <P>When all the data has been received from the session the signaler
	 * object, initialized in the constructor, is signaled. In addition,
	 * the receiving instance will call notifyAll() on itself at the same
	 * time.</P>
	 *
	 * @param session	The SNMP Session that received the PDU
	 * @param command	The command contained in the received pdu
	 * @param pdu		The actual received PDU.
	 *
	 */
	public void snmpReceivedPdu(SnmpSession session, int command,
			SnmpPduPacket pdu) {
		if (command == SnmpPduPacket.RESPONSE) {
			//
			// Check for SNMPv1 error stored in request pdu
			//
			int errStatus = ((SnmpPduRequest) pdu).getErrorStatus();
			if (errStatus != SnmpPduPacket.ErrNoError) {
				int errIndex = ((SnmpPduRequest) pdu).getErrorIndex();
				//
				// If first varbind had error (sysDescription) then we will assume
				// that nothing was collected for system group.  If the error occurred
				// later in the varbind list lets proceed since this information is
				// useful (older SNMP agents won't have sysServices implemented for example).
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

		//
		// Signal anyone waiting
		//
		if (m_signal != null) {
			synchronized (m_signal) {
				m_signal.signalAll();
			}
		}

		//
		// notify anyone waiting on this
		// particular object
		//
		synchronized (this) {
			this.notifyAll();
		}
	}

	/**
	 * <P>This method is part of the SnmpHandler interface and called when
	 * an internal error happens in a session. This is usually the result
	 * of an I/O error. This method will not be called if the session times
	 * out sending a packet, see snmpTimeoutError for timeout handling.</P>
	 *
	 * @param session	The session that had an unexpected error
	 * @param error		The error condition
	 * @param pdu		The PDU being sent when the error occured
	 *
	 * @see #snmpTimeoutError
	 * @see org.opennms.protocols.snmp.SnmpHandler SnmpHandler
	 */
	public void snmpInternalError(SnmpSession session, int error, SnmpSyntax pdu) {
		Category log = ThreadCategory.getInstance(getClass());
		if (log.isEnabledFor(Priority.WARN)) {
			log
					.warn("snmpInternalError: The session experienced an internal error, error = "
							+ error);
		}

		m_error = true;

		if (m_signal != null) {
			synchronized (m_signal) {
				m_signal.signalAll();
			}
		}

		synchronized (this) {
			this.notifyAll();
		}
	}

	/**
	 * <P>This method is part of the SnmpHandler interface and is invoked
	 * when the SnmpSession does not receive a reply after exhausting 
	 * the retransmission attempts.</P>
	 *
	 * @param session	The session invoking the error handler
	 * @param pdu		The PDU that the remote failed to respond to.
	 *
	 * @see org.opennms.protocols.snmp.SnmpHandler SnmpHandler
	 *
	 */
	public void snmpTimeoutError(SnmpSession session, SnmpSyntax pdu) {
		Category log = ThreadCategory.getInstance(getClass());
		if (log.isEnabledFor(Priority.WARN)) {
			log
					.warn("snmpTimeoutError: The session timed out communicating with the agent.");
		}

		m_error = true;

		if (m_signal != null) {
			synchronized (m_signal) {
				m_signal.signalAll();
			}
		}

		synchronized (this) {
			this.notifyAll();
		}
	}

	/**
	 * <P>Returns the success or failure code for collection
	 * of the data.</P>
	 */
	public boolean failed() {
		return m_error;
	}

	/**
	 * This method takes an SnmpOctetString and replaces any unprintable 
	 * characters with ASCII period ('.') and returns the resulting
	 * character string.  Special case in which the supplied SnmpOctetString
	 * consists of a single ASCII Null byte is also handled.  In this 
	 * special case an empty string is returned.
	 * 
	 * NOTE:  A character is considered unprintable if its decimal value
	 *        falls outside of the range: 32 - 126. 
	 *
	 * @param octetString	SnmpOctetString from which to generate the String
	 *
	 * @return a Java String object created from the octet string's byte array.
	 */
	public static String getPrintableString(SnmpOctetString octetString) {
		// Valid SnmpOctetString object
		if (octetString == null) {
			return null;
		}

		byte bytes[] = octetString.getString();

		// Sanity check
		if (bytes == null || bytes.length == 0) {
			return null;
		}

		// Check for special case where byte array contains a single
		// ASCII null character
		if (bytes.length == 1 && bytes[0] == 0) {
			return null;
		}

		// Replace all unprintable chars (chars outside of
		// decimal range 32 - 126 inclusive) with an 
		// ASCII period char (decimal 46).
		// 
		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] < 32 || bytes[i] > 126) {
				bytes[i] = 46; // ASCII period '.'
			}
		}

		// Create string, trim any white-space and return
		String result = new String(bytes);
		return result.trim();
	}

}