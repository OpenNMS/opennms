// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//	Brian Weaver	<weave@opennms.org>
//	http://www.opennms.org/
//
//
// Tab Size = 8
//
//
package org.opennms.netmgt.collectd;

import java.util.*;

import org.apache.log4j.Priority;
import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.protocols.snmp.*;
import org.opennms.netmgt.utils.Signaler;
import org.opennms.netmgt.config.DataCollectionConfigFactory;

/**
 * <P>The SnmpNodeCollector class is responsible for performing the actual
 * SNMP data collection for a node over a specified network interface.
 * The SnmpNodeCollector implements the SnmpHandler class in order to 
 * receive notifications when an SNMP reply is received or error 
 * occurs.</P>
 * 
 * <P>The SnmpNodeCollector is provided a list of MIB objects to collect
 * and an interface over which to collect the data. Data collection
 * can be via SNMPv1 GetNext requests or SNMPv2 GetBulk requests
 * depending upon the parms used to construct the collector.</P>
 *
 * @author <A HREF="mailto:mike@opennms.org">Mike</A>
 * @author <A>Jon Whetzel</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 * @version 1.1.1.1
 *
 */
public class SnmpNodeCollector implements SnmpHandler
{
	/**
	 * <P>Flag indicating the success or failure of the
	 * informational query. If the flag is set to false
	 * then either part of all of the information was
	 * unable to be retreived. If it is set to true then
	 * all of the data was received from the remote host.</P>
	 */
	private boolean		m_error;
	
	/**
	 * <P>Reason that the SNMP request failed.  Please
	 * see org.opennms.protocols.snmp.SnmpPduPacket class
	 * for a list of possible SNMP error codes.  This
	 * variable only has meaning of m_error flag is true.</P>
	 */
	private int		m_errorStatus;

	/**
	 * <P>Array of SNMPv1 error strings.  
	 * Please see org.opennms.protocols.snmp.SnmpPduPacket
	 * class for list of SNMP error codes which serve as 
	 * indices into this string array.</P>
	 */
	private static String[] m_errorText = {	"ErrNoError",
						"ErrTooBig",
						"ErrNoSuchName",
						"ErrBadValue",
						"ErrReadOnly",
						"ErrGenError" };
		
	/**
	 * <P>If the SNMP collection failed due to a problem
	 * with one or more varbinds (for example if a 
	 * particular object oid is requested which is not implemented
	 * in the target's SNMP agent) then this value will be
	 * set equal to the *first* failing varbind in the request.
	 * This variable only has meaning if m_error flag is true.
	 * Will be set to -1 if the SNMP collection failed for 
	 * an unrelated reason.</P>
	 */
	private int		m_errorIndex;
	
	/**
	 * <P>Flag indicating if the SNMP collection failed due
	 * to the SNMP request timing out.  Its value only has
	 * meaning if m_error flag is true.</P>
	 */
	private boolean		m_timeout;
	
	/**
	 * <P>Used to synchronize the class to ensure that the
	 * session has finished collecting data before the
	 * value of success or failure is set, and control
	 * is returned to the caller.</P>
	 */
	private Signaler 	m_signal;

	/**
	 * List of MibObject objects to be collected.
	 */
	private List 		m_objList;
	
	/**
	 * Initialized to zero. As each response
	 * PDU is received this value is incremented by the number  
	 * of vars contained in the response.  Processing will
	 * continue until this value reaches the total number
	 * of oids in the MibObject list (m_olbjList)
	 * and all oid values have been retrieved.
	 */
	private int 		m_oidListIndex;
	
	/**
	 * <P> Used to store the collected MIB data.</P>
	 */
	private SNMPCollectorEntry  m_collectorEntry;

	/**
	 * <P> Used for classifying the SNMP version of the session</P>
	 *
	 */
	private int             m_version;

	/**
	*<P>Holds the IP Address of the primary SNMP iterface.</P>
	*/
	private String          m_primaryIf;

	/**
	 * Max number of variables permitted in a single outgoing
	 * SNMP PDU request..
	 */
	private int m_maxVarsPerPdu;
	
