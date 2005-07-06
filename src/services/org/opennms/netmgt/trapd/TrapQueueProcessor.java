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
// 2005 Jan 11: Added a check to insure V2 traps had TIMTICKS varbind.
// 2003 Aug 21: Modifications to support ScriptD.
// 2003 Feb 28: Small fix for null terminated strings in traps.
// 2003 Jan 31: Cleaned up some unused imports.
// 2003 Jan 08: Added code to associate IP addresses from traps with nodes.
// 2002 Nov 29: Fixed a small bug in trap handler. Bug #676.
// 2002 Jul 18: Added a check for bad varbind from Extreme traps.
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

package org.opennms.netmgt.trapd;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Category;
import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.eventd.EventConfigurationManager;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;
import org.opennms.netmgt.xml.eventconf.Logmsg;
import org.opennms.protocols.snmp.SnmpCounter32;
import org.opennms.protocols.snmp.SnmpCounter64;
import org.opennms.protocols.snmp.SnmpGauge32;
import org.opennms.protocols.snmp.SnmpIPAddress;
import org.opennms.protocols.snmp.SnmpInt32;
import org.opennms.protocols.snmp.SnmpNull;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpOctetString;
import org.opennms.protocols.snmp.SnmpOpaque;
import org.opennms.protocols.snmp.SnmpSyntax;
import org.opennms.protocols.snmp.SnmpTimeTicks;

/**
 * The TrapQueueProcessor handles the conversion of V1 and V2 traps to events
 * and sending them out the JSDT channel that eventd is listening on
 * 
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson </A>
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 *  
 */
class TrapQueueProcessor implements Runnable, PausableFiber {
	/**
	 * The sysUpTimeOID, which should be the first varbind in a V2 trap
	 */
	static final String SNMP_SYSUPTIME_OID = ".1.3.6.1.2.1.1.3.0";

	/**
	 * The sysUpTimeOID, which should be the first varbind in a V2 trap, but in
	 * the case of Extreme Networks only mostly
	 */
	static final String EXTREME_SNMP_SYSUPTIME_OID = ".1.3.6.1.2.1.1.3";

	/**
	 * The snmpTrapOID, which should be the second varbind in a V2 trap
	 */
	static final String SNMP_TRAP_OID = ".1.3.6.1.6.3.1.1.4.1.0";

	/**
	 * The snmp sysUpTime OID is the first varbind
	 */
	static final int SNMP_SYSUPTIME_OID_INDEX = 0;

	/**
	 * The snmp trap OID is the second varbind
	 */
	static final int SNMP_TRAP_OID_INDEX = 1;

	/**
	 * The input queue
	 */
	private FifoQueue m_backlogQ;

	/**
	 * The name of the local host.
	 */
	private String m_localAddr;

	/**
	 * Current status of the fiber
	 */
	private int m_status;

	/**
	 * The thread that is executing the <code>run</code> method on behalf of
	 * the fiber.
	 */
	private Thread m_worker;

	/**
	 * Whether or not a newSuspect event should be generated with a trap from an
	 * unknown IP address
	 */
	private boolean m_newSuspect;

	private EventIpcManager m_eventMgr;
	
	private SyntaxToEvent[] m_syntaxToEvents;
	
    
	
