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
//      http://www.blast.com/
//
// Tab Size = 8
//
// SnmpIfCollector.java,v 1.1.1.1 2001/11/11 17:34:38 ben Exp
//

package org.opennms.netmgt.collectd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
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
 * The SnmpIfCollector class is responsible for performing the actual
 * SNMP data collection for a node over a specified network interface.
 * The SnmpIfCollector implements the SnmpHandler class in order to 
 * receive notifications when an SNMP reply is received or error 
 * occurs.
 * 
 * The SnmpIfCollector is provided a list of MIB objects to collect
 * and an interface over which to collect the data. Data collection
 * can be via SNMPv1 GetNext requests or SNMPv2 GetBulk requests
 * depending upon the parms used to construct the collector.
 *
 * @author <A HREF="mailto:mike@opennms.org">Mike</A>
 * @author <A>Jon Whetzel</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 * @version 1.1.1.1
 *
 */
public class SnmpIfCollector implements SnmpHandler
{
	/**
	 * Flag indicating the success or failure of the
	 * informational query. If the flag is set to false
	 * then either part of all of the information was
	 * unable to be retreived. If it is set to true then
	 * all of the data was received from the remote host.
	 */
	private boolean		m_error;
	
	/**
	 * Reason that the SNMP request failed.  Please
	 * see org.opennms.protocols.snmp.SnmpPduPacket class
	 * for a list of possible SNMP error codes.  This
	 * variable only has meaning of m_error flag is true.
	 */
	private int		m_errorStatus;

	/**
	 * Array of SNMPv1 error strings.  
	 * Please see org.opennms.protocols.snmp.SnmpPduPacket
	 * class for list of SNMP error codes which serve as 
	 * indices into this string array.
	 */
	private static String[] m_errorText = {	"ErrNoError",
						"ErrTooBig",
						"ErrNoSuchName",
						"ErrBadValue",
						"ErrReadOnly",
						"ErrGenError" };
		
	/**
	 * If the SNMP collection failed due to a problem
	 * with one or more varbinds (for example if a 
	 * particular object oid is requested which is not implemented
	 * in the target's SNMP agent) then this value will be
	 * set equal to the *first* failing varbind in the request.
	 * This variable only has meaning if m_error flag is true.
	 * Will be set to -1 if the SNMP collection failed for 
	 * an unrelated reason.
	 */
	private int		m_errorIndex;
	
	/**
	 * Flag indicating if the SNMP collection failed due
	 * to the SNMP request timing out.  Its value only has
	 * meaning if m_error flag is true.
	 */
	private boolean		m_timeout;
	
	/**
	 * Used to synchronize the class to ensure that the
	 * session has finished collecting data before the
	 * value of success or failure is set, and control
	 * is returned to the caller.
	 */
	private Signaler 	m_signal;

	/**
	 * List of MibObject objects to be collected.
	 */
	private java.util.List 	m_objList;
	
	/**
	 * Copy of m_objList list that will used solely by version 2.
	 * As each object is collected from the remote node via
	 * subsequent SNMPv2 GetBulk commands the MibObject is removed
	 * and the collection for the next object started.
	 */
	private java.util.List            m_objList_v2;
       
	 /**
	 * Used to store all the generated maps of the MIB
	 * data for each interface.
	 */
	private java.util.List             m_entries;

	/**
	 * Used for classifying the SNMP version of the session.
	 *
	 */
	private int              m_version;

	/**
	 * Used for keeping track of all the possible indexes.  Needed
	 * for generating packets for SNMP v1.
	 *
	 */
	 private String[]        m_indexArray;

	/**
	 * Used as temporary storage for variables coming from
	 * Snmp v2 PDU reponses.  Once all the data is gathered, we
	 * can generate maps of the data, one map per interface.
	 */
	 private List            m_responseVbList;
 
