//
// Copyright (C) 2002-2003 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//
//
// Tab Size = 8
//

package org.opennms.netmgt.trapd;

import java.io.*;

import java.lang.reflect.UndeclaredThrowableException;

import java.net.InetAddress;
import java.net.SocketException;

import java.sql.SQLException;

import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueImpl;
import org.opennms.core.queue.FifoQueueException;

import org.opennms.core.fiber.PausableFiber;

import org.opennms.netmgt.xml.event.*;

import org.opennms.protocols.snmp.*;

import org.opennms.netmgt.config.TrapdConfigFactory;

/**
 * <p>The Trapd listens for SNMP traps on the standard port(162).
 * Creates a SnmpTrapSession and implements the SnmpTrapHandler to get
 * callbacks when traps are received</p>
 *
 * <p>The received traps are converted into XML and sent to eventd</p>
 *
 * <p><strong>Note:</strong>Trapd is a PausableFiber so as to receive
 * control events. However, a 'pause' on Trapd has no impact on the
 * receiving and processing of traps</p>
 *
 * @author	<A HREF="mailto:weave@opennms.org">Brian Weaver</A>
 * @author 	<A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj</A>
 * @author 	<A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author 	<A HREF="mailto:mike@opennms.org">Mike Davidson</A>
 * @author 	<A HREF="mailto:tarus@opennms.org">Tarus Balog</A>
 * @author	<A HREF="http://www.opennms.org">OpenNMS.org</A>
 *
 */