	/**
	 * Process a V2 trap and convert it to an event for transmission.
	 * 
	 * <p>
	 * From RFC2089 ('Mapping SNMPv2 onto SNMPv1'), section 3.3 ('Processing an
	 * outgoing SNMPv2 TRAP')
	 * </p>
	 * 
	 * <p>
	 * <strong>2b </strong>
	 * <p>
	 * If the snmpTrapOID.0 value is one of the standard traps the specific-trap
	 * field is set to zero and the generic trap field is set according to this
	 * mapping:
	 * <p>
	 * 
	 * <pre>
	 * 
	 *  
	 *   
	 *    
	 *     
	 *      
	 *            value of snmpTrapOID.0                generic-trap
	 *            ===============================       ============
	 *            1.3.6.1.6.3.1.1.5.1 (coldStart)                  0
	 *            1.3.6.1.6.3.1.1.5.2 (warmStart)                  1
	 *            1.3.6.1.6.3.1.1.5.3 (linkDown)                   2
	 *            1.3.6.1.6.3.1.1.5.4 (linkUp)                     3
	 *            1.3.6.1.6.3.1.1.5.5 (authenticationFailure)      4
	 *            1.3.6.1.6.3.1.1.5.6 (egpNeighborLoss)            5
	 *       
	 *      
	 *     
	 *    
	 *   
	 *  
	 * </pre>
	 * 
	 * <p>
	 * The enterprise field is set to the value of snmpTrapEnterprise.0 if this
	 * varBind is present, otherwise it is set to the value snmpTraps as defined
	 * in RFC1907 [4].
	 * </p>
	 * 
	 * <p>
	 * <strong>2c. </strong>
	 * </p>
	 * <p>
	 * If the snmpTrapOID.0 value is not one of the standard traps, then the
	 * generic-trap field is set to 6 and the specific-trap field is set to the
	 * last subid of the snmpTrapOID.0 value.
	 * </p>
	 * 
	 * <p>
	 * If the next to last subid of snmpTrapOID.0 is zero, then the enterprise
	 * field is set to snmpTrapOID.0 value and the last 2 subids are truncated
	 * from that value. If the next to last subid of snmpTrapOID.0 is not zero,
	 * then the enterprise field is set to snmpTrapOID.0 value and the last 1
	 * subid is truncated from that value.
	 * </p>
	 * 
	 * <p>
	 * In any event, the snmpTrapEnterprise.0 varBind (if present) is ignored in
	 * this case.
	 * </p>
	 * 
	 * @param info
	 *            V2 trap
	 */
	private void process(TrapInformation info) {
        
        try {
            Event event = info.getEventForTrap(this);
            processTrapEvent(event, info.getTrapInterface(), info.getNodeId(info.getTrapInterface()));
        } catch (IllegalArgumentException e) {
            log().info(e.getMessage());
        }
	}
    


    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public Parm processSyntax(String name, SnmpSyntax obj) {
		Category log = log();
		Value val = new Value();

		if (obj instanceof SnmpOctetString) {
			//
			// check for non-printable characters. If they
			// exist then print the string out as hexidecimal
			//
			boolean asHex = false;
			byte[] data = ((SnmpOctetString) obj).getString();
			for (int x = 0; x < data.length; x++) {
				byte b = data[x];
				if ((b < 32 && b != 9 && b != 10 && b != 13 && b != 0) || b == 127) {
					asHex = true;
					break;
				}
			}

			data = null;

			String encoding = asHex ? EventConstants.XML_ENCODING_BASE64
				: EventConstants.XML_ENCODING_TEXT;

			val.setType(EventConstants.TYPE_SNMP_OCTET_STRING);
			val.setEncoding(encoding);
			val.setContent(EventConstants.toString(encoding, obj));

			// DEBUG
			if (!asHex && log.isDebugEnabled()) {
				log.debug("snmpReceivedTrap: string varbind: "
					+ (((SnmpOctetString) obj).toString()));
			}
		} else {
			boolean found = false;
			for (int i = 0; i < m_syntaxToEvents.length; i++) {
				if (m_syntaxToEvents[i].getClassMatch() == null ||
						m_syntaxToEvents[i].m_classMatch.isInstance(obj)) {
					val.setType(m_syntaxToEvents[i].getType());
					val.setEncoding(m_syntaxToEvents[i].getEncoding());
					val.setContent(EventConstants.toString(
						m_syntaxToEvents[i].getType(), obj));
					found = true;
					break;
				}
			}
			if (!found) {
				throw new IllegalStateException("Internal error: fell through the " +
						"bottom of the loop.  The syntax-to-events array might not have a " +
						"catch-all for Object");
			}
		}

		Parm parm = new Parm();
		parm.setParmName(name);
		parm.setValue(val);

		return parm;
	}
    