	/**
	 * For SNMPv1, used for keeping track of the SNMP response PDUs received.
	 * 
	 * For SNMPv2, this used for keeping track of the total number of variable bindings
	 * returned during each subsequent MIB object collection.  This variable 
	 * is reset back to 0 after the MIB object has been collected for all interfaces.
	 */
	 private int             m_responses;
	 
	/**
	 * For SNMPv1, used to store the collected MIB data.
	 */
	private SNMPCollectorEntry  m_collectorEntry;
	
	/** 
	 * For SNMPv1, keeps track of current oid list index.
	 */
	 private int 		m_oidListIndex;
	 
	/**
	 * Holds the ifIndex of the primary SNMP interface.
	 */
	 private String          m_primaryIfIndex;

       /**
	* Holds the IP Address of the primary SNMP iterface.
	*/
	private String          m_primaryIf;

	/**
	 * The number of interfaces associated with the remote host.
	 */
	private int		m_numInterfaces;
	
	/**
	 * Max number of variables permitted in a single outgoing
	 * SNMP PDU request..
	 */
	private int m_maxVarsPerPdu;
	
	/**
	 * Map of IfInfo objects representing each of the nodes
	 * interfaces.
	 */
	private Map 		m_ifMap;
	
	/**
	 * The default constructor is marked private and will
	 * always throw an exception. This is done to disallow
	 * the default constructor. The reason is that this
	 * object requires several arguments to perform it's duties.
	 *
	 * @exception java.lang.UnsupportedOperationException Always thrown from
	 *	this method since it is not supported.
	 */
	private SnmpIfCollector( )
		throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("Default Constructor not supported");
	}
	
	/**
	 * The class constructor is used to initialize the collector
	 * and send out the initial SNMP packet requesting data. The
	 * data is then received and store by the object. When all the
	 * data has been collected the passed signaler object is <EM>
	 * notified</EM> using the notifyAll() method.
	 *
	 * @param session	The SNMP session with the remote agent.
	 * @param signaler	The object signaled when data collection is done.
	 * @param primaryIfIndex       The ifIndex value linked to the primary IP.
	 * @param ifMap		Map of org.opennms.netmgt.poller.collectd.IfInfo objects.
	 * @param ifCount	Number of interfaces found in node's MIB-II ifTable
	 * @param maxVarsPerPdu Max number of variables permitted in a single PDU.
	 */
	public SnmpIfCollector(SnmpSession 	session,
				Signaler 	signaler, 
				String 		primaryIfIndex, 
				Map 		ifMap,
				int		ifCount,
				int		maxVarsPerPdu)
	{
		super();
		
		// Log4j category
		//
		Category log = ThreadCategory.getInstance(getClass());
		
		m_error = false;
		m_errorIndex = -1;
		m_timeout = false;
		
		// Process parameters
		//
		m_primaryIf = session.getPeer().getPeer().getHostAddress();
		m_primaryIfIndex = primaryIfIndex;
		m_version = session.getPeer().getParameters().getVersion();
		m_signal = signaler;		
		m_oidListIndex = 0;
		m_collectorEntry = null;
		m_numInterfaces = ifCount;
		m_ifMap = ifMap;
		m_maxVarsPerPdu = maxVarsPerPdu;
		if (log.isDebugEnabled())
			log.debug("SnmpIfCollector: maxVarsPerPdu=" + maxVarsPerPdu);
		
		// Build (String) array of interface indices using ifMap parm
		//
		Collection interfaces = ifMap.values();
		Iterator iter = interfaces.iterator();
		m_indexArray = new String[ifMap.size()];
		if (log.isDebugEnabled())
			log.debug("SnmpIfCollector: ifMap size: " + ifMap.size());

		int i = 0;
		while(iter.hasNext())
		{
			IfInfo ifInfo = (IfInfo)iter.next();
			m_indexArray[i++] = String.valueOf(ifInfo.getIndex());
			if (log.isDebugEnabled())
				log.debug("SnmpIfCollector: arrayIndex: " + i + " ifIndex: " + String.valueOf(ifInfo.getIndex()));
		}	
		
		// Build object lists for SNMPv2 processing
		//
		m_objList = null;
		m_objList_v2 = null;
		if (m_version == SnmpSMI.SNMPV2)
		{
			// Generate object list consisting of all unique oids
			// to be collected from all interfaces
			m_objList = buildV2CombinedOidList(ifMap);
			
			// Create copy of combined oid list which can
			// be modified during collection without affecting
			// the master list
			m_objList_v2 = new ArrayList(m_objList.size());
			for (int c = 0; c < m_objList.size(); c++) 
			{
				m_objList_v2.add(m_objList.get(c));
			}
		}
			
		// Allocate temporary storage to hold response varbinds.
		//
		m_responseVbList = new ArrayList();
		m_responses = 0;

		// Instantiate ArrayList to hold generated SnmpCollectoryEntry objects
		// created during the collection.
		//
		m_entries = new ArrayList(2);
		
		// Create initial PDU request and send it to the remote host.
		//
		SnmpPduPacket pdu = null;
		if (m_version == SnmpSMI.SNMPV2)
		{
			pdu = getNextSnmpV2Pdu(m_primaryIf);
		}
		else if (m_version == SnmpSMI.SNMPV1)
		{
			pdu = getNextSnmpV1Pdu(m_primaryIf);
		}
		if (log.isDebugEnabled())
			log.debug("SnmpIfCollector: sending initial interface SNMP get(Next/Bulk) request PDU for " + m_primaryIf + " with ifIndex: " + m_primaryIfIndex);

		session.send(pdu, this);
	}
	
	/**
	* This method will take an OID, and generate the succeeding OID.  
	* This will be used for examining responses from SNMPv2 GETBULK packets
	* when doing SNMPv2 collection, so that we can keep all the 
	* data for a particular object, and throw out the rest.
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
		Category log = ThreadCategory.getInstance(SnmpIfCollector.class);
		
		SnmpObjectId id = new SnmpObjectId(oid);
		int[] ids = id.getIdentifiers();
		++ids[ids.length-1];
		id.setIdentifiers(ids);
		if (log.isDebugEnabled())
			log.debug("stop_oid: stop_oid = " + id.toString());
		return id;
	}
	
	/**
	 * This method constructs the next SnmpPduPacket (pdu) for sending
	 * to the remote node.
	 * 
	 * For SNMPv1, GET commands are built. Each constructed PDU contains 
	 * varbinds for all the objects to be collected for a single interface.  
	 * For objects whose instance id 
	 * is determined by ifIndex (such as ifOctetsIn or ifOctetsOut in the ifTable) 
	 * the interface index will be appended to the oid name within the varbind.  
	 * Therefore, for an SNMPv1 based collection the number of PDUs generated
	 * will be equal to the number interfaces in the remote node's ifTable.  As 
	 * each GET response is received the response count is incremented and 
	 * each subsequent call to getNextSnmpV1Pdu() will generate a PDU for collecting
	 * data pertaining to the next interface in the ifTable.
	 *
	 * @param ifAddress 	Interface address of the remote agent
	 * 
	 * @return An SnmpPduPacket appropriate for the SNMP version supported.
	 *
	 * @see org.opennms.protocols.snmp.SnmpNull		SnmpNull
	 * @see org.opennms.protocols.snmp.SnmpPduPacket 	SnmpPduPacket
	 */
	private SnmpPduPacket getNextSnmpV1Pdu(String ifAddress)
	{
		// Log4j category
		//
		Category log = ThreadCategory.getInstance(getClass());
		
		Integer index = null;
		SnmpPduPacket pdu = null;

		pdu = new SnmpPduRequest(SnmpPduPacket.GET);
		index = new Integer(m_indexArray[m_responses]);
		m_objList = ((IfInfo)m_ifMap.get(index)).getOidList();		
		if (log.isDebugEnabled())
			log.debug("getNextSnmpV1Pdu: ifindex: " + index + " oidListIndex=" + m_oidListIndex + " objCount=" + m_objList.size());
		
		pdu.setRequestId(SnmpPduPacket.nextSequence());
		
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
			String instance = mibObject.getInstance();
			
			// instance must be one of the following:
			//      1) null.  In this case the object's oid value is used as-is.
			// 	2) a special keyword which indicates a specific
			// 	value will be retrieved and appended to the object's oid.
			//	Currently the only supported keywords are:
			//		"ifIndex" - current interface's ifIndex is retrieved from the database
			//		"ifAddress" - current interface's IP address
			//	3) an integer value indicating the instance of the MIB object to be
			// 	retrieved.  This value will be appended to the object's oid.
			//
			if (instance == null)
			{
				oid = new SnmpObjectId(mibObject.getOid());
			}
			else if (instance.equals(MibObject.INSTANCE_IFINDEX))
			{
				// Verify that we have a valid ifIndex value
				if (index.intValue() == -1)
				{
					if(log.isEnabledFor(Priority.WARN))
					{
						log.warn("getNextSnmpV1Pdu: valid 'ifIndex' value unavailable for interface " + ifAddress + " and MIB object: " + mibObject.getOid());
						log.warn("getNextSnmpV1Pdu: substituting instance value 0.");
					}
					oid = new SnmpObjectId(mibObject.getOid() + ".0");
				}
				else
				{
					oid = new SnmpObjectId(mibObject.getOid() + "." + index.toString());
				}
			}
			else if (instance.equals(MibObject.INSTANCE_IFADDRESS))
			{
				// Append current interface's ip Address
				oid = new SnmpObjectId(mibObject.getOid() + "." + ifAddress);
			}
			else 
			{
				// Attempt to convert the instance string to an integer value
				// If the instance string cannot be converted to an integer just
				// use an instance string of "0".
				int temp = -1;
				try 
				{
					temp = Integer.parseInt(instance);
				}
				catch (NumberFormatException nfe)
				{
					instance = "0";  
				}

				oid = new SnmpObjectId(mibObject.getOid() + "." + instance);
			}	
			
			// Add the variable binding to the pdu
			if (log.isDebugEnabled()) 
				log.debug("getNextSnmpV1Pdu: adding oid to pdu: " + oid.toString());

			pdu.addVarBind(new SnmpVarBind(oid));
		}	
		
		if (log.isDebugEnabled())
			log.debug("getNextSnmpV1Pdu: finished building next pdu, num vars in pdu=" + pdu.getLength());
		return pdu;
	}
	
	/**
	 * This method constructs the next SnmpPduPacket (pdu) for sending
	 * to the remote node.
	 * 
	 * For SNMPv2, GETBULK commands are built. Each constructed PDU contains
	 * a single varbind representing the next object to be retrieved.  If
	 * that object pertains to all the remote node's interfaces (such as 
	 * ifOctetsIn or ifOctetsOut) then maxRepetitions will be set to the
	 * number of interfaces in the ifTable and the object will be collected
	 * for every interface with a single GETBULK pdu. If the oid is a 
	 * singular instance which doesn't pertain to all interfaces then 
	 * a GETBULK pdu is built with nonRepeaters set to 1 and maxRepetitions
	 * set to 0 and only that individual value is collected.  Therefore,
	 * for SNMPv2 based collection the number of PDUs generated will
	 * be equal to (numInterfaces * numInterfaceBasedObjects) + 
	 * (numSingularObjects).
	 *
	 * @param ifAddress 	Interface address of the remote agent
	 * 
	 * @return An SnmpPduPacket appropriate for the SNMP version supported.
	 *
	 * @see org.opennms.protocols.snmp.SnmpNull		SnmpNull
	 * @see org.opennms.protocols.snmp.SnmpPduPacket 	SnmpPduPacket
	 */
	private SnmpPduPacket getNextSnmpV2Pdu(String ifAddress)
	{
		// Log4j category
		//
		Category log = ThreadCategory.getInstance(getClass());
		
		Integer index = null;
		SnmpPduPacket pdu = new SnmpPduBulk();
			
			// Set Max repetitions
			//
			((SnmpPduBulk)pdu).setMaxRepititions(m_numInterfaces-m_responses); 
			if (log.isDebugEnabled())
			log.debug("getNextSnmpV2Pdu: responseCount: " + m_responses + " maxReps: " + (m_numInterfaces-m_responses));
			
			if (m_responses == 0)
				index = new Integer(0);
			else
				index = new Integer(m_indexArray[m_responses-1]);
		
		if (log.isDebugEnabled())
			log.debug("getNextSnmpV2Pdu: starting_ifindex: " + index);
		
		pdu.setRequestId(SnmpPduPacket.nextSequence());
		
		// Use next oid from object list to create SnmpVarBind object
		// and add it to the pdu.
		//
		MibObject mibObject = (MibObject)m_objList_v2.get(0);
			SnmpObjectId oid = null;
			String instance = mibObject.getInstance();
			
			// instance must be one of the following:
			//      1) null.  In this case the object's oid value is used as-is.
			// 	2) a special keyword which indicates a specific
			// 	value will be retrieved and appended to the object's oid.
			//	Currently the only supported keywords are:
			//		"ifIndex" - current interface's ifIndex is retrieved from the database
			//		"ifAddress" - current interface's IP address
			//	3) an integer value indicating the instance of the MIB object to be
			// 	retrieved.  This value will be appended to the object's oid.
			//
			if (instance == null)
			{
				oid = new SnmpObjectId(mibObject.getOid());
			}
			else if (instance.equals(MibObject.INSTANCE_IFINDEX))
			{
				// Verify that we have a valid ifIndex value
				if (index.intValue() == -1)
				{
					if(log.isEnabledFor(Priority.WARN))
					{
					log.warn("getNextSnmpV2Pdu: valid 'ifIndex' value unavailable for interface " + ifAddress + " and MIB object: " + mibObject.getOid());
					log.warn("getNextSnmpV2Pdu: substituting instance value 0.");
					}
					oid = new SnmpObjectId(mibObject.getOid() + ".0");
				}
				else
				{
					oid = new SnmpObjectId(mibObject.getOid() + "." + index.toString());
				}
			}
			else if (instance.equals(MibObject.INSTANCE_IFADDRESS))
			{
				// Append current interface's ip Address
				oid = new SnmpObjectId(mibObject.getOid() + "." + ifAddress);
			}
			else 
			{
				// Attempt to convert the instance string to an integer value
				// If the instance string cannot be converted to an integer just
				// use an instance string of "0".
				int temp = -1;
				try 
				{
					temp = Integer.parseInt(instance);
				}
				catch (NumberFormatException nfe)
				{
					instance = "0";  
				}
				
				// Create new SnmpObjectId using object's identifer appended with the instance id
				//
					// For SNMPV2 we are using GetBulk requests which in effect
					// performs a GetNext as opposed to a Get.  Therefore we
					// append (instance - 1) to the object id.  If instance
					// is '0' we leave off the instance id entirely.
					
					if (instance.equals("0"))
						oid = new SnmpObjectId(mibObject.getOid());
					else
					{
						Integer temp_instance = new Integer(temp-1);
						oid = new SnmpObjectId(mibObject.getOid() + "." + temp_instance.toString());
					}
					((SnmpPduBulk)pdu).setNonRepeaters(1);
					((SnmpPduBulk)pdu).setMaxRepititions(0);
				}
			
			// Add the variable binding to the pdu
			if (log.isDebugEnabled()) 
			log.debug("getNextSnmpV2Pdu: adding oid to pdu: " + oid.toString());

			pdu.addVarBind(new SnmpVarBind(oid));

		return pdu;
	}
	
	/**
	 * This method is used to process received SNMP PDU packets from
	 * the remote agent. The method is part of the SnmpHandler interface
	 * and will be invoked when a PDU is successfully decoded. The method
	 * is passed the receiving session, the PDU command, and the actual
	 * PDU packet.
	 *
	 * For SNMPv2, all the data will be stored within a temporary 
	 * array.  Once all data has been acquired, it will be organized 
	 * by interface.  For any MIB OID whose index is zero, their data
	 * will be associated with the primary interface's index.
	 *
	 * For SNMPv1, a map will be created after each response.  Once again,
	 * if the MIB OID has a zero at the end, it will be linked with the
	 * primary interface.
	 *
	 * When all the data has been received from the session the signaler
	 * object, initialized in the constructor, is signaled. In addition,
	 * the receiving instance will call notifyAll() on itself at the same
	 * time.
	 *
	 * @param session	The SNMP Session that received the PDU
	 * @param command	The command contained in the received pdu
	 * @param pdu		The actual received PDU.
	 *
	 */
	public void snmpReceivedPdu(SnmpSession session, int command, SnmpPduPacket pdu)
	{ 
		boolean doNotify = true;

		// Log4j category
		//
		Category log = ThreadCategory.getInstance(getClass());
		
		if (log.isDebugEnabled())
			log.debug("snmpReceivedPdu: got an SNMP pdu, num vars=" + pdu.getLength());
			
		if(command == SnmpPduPacket.RESPONSE)
		{
			try
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
						log.warn("snmpReceivedPDU: Error during interface SNMP collection for interface " + 
								session.getPeer().getPeer().toString() + ", SNMP error text: " + 
								m_errorText[m_errorStatus]);
					
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
							
							// Remove the failing MIB object from the object list,
							//
							m_objList.remove((m_errorIndex-1) + m_oidListIndex);
							
							if (log.isDebugEnabled())
								log.debug("snmpReceivedPDU: Removing failing varbind and resending request...");
	
							// Rebuild the request PDU and resend
							//
							SnmpPduPacket nxtpdu = getNextSnmpV1Pdu(m_primaryIf);
							doNotify = false;
							session.send(nxtpdu, this);
						}
					}
				}
				else if (m_version == SnmpSMI.SNMPV2) 
				{
				//
				// SNMPv2 Responses
				//
					if (log.isDebugEnabled())
						log.debug("snmpReceivedPdu: interface SNMP response arrived.  Handling GETBULK response.");
					    
					// Get next MibObject from object list & define the stopping point
					//
					MibObject mibobj = (MibObject)m_objList_v2.get(0);
					SnmpObjectId stop_oid = new SnmpObjectId(stop_oid(mibobj.getOid()));
					
					// Iterate over the variable bindings in the response PDU and if
					// the current varbind is within scope as defined by the stop oid
					// add it to the temporary array and increment the response count.
					// 
					// If it falls within bounds of stopping OID, add to the temporary
					// array.  Otherwise, remove the MIB variable (so the next one
					// can be accessed), and reset responses back to zero.
					boolean done = false;
					int numVarBinds = pdu.getLength();
					for (int y = 0; y < numVarBinds && !done; y++) 
					{
						SnmpVarBind vb = pdu.getVarBindAt(y);
						
						if (stop_oid.compare(vb.getName()) > 0) 
						{
							// Check varbind for SNMPv2 error
							if (vb.getValue() instanceof org.opennms.protocols.snmp.SnmpV2Error)
							{
								done = true;
								if (log.isDebugEnabled())
									log.debug("SnmpIfCollector.snmpReceivedPdu: SNMPv2 error for oid: " + vb.getName() + " error: " + vb.getValue());
							}
							else
							{
								if (log.isDebugEnabled())
									log.debug("snmpReceivedPDU: addint vb to response list, oid name:value pair: " + vb.getName() + " : " + vb.getValue());
						
								m_responseVbList.add(vb);
								m_responses++;
	
								// If number of responses exceeds the number of interfaces
								// then we're done.
								//  
								if (m_responses >= m_numInterfaces)
									done = true;
								
								// If the MIB object has an instance identifier which is not
								// ifIndex or ifAddress then it doesn't need to be collected for
								// all interfaces
								if (!mibobj.getInstance().equals(MibObject.INSTANCE_IFINDEX) && 
									!mibobj.getInstance().equals(MibObject.INSTANCE_IFADDRESS))
								{
									done = true;
								}
							}
						}
						else 
						{
							done = true;
						}
					}
					
					if (done)
					{
						Object temp = m_objList_v2.remove(0);
						m_responses = 0;
					}
					
					// When the v2 object list is empty we've collected all the
					// data.  Iterate over the interface index array and for each
					// build a list of SnmpVarBind objects collected from the remote
					// node.  Each interface's variable bind list is then passed 
					// to the SNMPCollectorEntry constructor and used to build 
					// a map of retrieved values indexed by object identifier.
					if (m_objList_v2.isEmpty()) 
					{
						if (log.isDebugEnabled())
							log.debug("snmpReceivedPdu(): All data acquired.  Begin formatting maps.");

						for (int a = 0; a < m_indexArray.length; a++)
						{
							int varIndex = 0;
							
							SnmpVarBind[] vars = new SnmpVarBind[m_objList.size()];
 
							// Add varbinds from the responseVbList which correspond
							// to this interface's ifIndex
							//
							Iterator r = m_responseVbList.iterator();
							while (r.hasNext())
							{
								SnmpVarBind vb = (SnmpVarBind)r.next();
 
								// Extract the "instance" id from the current SnmpVarBind's object id
								String from_oid = vb.getName().toString();
								SnmpObjectId id = new SnmpObjectId(from_oid);
								int[] ids = id.getIdentifiers();
								int instance_id = ids[ids.length-1];
								String instance = Integer.toString(instance_id);
 
								// If current index matches instance id of OID in array, add to
								// variable array for storage.
								if (instance.equals(m_indexArray[a]))
								{
									vars[varIndex++] = vb;
								}
							}

							// Generate value/object identifier map
							SNMPCollectorEntry ent = new SNMPCollectorEntry(vars, m_objList, m_indexArray[a]);
							m_entries.add(ent);
						}
					}
					else 
					{
						SnmpPduPacket nxtpdu = getNextSnmpV2Pdu(m_primaryIf);
						doNotify = false;
					
						if (log.isDebugEnabled())
							log.debug("SnmpCollector.snmpReceivedPdu(): Sending next GETBULK packet.");
					
						session.send(nxtpdu, this);
					}
				}
				else  
				{
				//
				// SNMPv1 Response
				//
					// Add the collected data to an SNMPCollectorEntry object.
					// Each SNMPCollectorEntry will contain all of the data collected
					// for a single interface.  However, depending upon the total
					// number of objects to be collected for each interface, it 
					// may require more than one send/receive sequence to collect
					// everything.
					//
					if (log.isDebugEnabled())
						log.debug("snmpReceivedPDU: interface SNMP response arrived. Handling GET response.");
					
					// Store retrieved responses in an SNMPCollectorEntry object
					//
					// If collector entry is null it indicates that this is the first
					// response PDU for a new interface so create a new collector
					// entry to hold the retrieved data.
					//
					// Otherwise this is additional data for the
					// current interface, simply update the current collector entry.
					//
					if (m_collectorEntry == null)
					{
						m_collectorEntry = new SNMPCollectorEntry(pdu.toVarBindArray(), m_objList, m_indexArray[m_responses]);
					}
					else
					{
						m_collectorEntry.update(pdu.toVarBindArray(), m_indexArray[m_responses]);
					}
			
					// Increment index
					m_oidListIndex = m_oidListIndex + pdu.getLength();
					    
					// Have we collected all objects for the current
					// interface?
					if (m_oidListIndex < m_objList.size())
					{
						// No, so build next request PDU for current interface and send it to the agent
						//
						if (log.isDebugEnabled())
							log.debug("snmpReceivedPDU: more to collect...sending next request, m_oidListIndex=" + m_oidListIndex + " totalObjects=" + m_objList.size());
						SnmpPduPacket nxtpdu = getNextSnmpV1Pdu(m_primaryIf);
						doNotify = false;
						session.send(nxtpdu, this);
						doNotify = false;
					}
					else
					{
						// Yes, so add the collector entry to the list, increment response
						// count and see if all interfaces have been collected.
						//
						if (log.isDebugEnabled())
							log.debug("snmpReceivedPDU: completed collection for interface with ifIndex=" + m_indexArray[m_responses]);
						m_entries.add(m_collectorEntry);
						m_responses++; // increment response count
						m_collectorEntry = null;  // reset collector entry
						m_oidListIndex = 0; // reset oid index
						
						// Do we have additional interfaces to collect for?
						if (m_responses != m_indexArray.length) 
						{
							if (log.isDebugEnabled())
								log.debug("snmpReceivedPDU: ResponseCount: " + m_responses + ", InterfaceCount: " + m_numInterfaces + " Generating next GET PDU");

							SnmpPduPacket nxtpdu = getNextSnmpV1Pdu(m_primaryIf);
							doNotify = false;
							session.send(nxtpdu, this);
						}
					}
				}
				
			} 
			catch (Throwable t)
			{
				if(log.isEnabledFor(Priority.WARN))
					log.warn("snmpReceivedPdu: Unexpected exception while processing SNMP response packet.", t);
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
	
	/**
	 * This method is part of the SnmpHandler interface and called when
	 * an internal error happens in a session. This is usually the result
	 * of an I/O error. This method will not be called if the session times
	 * out sending a packet, see snmpTimeoutError for timeout handling.
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
			log.debug("snmpInternalError: interface SNMP collection failed for interface " + session.getPeer().getPeer().toString() + ", SnmpSession errCode: " + error);

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
	 * This method is part of the SnmpHandler interface and is invoked
	 * when the SnmpSession does not receive a reply after exhausting 
	 * the retransmission attempts.
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
			log.debug("snmpTimeoutError: interface SNMP collection failed for interface " + session.getPeer().getPeer().toString());

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
	 * Returns the success or failure code for collection
	 * of the data.
	 */
	public boolean failed()
	{
		return m_error;
	}
	
	/**
	 * Returns true if SNMP collection failed due to timeout.
	 * Otherwise, returns false.
	 */
	public boolean timedout()
	{
		if (m_error)
			return m_timeout;
		else
			return false;
	}

       /**
	* Returns the list of all entry maps that can be used
	* to access all the information from the service polling.
	*/
	
	public java.util.List getEntries() {
	    
	    return m_entries;
	}
	
	/**
	 * This method is responsible for building a new object list consisting
	 * of all unique oids to be collected for all interfaces represented within
	 * the interface map.  The new list can then be used for SNMPv2 collection.
	 *
	 * @param ifMap		Map of IfInfo objects indexed by ifIndex
	 *
	 * @return unified MibObject list
	 */
	private static List buildV2CombinedOidList(Map ifMap)
	{
		List allOids = new ArrayList();
		
		// Iterate over all the interface's in the interface map
		//
		if (ifMap != null)
		{
			Iterator i = ifMap.values().iterator();
			while (i.hasNext())
			{
				IfInfo ifInfo = (IfInfo)i.next();
				List ifOidList = ifInfo.getOidList();
			
				// Add unique interface oid's to the list
				//
				Iterator j = ifOidList.iterator(); 
				while (j.hasNext())
				{
					MibObject oid = (MibObject)j.next();
					if (!allOids.contains(oid))
						allOids.add(oid);
				}
			}
		}
		
		return allOids;
	}
}

