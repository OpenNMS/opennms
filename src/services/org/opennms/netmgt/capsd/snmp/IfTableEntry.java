//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
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
//
package org.opennms.netmgt.capsd.snmp;

import java.util.*;
import org.opennms.protocols.snmp.*;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;


/**
 * <P>This object contains a list of all the elements
 * defined in the MIB-II interface table. An instance
 * object is initialized by calling the constructor and
 * passing in a variable list from an SNMP PDU. The
 * actual data can be recovered via the base class map
 * interface.</P>
 *
 * <P>Once an instance is created and its data set either
 * via the constructor or from the update method, the actual
 * elements can be retreived using the instance names. The 
 * names include: <EM>ifIndex</EM>, <EM>ifDescr</EM>, 
 * <EM>ifSpeed</EM>, <EM>etc al</EM>. The information can
 * also be accessed by using the complete object identifer
 * for the entry.</P>
 *
 * <P>For more information on the individual fields, and
 * to find out their respective object identifiers see
 * RFC1213 from the IETF.</P>
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya</A>
 * @author <A HREF="mailto:weave@opennms.org">Weave</A>
 * @author <A>Jon Whetzel</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 */
public final class IfTableEntry 
	extends java.util.TreeMap
{
	//
	// Lookup strings for specific table entries
	//
	public final static	String	IF_INDEX 	= "ifIndex";
	public final static	String	IF_DESCR	= "ifDescr";
	public final static	String	IF_TYPE 	= "ifType";
	public final static	String	IF_MTU 		= "ifMtu";
	public final static	String	IF_SPEED 	= "ifSpeed";
	public final static	String	IF_PHYS_ADDR	= "ifPhysAddr";
	public final static	String	IF_ADMIN_STATUS = "ifAdminStatus";
	public final static	String	IF_OPER_STATUS 	= "ifOperStatus";
	public final static	String	IF_LAST_CHANGE 	= "ifLastChange";
	public final static	String	IF_IN_OCTETS 	= "ifInOctets";
	public final static	String	IF_IN_UCAST 	= "ifInUcastPkts";
	public final static	String	IF_IN_NUCAST 	= "ifInNUcastPkts";
	public final static	String	IF_IN_DISCARDS 	= "ifInDiscards";
	public final static	String	IF_IN_ERRORS 	= "ifInErrors";
	public final static	String	IF_IN_UKNOWN_PROTOS 	= "ifInUnknownProtos";
	public final static	String	IF_OUT_OCTETS 	= "ifOutOctets";
	public final static	String	IF_OUT_UCAST 	= "ifOutUcastPkts";
	public final static	String	IF_OUT_NUCAST 	= "ifOutNUcastPkts";
	public final static	String	IF_OUT_DISCARDS = "ifOutDiscards";
	public final static	String	IF_OUT_ERRORS 	= "ifOutErrors";
	public final static 	String	IF_OUT_QLEN	= "ifOutQLen";
	public final static 	String	IF_SPECIFIC	= "ifSpecific";
	
	/**
	 * <P>The keys that will be supported by default from the 
	 * TreeMap base class. Each of the elements in the list
	 * are an instance of the SNMP Interface table. Objects
	 * in this list should be used by multiple instances of
	 * this class.</P>
	 */
	private static NamedSnmpVar[]	ms_elemList = null;
	
	/**
	 * <P>Initialize the element list for the class. This
	 * is class wide data, but will be used by each instance.</P>
	 */
	static
	{
		ms_elemList = new NamedSnmpVar[9];
		int ndx = 0;
		
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, 		IF_INDEX, 		".1.3.6.1.2.1.2.2.1.1",  1);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, 	IF_DESCR, 		".1.3.6.1.2.1.2.2.1.2",  2);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, 		IF_TYPE, 		".1.3.6.1.2.1.2.2.1.3",  3);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, 		IF_MTU, 		".1.3.6.1.2.1.2.2.1.4",  4);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPGAUGE32,		IF_SPEED, 		".1.3.6.1.2.1.2.2.1.5",  5);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING,	IF_PHYS_ADDR,		".1.3.6.1.2.1.2.2.1.6",  6);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, 		IF_ADMIN_STATUS,	".1.3.6.1.2.1.2.2.1.7",  7);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, 		IF_OPER_STATUS, 	".1.3.6.1.2.1.2.2.1.8",  8);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPTIMETICKS, 	IF_LAST_CHANGE, 	".1.3.6.1.2.1.2.2.1.9",  9);
//		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32, 	IF_IN_OCTETS,		".1.3.6.1.2.1.2.2.1.10", 10);
//		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,	IF_IN_UCAST,		".1.3.6.1.2.1.2.2.1.11", 11);
//		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,	IF_IN_NUCAST,	 	".1.3.6.1.2.1.2.2.1.12", 12);
//		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,	IF_IN_DISCARDS, 	".1.3.6.1.2.1.2.2.1.13", 13);
//		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,	IF_IN_ERRORS, 		".1.3.6.1.2.1.2.2.1.14", 14);
//		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,	IF_IN_UKNOWN_PROTOS,	".1.3.6.1.2.1.2.2.1.15", 15);
//		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,	IF_OUT_OCTETS, 		".1.3.6.1.2.1.2.2.1.16", 16);
//		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,	IF_OUT_UCAST, 		".1.3.6.1.2.1.2.2.1.17", 17);
//		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,	IF_OUT_NUCAST, 		".1.3.6.1.2.1.2.2.1.18", 18);
//		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,	IF_OUT_DISCARDS,	".1.3.6.1.2.1.2.2.1.19", 19);
//		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,	IF_OUT_ERRORS, 		".1.3.6.1.2.1.2.2.1.20", 20);
//		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPGAUGE32,		IF_OUT_QLEN, 		".1.3.6.1.2.1.2.2.1.21", 21);
//		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOBJECTID,	IF_SPECIFIC,	 	".1.3.6.1.2.1.2.2.1.22", 22);
	}

	/**
	 * <P>The TABLE_OID is the object identifier that represents
	 * the root of the interface table in the MIB forest.</P>
	 */
	public static final String	TABLE_OID	= ".1.3.6.1.2.1.2.2.1";	// start of table (GETNEXT)
	
	/**
	 * <P>The SnmpObjectId that represents the root of the 
	 * interface tree. It is created when the class is 
	 * initialized and contains the value of TABLE_OID.
	 *
	 * @see #TABLE_OID
	 */
	public static final SnmpObjectId ROOT = new SnmpObjectId(TABLE_OID);

	/**
	 * <P>Creates a default instance of the interface
	 * table entry map. The map represents a singular
	 * instance of the interface table. Each column in
	 * the table for the loaded instance may be retreived
	 * either through its name or object identifier.</P>
	 *
	 * <P>The initial table is constructied with zero
	 * elements in the map.</P>
	 */
	public IfTableEntry()
	{
		super();
	}
	
	/**
	 * <P>The class constructor used to initialize the 
	 * object to its initial state. Although the
	 * object's attributes and data can be changed after
	 * its created, this constructor will initialize
	 * all the variables as per their named varbind
	 * in the passed array. This array should have been
	 * collected from an SnmpPduRequest that was received
	 * from a remote host.</P>
	 *
	 * @param vars	The array of variable bindings.
	 *
	 */
	public IfTableEntry(SnmpVarBind[] vars)
	{
		// Initialize the map
		//
		this();
		update(vars);
	}
	
	/**
	 * <P>This method is used to update the map
	 * with the current information from the agent.
	 * The array of variables should be all the
	 * elements in the interfaces row.</P>
	 *
	 * </P>This does not clear out any column in the
	 * actual ifEntry row that does not have a definition.</P>
	 *
	 * @param vars	The variables in the interface row.
	 *
	 */
	public void update(SnmpVarBind[] vars)
	{
		//
		// iterate through the variable bindings
		// and set the members appropiately.
		//
		// Note: the creation of the snmp object id
		// is in the outer loop to limit the times a
		// new object is created.
		//
		for(int x = 0; x < ms_elemList.length; x++)
		{
			SnmpObjectId id = new SnmpObjectId(ms_elemList[x].getOid());
			for(int y = 0; y < vars.length; y++)
			{
			    	if(id.isRootOf(vars[y].getName()))
				{
					try 
					{
						//
						// Retrieve the class object of the expected SNMP data type for this element
						//
						Class classObj = ms_elemList[x].getTypeClass();
						
						//
						// If the SnmpSyntax object matches the expected class 
						// then store it in the map. Else, store a null pointer
						// in the map.
						//
						if (classObj.isInstance(vars[y].getValue()))
						{
							put(ms_elemList[x].getAlias(), vars[y].getValue());
							put(ms_elemList[x].getOid(), vars[y].getValue());
						}
						else
						{
							put(ms_elemList[x].getAlias(), null);
							put(ms_elemList[x].getOid(), null);
						}
					}
					catch (ClassNotFoundException e)
					{
						Category log = ThreadCategory.getInstance(getClass());
						log.error("Failed retrieving SNMP type class for element: " + ms_elemList[x].getAlias(), e);
					}
					catch (NullPointerException e)
					{
						Category log = ThreadCategory.getInstance(getClass());
						log.error("Invalid reference", e);
					}
				}
			}
		}
	}

	/**
	 * <P>This method is used to get a generic SNMPv1 GETNEXT PDU that
	 * contains one varbind per member element. The PDU can then be
	 * used to perform an <EM>SNMP walk</EM> of the MIB-II interface
	 * table on remote host via consecutive SNMPv1 GETNEXT requests.</P>
	 *
	 * @return An SnmpPduPacket with the command of GETNEXT and
	 * 	one varbind for each member variable.
	 */
	public static SnmpPduPacket getNextPdu()
	{
		SnmpPduPacket pdu = new SnmpPduRequest(SnmpPduRequest.GETNEXT);

		pdu.setRequestId(pdu.nextSequence());
		for(int x = 0; x < ms_elemList.length; x++)
		{
			SnmpObjectId   oid = new SnmpObjectId(ms_elemList[x].getOid());
			pdu.addVarBind(new SnmpVarBind(oid));
		}
		Category log = ThreadCategory.getInstance(IfTableEntry.class);
		if(log.isDebugEnabled())
		{
			log.debug("snmpReceivedPdu: generating next SNMPv1 request with id: " + pdu.getRequestId());
		}

		return pdu;
	}

	/**
	 * <P>This method is used to get a generic SNMPv2 GETBULK PDU that
	 * contains a single varbind -- the ifTable root oid. The PDU can then be
	 * used to perform an <EM>SNMP walk</EM> of the MIB-II interface
	 * table on remote host via SNMPv2 GETBULK REQUESTS.</P>
	 *
	 * @return An SnmpPduPacket with the command of GETBULK.
	 */
	public static SnmpPduPacket getBulkPdu(int numInterfaces)
	{
		SnmpPduPacket pdu = new SnmpPduBulk();

		((SnmpPduBulk)pdu).setMaxRepititions(numInterfaces * ms_elemList.length);
		pdu.setRequestId(pdu.nextSequence());

		SnmpObjectId oid = new SnmpObjectId(TABLE_OID);
		pdu.addVarBind(new SnmpVarBind(oid));

		Category log = ThreadCategory.getInstance(IfTableEntry.class);
		if(log.isDebugEnabled())
		{
			log.debug("snmpReceivedPdu: generating next SNMPv2 request with id: " + pdu.getRequestId());
		}
		return pdu;
	}
	
	/**
	 *<P>This method will determine where the cut off point will be for
	 * valid data from the response to the GETBULK packet.  By using the
	 * size of the element list, listed above, we can determine the 
	 * proper index for this task.</P>
	 */
        public static SnmpObjectId stop_oid() 
	{
		Integer endindex = new Integer(ms_elemList.length+1);
		String endoid    = new String(TABLE_OID + "." + endindex.toString());
		SnmpObjectId oid = new SnmpObjectId(endoid);

		return oid;
	}

       
       /**
	*<P>This method will generate a packet that will go out and retrieve the
	*ifNumber variable from the MIB, the variable that states the number of
	*interfaces for the device.</P>
	*/
        public static SnmpPduRequest getIfNumberPdu() 
	{
		SnmpPduRequest pdu = new SnmpPduRequest(SnmpPduRequest.GETNEXT);
		SnmpObjectId oid   = new SnmpObjectId(".1.3.6.1.2.1.2.1");
		pdu.addVarBind(new SnmpVarBind(oid));
		pdu.setRequestId(pdu.nextSequence());

		return pdu;
	}
	   
	/** 
	 * <P>Returns the number of entries in the MIB-II ifTable element list.</P>
	 */
	public static int getElementListSize()
	{
		return ms_elemList.length;
	}
}