    public void processTrapEvent(Event event, String trapInterface, long nodeId) {

        org.opennms.netmgt.xml.eventconf.Event econf = EventConfigurationManager.get(event);
		if (econf == null || econf.getUei() == null) {
			event.setUei("uei.opennms.org/default/trap");
		} else {
			event.setUei(econf.getUei());
		}
		
		if (econf != null) {
			Logmsg logmsg = econf.getLogmsg();
			if (logmsg != null) {
				String dest = logmsg.getDest();
				if ("discardtraps".equals(dest)) {
					log().debug("Trap discarded due to matching event having logmsg dest == discardtraps");
					return;
				}
			}
		}
		
		// send the event to eventd
		m_eventMgr.sendNow(event);

		log().debug("Trap successfully converted and sent to eventd");

		if (nodeId == -1 && m_newSuspect) {
			sendNewSuspectEvent(trapInterface);

			if (log().isDebugEnabled()) {
				log().debug("Sent newSuspectEvent for interface: "
						+ trapInterface);
			}
		}
	}

	/**
	 * Send a newSuspect event for the interface
	 * 
	 * @param trapInterface
	 *            The interface for which the newSuspect event is to be
	 *            generated
	 */
	private void sendNewSuspectEvent(String trapInterface) {
		// construct event with 'trapd' as source
		Event event = new Event();
		event.setSource("trapd");
		event
				.setUei(org.opennms.netmgt.EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI);
		event.setHost(m_localAddr);
		event.setInterface(trapInterface);
		event.setTime(org.opennms.netmgt.EventConstants
				.formatToString(new java.util.Date()));

		// send the event to eventd
		m_eventMgr.sendNow(event);
	}

	/**
	 * Returns true if the status is ok and the thread should continue running.
	 * If the status returend is false then the thread should exit.
	 *  
	 */
	private synchronized boolean statusOK() {
		Category log = log();

		//
		// Loop until there is a new client or we are shutdown
		//
		boolean exitThread = false;
		boolean exitCheck = false;
		while (!exitCheck) {
			//
			// check the child thread!
			//
			if (m_worker.isAlive() == false && m_status != STOP_PENDING) {
				log.warn(getName() + " terminated abnormally");
				m_status = STOP_PENDING;
			}

			//
			// do normal status checks now
			//
			if (m_status == STOP_PENDING) {
				exitCheck = true;
				exitThread = true;
				m_status = STOPPED;
			} else if (m_status == PAUSE_PENDING) {
				pause();
			} else if (m_status == RESUME_PENDING) {
				resume();
			} else if (m_status == PAUSED) {
				try {
					wait();
				} catch (InterruptedException e) {
					m_status = STOP_PENDING;
				}
			} else if (m_status == RUNNING) {
				exitCheck = true;
			}

		} // end !exit check

		return !exitThread;

	} // statusOK

	/**
	 * The constructor
	 */
	TrapQueueProcessor(FifoQueue backlog, boolean newSuspect,
			EventIpcManager eventMgr) {
		m_backlogQ = backlog;
		m_newSuspect = newSuspect;
		m_eventMgr = eventMgr;
		try {
			m_localAddr = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException uhE) {
			Category log = log();
			m_localAddr = "localhost";
			log.error("<ctor>: Error looking up local hostname", uhE);
		}
		
		m_syntaxToEvents = new SyntaxToEvent[] {
			new SyntaxToEvent(SnmpInt32.class, EventConstants.TYPE_SNMP_INT32,
				EventConstants.XML_ENCODING_TEXT),
			new SyntaxToEvent(SnmpNull.class, EventConstants.TYPE_SNMP_NULL,
				EventConstants.XML_ENCODING_TEXT),
			new SyntaxToEvent(SnmpObjectId.class, EventConstants.TYPE_SNMP_OBJECT_IDENTIFIER,
				EventConstants.XML_ENCODING_TEXT),
			new SyntaxToEvent(SnmpIPAddress.class, EventConstants.TYPE_SNMP_IPADDRESS,
				EventConstants.XML_ENCODING_TEXT),
			new SyntaxToEvent(SnmpTimeTicks.class, EventConstants.TYPE_SNMP_TIMETICKS,
				EventConstants.XML_ENCODING_TEXT),
			new SyntaxToEvent(SnmpCounter32.class, EventConstants.TYPE_SNMP_COUNTER32,
				EventConstants.XML_ENCODING_TEXT),
			new SyntaxToEvent(SnmpGauge32.class, EventConstants.TYPE_SNMP_GAUGE32,
				EventConstants.XML_ENCODING_TEXT),
			new SyntaxToEvent(SnmpOpaque.class, EventConstants.TYPE_SNMP_OPAQUE,
				EventConstants.XML_ENCODING_BASE64),
			new SyntaxToEvent(SnmpCounter64.class, EventConstants.TYPE_SNMP_COUNTER64,
				EventConstants.XML_ENCODING_TEXT),
			new SyntaxToEvent(Object.class, EventConstants.TYPE_STRING,
				EventConstants.XML_ENCODING_TEXT)
		};
	}