	/**
	 * <P>The default constructor is marked private and will
	 * always throw an exception. This is done to disallow
	 * the default constructor. The reason is that this
	 * object requires several arguments to perform it's duties.
	 *
	 * @exception java.lang.UnsupportedOperationException Always thrown from
	 *	this method since it is not supported.
	 *
	 * @see #SnmpNodeCollector(SnmpSession, String, int, Signaler, List)
	 */
	private SnmpNodeCollector( )
		throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("Default Constructor not supported");
	}
	
	/**
	 * <P>The class constructor is used to initialize the collector
	 * and send out the initial SNMP packet requesting data. The
	 * data is then received and store by the object. When all the
	 * data has been collected the passed signaler object is <EM>
	 * notified</EM> using the notifyAll() method.</P>
	 *
	 * @param session	The SNMP session with the remote agent.
	 * @param signaler	The object signaled when data collection is done.
	 * @param objList	The list of object id's to be collected.
	 * @param maxVarsPerPdu Max number of vars permitted in a single PDU
	 */
	public SnmpNodeCollector(SnmpSession 	session,
				Signaler 	signaler, 
				List 		objList,
				int		maxVarsPerPdu)
	{
		super();
		
		// Log4j category
		//
		Category log = ThreadCategory.getInstance(getClass());
		
		m_error = false;
		m_errorIndex = -1;
		m_timeout = false;
		
		m_collectorEntry = null;
		
		// Process parameters
		//
		m_primaryIf = session.getPeer().getPeer().getHostAddress();
		m_version = session.getPeer().getParameters().getVersion();
		m_signal = signaler;		
		m_objList = objList;
		m_oidListIndex = 0;
		m_maxVarsPerPdu = maxVarsPerPdu;
		
		if (log.isDebugEnabled())
			log.debug("SnmpNodeCollector: totalOids=" + objList.size() + " maxVarsPerPdu=" + maxVarsPerPdu);
			
		// Create initial PDU request and send it to the remote host.
		//
		SnmpPduPacket pdu = getNextPdu(m_primaryIf);
		if (log.isDebugEnabled())
			log.debug("SnmpNodeCollector: sending initial SNMP get/getBulk request PDU for " + m_primaryIf);

		session.send(pdu, this);
	}
	
	/**
	*<P>This method will take an OID, and generate the succeeding OID.  
	* This will be used for examining responses from SNMPv2 GETBULK packets
	* when doing SNMPv2 collection, so that we can keep all the 
	* data for a particular object, and throw out the rest.</P>
	*
	* @param oid	Object identifier from which to generate the stop oid
	* 
	* @return SnmpObjectId object which represents the oid on which to 
	*         stop the bulk collection.
	*/
	public static SnmpObjectId stop_oid(String oid) 
	{
		// Log4j category
		//
		Category log = ThreadCategory.getInstance(SnmpNodeCollector.class);
		
		SnmpObjectId id = new SnmpObjectId(oid);
		int[] ids = id.getIdentifiers();
		++ids[ids.length-1];
		id.setIdentifiers(ids);
		if (log.isDebugEnabled())
			log.debug("stop_oid: stop_oid = " + id.toString());
		return id;
	}
	
	/**
	 * <P>This method constructs the next SnmpPduPacket (pdu) for sending
	 * to the remote node.  The version of SNMP supported
	 * will determine whether a GET or GETBULK PDU is built.</P>
	 * 
	 * <P>For SNMPv1, GET commands are built. Each constructed PDU contains 
	 * varbinds for all the objects to be collected.</P>
	 *
	 * <P>For SNMPv2, GETBULK commands are built. Each constructed PDU contains
	 * varbinds for all the objects to be collected.</P>
	 *
	 * @param ifAddress 	Interface address of the remote agent
	 * 
	 * @return An SnmpPduPacket appropriate for the SNMP version supported.
	 *
	 * @see org.opennms.protocols.snmp.SnmpNull		SnmpNull
	 * @see org.opennms.protocols.snmp.SnmpPduPacket 	SnmpPduPacket
	 */
	public SnmpPduPacket getNextPdu(String ifAddress)
	{
		// Log4j category
		//
		Category log = ThreadCategory.getInstance(getClass());
		
		SnmpPduPacket pdu = null;
		int nonRepeaters = 0;  // Applicable to SNMPv2 only

		// SNMPv2 Support
		if (m_version == SnmpSMI.SNMPV2) 
		{
			pdu = new SnmpPduBulk();
		}
		// SNMPv1 Support
		else 
		{
			pdu = new SnmpPduRequest(SnmpPduPacket.GET);
		}
		
		pdu.setRequestId(pdu.nextSequence());
		
		// Generate new SnmpVarBind object.  Add each oid from the
		// object list until max var count is reached or there are no
		// more objects to collect.
		Object[] oidArray = m_objList.toArray();
		for (int ii=m_oidListIndex; 
			ii < m_objList.size() && (ii-m_oidListIndex) < m_maxVarsPerPdu;
			ii++)
		{
			MibObject mibObject = ((MibObject)oidArray[ii]);
			
			SnmpObjectId oid = null;
			String instanceStr = mibObject.getInstance();
			
			//
			// instance must be one of the following:
			//      1) null.  In this case the object's oid value is used as-is.
			//	2) a sequence of period-separated decimal values indicating the
			// 	instance to be retrieved.  This value will be appended to the 
			// 	objects oid.  In the case of SNMPv2 the final decimal value will
			//	be decremented by one due to the fact that SNMP GetNext is used.
			//	3) an integer value indicating the instance of the MIB object to be
			// 	retrieved.  This value will be appended to the object's oid.
			//	In the case of SNMPv2 the final decimal value will
			//	be decremented by one due to the fact that SNMP GetNext is used.
			//
			if (instanceStr == null)
			{
				log.warn("No instance specified for MIB object " + mibObject.getOid());
				oid = new SnmpObjectId(mibObject.getOid());
			}
			else if (m_version == SnmpSMI.SNMPV1)
			{
				oid = new SnmpObjectId(mibObject.getOid() + "." + instanceStr);
			}
			else if (m_version == SnmpSMI.SNMPV2)
			{
				String instancePrefix = null;
				String lastDecimalValue = null;
				
				// Sequence of period-separated decimal values?
				//
				if (instanceStr.indexOf(".") != -1)
				{
					// Extract last decimal value so we can decrement it by one
					int lastPeriod = instanceStr.lastIndexOf('.');
					instancePrefix = instanceStr.substring(0, lastPeriod);
					lastDecimalValue = instanceStr.substring(lastPeriod+1);
				}
				// Single integer value?
				else 
				{
					lastDecimalValue = instanceStr;
				}
				
				// convert the lastDecimalValue string to an integer
				int lastValue = -1;
				try 
				{
					lastValue = Integer.parseInt(lastDecimalValue);
				}
				catch (NumberFormatException nfe)
				{
					// If the value cannot be converted to an integer just
					// use a string of "0".
					log.warn("Failed to convert last value " + lastDecimalValue + " from instance " + instanceStr + " to an integer...will use a value of 0");
					lastValue = 0;  
				}
					
				// For SNMPV2 we are using GetBulk requests which in effect
				// performs a GetNext as opposed to a Get.  Therefore we need
				// to decrement the final decimal value of the instance string
				// by one so that the GetNext will retrieve the appropriate
				// entry.  If instance is '0' we leave off the instance id entirely
				//
				if (lastValue == 0)
					oid = new SnmpObjectId(mibObject.getOid());
				else
				{
					Integer instance = new Integer(lastValue-1);
					
					if (instancePrefix == null)
						oid = new SnmpObjectId(mibObject.getOid() + "." + instance.toString());
					else
						oid = new SnmpObjectId(mibObject.getOid() + "." + instancePrefix + "." + instance.toString());
				}
				
				// increment non-repeaters count
				nonRepeaters ++;
			} 
			
			// Add the variable binding to the pdu
			if (log.isDebugEnabled()) 
				log.debug("getNextPdu: adding object id to pdu: " + oid.toString());

			pdu.addVarBind(new SnmpVarBind(oid));

		}	

		// if SNMPv2, assign the non repeater and max repetitions count
		if (m_version == SnmpSMI.SNMPV2)
		{
			((SnmpPduBulk)pdu).setNonRepeaters(nonRepeaters);
			((SnmpPduBulk)pdu).setMaxRepititions(0);
		}
		
		if (log.isDebugEnabled())
			log.debug("getNextPdu: finished building next pdu, num vars in pdu=" + pdu.getLength());
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
	public void snmpReceivedPdu(SnmpSession session, int command, SnmpPduPacket pdu)
	{ 
		boolean doNotify = true;
		boolean storeResponseData = true;
		
		// Log4j category
		//
		Category log = ThreadCategory.getInstance(getClass());
		
		if (log.isDebugEnabled())
			log.debug("snmpReceivedPdu: got an SNMP pdu, num vars=" + pdu.getLength());
		
		try
		{
			if(command == SnmpPduPacket.RESPONSE)
			{
				//
				// Check for SNMP error stored in request pdu
				//
				m_errorStatus = ((SnmpPduRequest)pdu).getErrorStatus();
				if (m_errorStatus != SnmpPduPacket.ErrNoError)
				{
					m_error = true;
 
					m_errorIndex = ((SnmpPduRequest)pdu).getErrorIndex();
					if(log.isEnabledFor(Priority.WARN))
						log.warn("snmpReceivedPDU: Error during node SNMP collection for interface " + session.getPeer().getPeer().toString() + ", SNMP error text: " + m_errorText[m_errorStatus]);
 
					// If valid m_errorIndex variable is set:
					//   - print warning indicating the failing object
					//     if SNMP version is SNMPv1:
					//       - remove the failing object from the MIB object list
					//       - rebuild the SNMP request PDU (absent the failing object)
					//       - resend the request
					//
					if (m_errorIndex > 0 && (m_errorIndex + m_oidListIndex) <= m_objList.size())
					{
						MibObject tempObj = (MibObject)m_objList.get((m_errorIndex-1)+m_oidListIndex);
						if(log.isEnabledFor(Priority.WARN))
							log.warn("snmpReceivedPDU: Failing varbind - name: " + tempObj.getAlias() + " oid: " + tempObj.getOid());
 
						if (m_version == SnmpSMI.SNMPV1)
						{
							m_error = false; // attempt to recover
							storeResponseData = false;
							
							// Remove the failing MIB object from the object list,
							//
							m_objList.remove((m_errorIndex-1) + m_oidListIndex);
							
							if (log.isDebugEnabled())
								log.debug("snmpReceivedPDU: Removing failing varbind and resending request...");
 
							// Rebuild the request PDU and resend
							//
							SnmpPduPacket nxtpdu = getNextPdu(m_primaryIf);
							doNotify = false;
							session.send(nxtpdu, this);
						}
					}
				}

				//
				// SNMPv2 Responses
				//
				else if (m_version == SnmpSMI.SNMPV2) 
				{
					if (log.isDebugEnabled())
						log.debug("snmpReceivedPdu: node SNMP response arrived.  Handling GETBULK response.");
					
					int numVarBinds = pdu.getLength();
					for (int y = 0; y < numVarBinds; y++) 
					{
						SnmpVarBind vb = pdu.getVarBindAt(y);
						
						if (log.isDebugEnabled())
							log.debug("snmpReceivedPDU: current oid name,value pair: " + vb.getName() + " : " + vb.getValue());
						
						// Check varbind for SNMPv2 error
						if (vb.getValue() instanceof org.opennms.protocols.snmp.SnmpV2Error)
						{
							if (log.isDebugEnabled())
								log.debug("SnmpNodeCollector.snmpReceivedPdu: SNMPv2 error for oid: " + vb.getName() + " error: " + vb.getValue());
						}
					}
				}
				else  
				{
					//
					// SNMPv1 Response
					//
					// Each SNMPv1 response contains all of the data collected for
					// one of the remote node's interfaces.  Create a new SnmpNodeCollectorEntry
					// from the response PDU's variable binding list.
					//
					if (log.isDebugEnabled())
						log.debug("snmpReceivedPDU: node SNMP response arrived. Handling GET response.");
				}
				
				// Regardless of SNMPv1 or SNMPv2 response store all responses in an
				// SNMPCollectorEntry object
				//
				if (!m_error && storeResponseData)
				{
					if (m_collectorEntry == null)
					{
						m_collectorEntry = new SNMPCollectorEntry(pdu.toVarBindArray(), m_objList, null);
					}
					else
					{
						m_collectorEntry.update(pdu.toVarBindArray(), null);
					}
					
					// Have all objects been collected?
					m_oidListIndex = m_oidListIndex + pdu.getLength();
					if (m_oidListIndex < m_objList.size())
					{
						// Buld next request PDU and send it to the agent
						//
						if (log.isDebugEnabled())
							log.debug("snmpReceivedPDU: more to collect...sending next request, oidListIndex=" + m_oidListIndex + " totalObjects=" + m_objList.size());
						SnmpPduPacket nxtpdu = getNextPdu(m_primaryIf);
						doNotify = false;
						session.send(nxtpdu, this);
						doNotify = false;
					}
					else
					{
						if (log.isDebugEnabled())
							log.debug("snmpReceivedPDU: collection completed!!");
					}
				}
			}
			else // It was an invalid PDU
			{
				if (log.isDebugEnabled())
					log.debug("snmpReceivedPdu: Invalid PDU!");

				m_error = true;
			}
	    
			//	
			// Signal anyone waiting
			//
			if (doNotify) 	
			{
				if(m_signal != null)
				{
					synchronized(m_signal)
					{
						m_signal.signalAll();
					}
				}
		
				//
				// notify anyone waiting on this
				// particular object
				//
				synchronized(this)
				{
					this.notifyAll();
				}
			}
		} 
		catch (Throwable t)
		{
			if(log.isEnabledFor(Priority.WARN))
				log.warn("snmpReceivedPdu: unexpected exception...", t);
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
	public void snmpInternalError(SnmpSession session, int error, SnmpSyntax pdu)
	{
		// Log4j category
		//
		Category log = ThreadCategory.getInstance(getClass());
		
		if (log.isDebugEnabled())
			log.debug("snmpInternalError: node SNMP collection failed for interface " + session.getPeer().getPeer().toString() + ", SnmpSession errCode: " + error);

		m_error = true;

		if(m_signal != null)
		{
			if (log.isDebugEnabled())
				log.debug("snmpInternalError: synchronizing on signal...");

			synchronized(m_signal)
			{
				if (log.isDebugEnabled())
					log.debug("snmpInternalError: calling signalAll....");

				m_signal.signalAll();

				if (log.isDebugEnabled())
					log.debug("snmpInternalError: back from calling signalAll....");
			}
		}
		
		synchronized(this)
		{
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
	public void snmpTimeoutError(SnmpSession session, SnmpSyntax pdu)
	{
		// Log4j category
		//
		Category log = ThreadCategory.getInstance(getClass());
		
		if (log.isDebugEnabled())
			log.debug("snmpTimeoutError: node SNMP collection failed for interface " + session.getPeer().getPeer().toString());

		m_error = true;
		m_timeout = true;
		
		if(m_signal != null)
		{
			synchronized(m_signal)
			{
				m_signal.signalAll();
			}
		}
		
		synchronized(this)
		{
			this.notifyAll();
		}
	}		

	/**
	 * <P>Returns the success or failure code for collection
	 * of the data.</P>
	 */
	public boolean failed()
	{
		return m_error;
	}
	
	/**
	 * <P>Returns true if SNMP collection failed due to timeout.
	 * Otherwise, returns false.</P>
	 */
	public boolean timedout()
	{
		if (m_error)
			return m_timeout;
		else
			return false;
	}

       /**
	* <P>Returns the list of all entry maps that can be used
	* to access all the information from the service polling.
	*/
	
	public SNMPCollectorEntry getEntry() {
	    
	    return m_collectorEntry;
	}
}