public class Trapd
	implements SnmpTrapHandler,
		   PausableFiber
{
	/**
	 * The name of the logging category for Trapd.
	 */
	private static final String	LOG4J_CATEGORY = "OpenNMS.Trapd";

	/**
	 * SQL to get already kown IPs
	 */
	private static final String	GET_KNOWN_IPS = "SELECT ipAddr, nodeId FROM ipInterface";
	
	/**
	 * The singlton instance.
	 */
	private static final Trapd	m_singleton = new Trapd();

	/**
	 * The trap session used by Trapd to receive traps
	 */
	private SnmpTrapSession		m_trapSession;

	/**
	 * The name of this service.
	 */
	private String			m_name;

	/**
	 * The last status sent to the service control manager.
	 */
	private int			m_status = START_PENDING;
	
	/**
	 * The communication queue
	 */
	private FifoQueue		m_backlogQ;

	/**
	 * The list of known IPs
	 */
	private Map			m_knownIps;
	
	/**
	 * The queue processing thread
	 */
	private TrapQueueProcessor	m_processor;

        /**
         * The class instance used to recieve new events from
         * for the system.
         */
        private BroadcastEventProcessor m_eventReader;

	/**
	 * V2 Trap information object for processing
	 * by the queue reader
	 */
	static class V2TrapInformation
	{
		/**
		 * The received PDU
		 */
		private SnmpPduPacket	m_pdu;
		
		/**
		 * The internet address of the sending agent
		 */
		private InetAddress	m_agent;
		
		/**
		 * The community string from the actual SNMP packet
		 */
		private SnmpOctetString	m_community;
		
		/**
		 * Constructs a new trap information instance that
		 * contains the sending agent, the community string,
		 * and the Protocol Data Unit.
		 *
		 * @param agent		The sending agent's address
		 * @param community	The community string from the SNMP packet.
		 * @param pdu		The encapsulated Protocol Data Unit.
		 *
		 */
		public V2TrapInformation(InetAddress     agent,
					 SnmpOctetString community,
					 SnmpPduPacket	 pdu)
		{
			m_pdu = pdu;
			m_agent = agent;
			m_community = community;
		}

		/**
		 * Returns the sending agent's internet address
		 */
		public InetAddress getAgent()
		{
			return m_agent;
		}
		
		/**
		 * Returns the Protocol Data Unit that was encapsulated
		 * within the SNMP Trap message
		 */
		public SnmpPduPacket getPdu()
		{
			return m_pdu;
		}
		
		/**
		 * Returns the SNMP community string from
		 * the received packet.
		 */
		public SnmpOctetString getCommunity()
		{
			return m_community;
		}
	}

	
	/**
	 * V1 trap element for processing by the queue
	 * reader
	 */
	static class V1TrapInformation
	{
		/**
		 * The received PDU
		 */
		private SnmpPduTrap	m_pdu;

		/**
		 * The internet address of the sending agent
		 */
		private InetAddress	m_agent;

		/**
		 * The community string from the actual SNMP packet
		 */
		private SnmpOctetString	m_community;
		
		/**
		 * Constructs a new trap information instance that
		 * contains the sending agent, the community string,
		 * and the Protocol Data Unit.
		 *
		 * @param agent		The sending agent's address
		 * @param community	The community string from the SNMP packet.
		 * @param pdu		The encapsulated Protocol Data Unit.
		 *
		 */
		public V1TrapInformation(InetAddress     agent,
					 SnmpOctetString community,
					 SnmpPduTrap	 pdu)
		{
			m_pdu = pdu;
			m_agent = agent;
			m_community = community;
		}
		
		/**
		 * Returns the sending agent's internet address
		 */
		public InetAddress getAgent()
		{
			return m_agent;
		}

		/**
		 * Returns the Protocol Data Unit that was encapsulated
		 * within the SNMP Trap message
		 */
		public SnmpPduTrap getPdu()
		{
			return m_pdu;
		}
		
		/**
		 * Returns the SNMP community string from
		 * the received packet.
		 */
		public SnmpOctetString getCommunity()
		{
			return m_community;
		}
	}
	
	/**
	 * <P>Constructs a new Trapd object that receives and forwards
	 * trap messages via JSDT. The session is initialized with the
	 * default client name of <EM>OpenNMS.trapd</EM>. The trap session
	 * is started on the default port, as defined by the SNMP libarary.</P>
	 *
	 * @see org.opennms.protocols.snmp.SnmpTrapSession
	 */
	public Trapd()
	{
		m_name    = "OpenNMS.Trapd";
	}


	/**
	 * <P>Process the recieved SNMP v2c trap that was received
	 * by the underlying trap session.</P>
	 *
	 * @param session	The trap session that received the datagram.
	 * @param agent		The remote agent that sent the datagram.
	 * @param port		The remmote port the trap was sent from.
	 * @param community	The community string contained in the message.
	 * @param pdu		The protocol data unit containing the data
	 *
	 */
	public void snmpReceivedTrap(SnmpTrapSession	session,
			             InetAddress	agent,
			             int		port,
			             SnmpOctetString	community,
			             SnmpPduPacket	pdu)
	{
		try
		{
			m_backlogQ.add(new V2TrapInformation(agent, community, pdu));
		}
		catch(InterruptedException ie)
		{
			Category log = ThreadCategory.getInstance(getClass());
			log.warn("snmpReceivedTrap: Error adding trap to queue, it was interrupted", ie);
		}
		catch(FifoQueueException qe)
		{
			Category log = ThreadCategory.getInstance(getClass());
			log.warn("snmpReceivedTrap: Error adding trap to queue", qe);
		}
	}


	/**
	 * <P>Process the recieved SNMP v1 trap that was received
	 * by the underlying trap session.</P>
	 *
	 * @param session	The trap session that received the datagram.
	 * @param agent		The remote agent that sent the datagram.
	 * @param port		The remmote port the trap was sent from.
	 * @param community	The community string contained in the message.
	 * @param pdu		The protocol data unit containing the data
	 *
	 */
	public void snmpReceivedTrap(SnmpTrapSession	session,
				     InetAddress	agent,
				     int		port,
				     SnmpOctetString	community,
				     SnmpPduTrap	pdu)
	{
		try
		{
			m_backlogQ.add(new V1TrapInformation(agent, community, pdu));
		}
		catch(InterruptedException ie)
		{
			Category log = ThreadCategory.getInstance(getClass());
			log.warn("snmpReceivedTrap: Error adding trap to queue, it was interrupted", ie);
		}
		catch(FifoQueueException qe)
		{
			Category log = ThreadCategory.getInstance(getClass());
			log.warn("snmpReceivedTrap: Error adding trap to queue", qe);
		}
	}


	/**
	 * <P>Processes an error condition that occurs in the SnmpTrapSession.
	 * The errors are logged and ignored by the trapd class.</P>
	 */
	public void snmpTrapSessionError(SnmpTrapSession session,
					 int		 error,
					 Object		 ref)
	{
		Category log = ThreadCategory.getInstance(getClass());

		log.warn("Error Processing Received Trap: error = " + error
			 + (ref != null ? ", ref = " + ref.toString() : ""));
	}

	public synchronized void init()
	{
		ThreadCategory.setPrefix(LOG4J_CATEGORY);

		Category log = ThreadCategory.getInstance();

		try
		{
			if(log.isDebugEnabled())
				log.debug("start: Initializing the trapd config factory");

			TrapdConfigFactory.reload();
			TrapdConfigFactory tFactory = TrapdConfigFactory.getInstance();

			if(log.isDebugEnabled())
				log.debug("start: Getting the already known IPs");

			// clear out the known nodes
			TrapdIPMgr.dataSourceSync();

			// Get the newSuspectOnTrap flag
			boolean m_newSuspect = tFactory.getNewSuspectOnTrap();

			// set up the trap processor
			m_backlogQ  = new FifoQueueImpl();
			m_processor = new TrapQueueProcessor(m_backlogQ, m_newSuspect);

			if(log.isDebugEnabled())
				log.debug("start: Creating the trap queue processor");

			// Initialize the trapd session
			int port = tFactory.getSnmpTrapPort();
			m_trapSession = new SnmpTrapSession(this, port);

			if(log.isDebugEnabled())
				log.debug("start: Creating the trap session");

		}
		catch( SocketException e ) 
		{
			log.error("Failed to setup SNMP trap port", e);
			throw new UndeclaredThrowableException(e);
		}
		catch( MarshalException e )
		{
			log.error("Failed to load configuration", e);
			throw new UndeclaredThrowableException(e);
		}
		catch( ValidationException e )
		{
			log.error("Failed to load configuration", e);
			throw new UndeclaredThrowableException(e);
		}
		catch( IOException e )
		{
			log.error("Failed to load configuration", e);
			throw new UndeclaredThrowableException(e);
		}
		catch( SQLException e ) 
		{
			log.error("Failed to load known IP address list", e);
			throw new UndeclaredThrowableException(e);
		}

                try
                {
                        m_eventReader = new BroadcastEventProcessor();
                }
                catch(Exception ex)
                {
                        ThreadCategory.getInstance().error("Failed to create event reader", ex);
                        throw new UndeclaredThrowableException(ex);
                }

	}
    
    
	/**
	 * Create the SNMP trap session and create the JSDT communication 
	 * channel to communicate with eventd.
	 *
	 * @exception java.lang.reflect.UndeclaredThrowableException if an
	 * unexpected database, or IO exception occurs. 
	 *
	 * @see org.opennms.protocols.snmp.SnmpTrapSession
	 * @see org.opennms.protocols.snmp.SnmpTrapHandler
	 */
	public synchronized void start()
	{
		m_status = STARTING;

		ThreadCategory.setPrefix(LOG4J_CATEGORY);

		Category log = ThreadCategory.getInstance();

		if(log.isDebugEnabled())
			log.debug("start: Initializing the trapd config factory");

		m_processor.start();

		m_status = RUNNING;

		if(log.isDebugEnabled())
			log.debug("start: Trapd ready to receive traps");

	}
    
	/**
	 * Pauses Trapd
	 */
	public void pause()
	{
		if(m_status != RUNNING)
			return;

		m_status = PAUSE_PENDING;

		Category log = ThreadCategory.getInstance(getClass());

		if(log.isDebugEnabled())
			log.debug("Calling pause on processor");

		m_processor.pause();

		if(log.isDebugEnabled())
			log.debug("Processor paused");

		m_status = PAUSED;

	     	if(log.isDebugEnabled())
			log.debug("Trapd paused");
	}

	/**
	 * Resumes Trapd
	 */
	public void resume()
	{
		if(m_status != PAUSED)
			return;

		m_status = RESUME_PENDING;

		Category log = ThreadCategory.getInstance(getClass());

		if(log.isDebugEnabled())
			log.debug("Calling resume on processor");

		m_processor.resume();

		if(log.isDebugEnabled())
			log.debug("Processor resumed");

		m_status = RUNNING;

		if(log.isDebugEnabled())
			log.debug("Trapd resumed");
	}
	
	
	/**
	 * Stops the currently running service. If the service is
	 * not running then the command is silently discarded.
	 */
	public synchronized void stop()
	{
		Category log = ThreadCategory.getInstance(getClass());

		m_status = STOP_PENDING;

		// shutdown and wait on the background processing thread to exit.
		if(log.isDebugEnabled())
			log.debug("exit: closing communication paths.");

		try
		{
			if(log.isDebugEnabled())
				log.debug("stop: Closing SNMP trap session.");

			m_trapSession.close();

			if(log.isDebugEnabled())
				log.debug("stop: SNMP trap session closed.");
		}
		catch(IllegalStateException isE)
		{
			if(log.isDebugEnabled())
				log.debug("stop: The SNMP session was already closed");
		}

		if(log.isDebugEnabled())
				log.debug("stop: Stopping queue processor.");

		// interrupt the processor daemon thread
		m_processor.stop();

		m_status = STOPPED;

		if(log.isDebugEnabled())
				log.debug("stop: Trapd stopped");
	}


	/**
	 * Returns the current status of the service.
	 *
	 * @return The service's status.
	 */
	public synchronized int getStatus()
	{
		return m_status;
	}


	/**
	 * Returns the name of the service.
	 *
	 * @return The service's name.
	 */
	public String getName()
	{
		return m_name;
	}


	/**
	 * Returns the singular instance of the trapd
	 * daemon. There can be only one instance of this
	 * service per virtual machine.
	 */
	public static Trapd getInstance()
	{
		return m_singleton;
	}

}