	/**
	 * Starts the current fiber. If the fiber has already been started,
	 * regardless of it's current state, then an IllegalStateException is
	 * thrown.
	 * 
	 * @throws java.lang.IllegalStateException
	 *             Thrown if the fiber has already been started.
	 *  
	 */
	public synchronized void start() {
		Category log = log();

		if (m_worker != null)
			throw new IllegalStateException(
					"The fiber is running or has already run");

		m_status = STARTING;

		m_worker = new Thread(this, getName());
		m_worker.start();

		if (log.isDebugEnabled()) {
			log.debug(getName() + " started");
		}
	}

	/**
	 * Pauses the current fiber.
	 */
	public synchronized void pause() {
		if (m_worker == null || m_worker.isAlive() == false)
			throw new IllegalStateException("The fiber is not running");

		m_status = PAUSED;
		notifyAll();
	}

	/**
	 * Resumes the currently paused fiber.
	 */
	public synchronized void resume() {
		if (m_worker == null || m_worker.isAlive() == false)
			throw new IllegalStateException("The fiber is not running");

		m_status = RUNNING;
		notifyAll();
	}

	/**
	 * <p>
	 * Stops this fiber. If the fiber has never been started then an
	 * <code>IllegalStateExceptio</code> is generated.
	 * </p>
	 * 
	 * @throws java.lang.IllegalStateException
	 *             Thrown if the fiber has never been started.
	 */
	public synchronized void stop() {
		if (m_worker == null)
			throw new IllegalStateException("The fiber has never run");

		m_status = STOP_PENDING;
		m_worker.interrupt();
		notifyAll();
	}

	/**
	 * Returns the name of the fiber.
	 * 
	 * @return The name of the Fiber.
	 */
	public String getName() {
		return "TrapQueueProcessor";
	}

	/**
	 * Returns the current status of the fiber
	 * 
	 * @return The status of the Fiber.
	 */
	public synchronized int getStatus() {
		if (m_worker != null && !m_worker.isAlive())
			m_status = STOPPED;

		return m_status;
	}

	/**
	 * Reads off of the input queue and depending on the type (V1 or V2 trap) of
	 * object read, process the traps to convert them to events and send them
	 * out
	 */
	public void run() {
		Category log = log();

		synchronized (this) {
			m_status = RUNNING;
		}

		while (statusOK()) {
			TrapInformation o = null;
			try {
				o = (TrapInformation)m_backlogQ.remove(1000);
			} catch (InterruptedException iE) {
				log.debug("Trapd.QueueProcessor: caught interrupted exception");

				o = null;

				m_status = STOP_PENDING;
			} catch (FifoQueueException qE) {
				log.debug("Trapd.QueueProcessor: caught fifo queue exception");
				log.debug(qE.getLocalizedMessage(), qE);

				o = null;

				m_status = STOP_PENDING;
			}

			if (o != null && statusOK()) {
                try {
                    process(o);
                } catch (Throwable t) {
                    log.error("Unexpected error processing trap", t);
                }
			}
		}
	}
	
	public class SyntaxToEvent {
		private Class m_classMatch;
		private String m_type;
		private String m_encoding;
		
		public SyntaxToEvent(Class classMatch, String type, String encoding) {
			m_classMatch = classMatch;
			m_type = type;
			m_encoding = encoding;
		}
		
		public Class getClassMatch() {
			return m_classMatch;
		}
		
		public String getType() {
			return m_type;
		}
		
		public String getEncoding() {
			return m_encoding;
		}
	}
}