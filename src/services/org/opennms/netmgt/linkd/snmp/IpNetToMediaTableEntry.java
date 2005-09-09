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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.linkd.snmp;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.linkd.snmp.NamedSnmpVar;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpPduBulk;
import org.opennms.protocols.snmp.SnmpPduPacket;
import org.opennms.protocols.snmp.SnmpPduRequest;
import org.opennms.protocols.snmp.SnmpSMI;
import org.opennms.protocols.snmp.SnmpVarBind;

/**
 *<P>The IpNetToMediaTableEntry class is designed to hold all the MIB-II
 * information for one entry in the ipNetToMediaTable. The table effectively
 * contains a list of these entries, each entry having information
 * about one physical address. The entry contains the ifindex binding, the mac address,
 * ip address and entry type.</P>
 *
 * <P>This object is used by the IpNetToMediaTable to hold infomation
 * single entries in the table. See the IpNetToMediaTable documentation
 * form more information.</P>
 *
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio</A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya</A>
 * @author <A HREF="mailto:weave@oculan.com">Weave</A>
 * @author <A>Jon Whetzel</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 *
 * @see IpNetToMediaTable
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 */
public final class IpNetToMediaTableEntry
	extends java.util.TreeMap
{
	// Lookup strings for specific table entries
	//
	public final static	String	INTM_INDEX		= "ipNetToMediaIfIndex";
	public final static	String	INTM_PHYSADDR	= "ipNetToMediaPhysAddress";
	public final static	String	INTM_NETADDR	= "ipNetToMediaNetAddress";
	public final static	String	INTM_TYPE		= "ipNetToMediatype";

	/**
	 * <P>The keys that will be supported by default from the 
	 * TreeMap base class. Each of the elements in the list
	 * are an instance of the IpNetToMediatable. Objects
	 * in this list should be used by multiple instances of
	 * this class.</P>
	 */
	private static NamedSnmpVar[]	ipNetToMedia_elemList = null;
	
	/**
	 * <P>Initialize the element list for the class. This
	 * is class wide data, but will be used by each instance.</P>
	 */
	static
	{
		ipNetToMedia_elemList = new NamedSnmpVar[4];
		int ndx = 0;
		
		/**
		 * The interface on which this entry's equivalence
 		 * is effective. The interface identified by a
 		 * particular value of this index is the same
 		 * interface as identified by the same value of
 		 * ifIndex.
		 */
		ipNetToMedia_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,			INTM_INDEX,		".1.3.6.1.2.1.4.22.1.1",  1);

		/**
		 * The media-dependent `physical' address. 
		 */
		ipNetToMedia_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING,	INTM_PHYSADDR,	".1.3.6.1.2.1.4.22.1.2",  2);
		
		/**
		 * The IpAddress corresponding to the media-
		 * dependent `physical' address.
		 */
		ipNetToMedia_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPIPADDRESS,		INTM_NETADDR,	".1.3.6.1.2.1.4.22.1.3",  3);
		
		/**
		 * The type of mapping.
 		 * Setting this object to the value invalid(2) has
 		 * the effect of invalidating the corresponding entry
 		 * in the ipNetToMediaTable. That is, it effectively
 		 * disassociates the interface identified with said
 		 * entry from the mapping identified with said entry.
 		 * It is an implementation-specific matter as to
 		 * whether the agent removes an invalidated entry
 		 * from the table. Accordingly, management stations
 		 * must be prepared to receive tabular information
 		 * from agents that corresponds to entries not
 		 * currently in use. Proper interpretation of such
 		 * entries requires examination of the relevant
 		 * ipNetToMediaType object.
		 */
		ipNetToMedia_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,			INTM_TYPE,		".1.3.6.1.2.1.4.22.1.4",  4);
	}
	

	/**
	 * <P>The TABLE_OID is the object identifier that represents
	 * the root of the IP Address table in the MIB forest.</P>
	 */
	public static final String	TABLE_OID	= ".1.3.6.1.2.1.4.22.1";	// start of table (GETNEXT)

	/**
	 * <P>The SnmpObjectId that represents the root of the 
	 * ipNetToMediaTable tree. It is created when the class is 
	 * initialized and contains the value of TABLE_OID.
	 *
	 * @see #TABLE_OID
	 */
	public static final SnmpObjectId ROOT = new SnmpObjectId(TABLE_OID);

	/**
	 * <P>Creates a default instance of the ipNetToMedia
	 * table entry map. The map represents a singular
	 * instance of the mac address table. Each column in
	 * the table for the loaded instance may be retreived
	 * either through its name or object identifier.</P>
	 *
	 * <P>The initial table is constructied with zero
	 * elements in the map.</P>
	 */
	public IpNetToMediaTableEntry( )
	{
		super();
	}
	
	/**
	 * <P>The class constructor used to initialize the
	 * object to its initial state. Although the
	 * object's member variables can change after an
	 * instance is created, this constructor will
	 * initialize all the variables as per their named
	 * variable from the passed array of SNMP varbinds.</P>
	 *
	 * <P>If the information in the object should not be
	 * modified then a <EM>final</EM> modifier can be 
	 * applied to the created object.</P>
	 *
	 * @param vars	The array of variable bindings.
	 */
	public IpNetToMediaTableEntry(SnmpVarBind[] vars)
	{
		this();
		update(vars);
	}

	/**
	 * <P>This method is used to update the map
	 * with the current information from the agent.
	 * The array of variables should be all the
	 * elements in the address row.</P>
	 *
	 * </P>This does not clear out any column in the
	 * actual row that does not have a definition.</P>
	 *
	 * @param vars	The variables in the interface row.
	 *
	 */
	public void update(SnmpVarBind[] vars)
	{
		Category log = ThreadCategory.getInstance(getClass());

		//
		// iterate through the variable bindings
		// and set the members appropiately.
		//
		// Note: the creation of the snmp object id
		// is in the outer loop to limit the times a
		// new object is created.
		//
		for(int x = 0; x < ipNetToMedia_elemList.length; x++)
		{
			SnmpObjectId id = new SnmpObjectId(ipNetToMedia_elemList[x].getOid());

			for(int y = 0; y < vars.length; y++)
			{
				if(id.isRootOf(vars[y].getName()))
				{
					try 
					{
						//
						// Retrieve the class object of the expected SNMP data type for this element
						//
						Class classObj = ipNetToMedia_elemList[x].getTypeClass();
						
						//
						// If the SnmpSyntax object matches the expected class 
						// then store it in the map. Else, store a null pointer
						// in the map.
						//
						if (classObj == null || classObj.isInstance(vars[y].getValue()))
						{
							if (log.isDebugEnabled())
							{
								log.debug("update: Types match!  SNMP Alias: "
									  + ipNetToMedia_elemList[x].getAlias() + "  Vars[y]: " + vars[y].toString());
        						}
							put(ipNetToMedia_elemList[x].getAlias(), vars[y].getValue());
							put(ipNetToMedia_elemList[x].getOid(), vars[y].getValue());
						}
						else
						{
							if (log.isDebugEnabled())
							{
								log.debug("update: variable '" + vars[y].toString() 
									  + "' does NOT match expected type '" + ipNetToMedia_elemList[x].getType() + "'");
							}
							put(ipNetToMedia_elemList[x].getAlias(), null);
							put(ipNetToMedia_elemList[x].getOid(), null);
						}
					}
					catch (ClassNotFoundException e)
					{
						log.error("Failed to retreive SNMP type class for element: " + ipNetToMedia_elemList[x].getAlias(), e);
					}
					catch (NullPointerException e)
					{
						log.error("Invalid reference", e);
					}
				}
			}
		}
	}

	/**
	 * <P>If the SNMP version is V1, this method is used to get a 
	 * generic SNMP GETNEXT PDU that contains one varbind per member 
	 * element.</P>
	 *
	 * <P>If the SNMP version is V2, this method is used to get an
	 * SNMP GETBULK PDU with a single varbind containing the TABLE_OID
	 * object identifier.</P>
	 *
	 * <P>The PDU can then be used to perform an <EM>SNMP walk</EM> of 
	 * the MIB-II IP Address table of a remote host.</P>
	 * 
	 * @param version	SnmpSMI.SNMPV1 or SnmpSMI.SNMPV2
	 * 
	 * @return An SnmpPduPacket object with a command of GETNEXT (for SNMPv1)
	 * or GETBULK (for SNMPv2).
	 *
	 */
	public static SnmpPduPacket getNextPdu(int version)
	{
		SnmpPduPacket pdu = null;

		if (version == SnmpSMI.SNMPV2) 
		{
			pdu = new SnmpPduBulk();
			((SnmpPduBulk)pdu).setMaxRepititions(10);
			pdu.setRequestId(SnmpPduPacket.nextSequence());
			SnmpObjectId   oid = new SnmpObjectId(TABLE_OID);
			pdu.addVarBind(new SnmpVarBind(oid));
		}
		else 
		{
			pdu = new SnmpPduRequest(SnmpPduPacket.GETNEXT);
			pdu.setRequestId(SnmpPduPacket.nextSequence());
			for(int x = 0; x < ipNetToMedia_elemList.length; x++)
			{
				SnmpObjectId   oid = new SnmpObjectId(ipNetToMedia_elemList[x].getOid());
				pdu.addVarBind(new SnmpVarBind(oid));
			}
		}

		return pdu;
	}

	
 	/**
	 *<P>This method will determine where the cut off point will be for
	 * valid data from the response to the GETBULK packet.  By using the
	 * size of the element list, listed above, we can determine the 
	 * proper index for this task.<P>
	 */
        public static SnmpObjectId stop_oid() 
	{
		Integer endindex = new Integer(ipNetToMedia_elemList.length+1);
		String endoid = new String(TABLE_OID + "." + endindex.toString());
		SnmpObjectId oid = new SnmpObjectId(endoid);

	    	return oid;
	}
	
	/** 
	 * <P>Returns the number of entries in the MIB-II ipNetToMediaTable element list.</P>
	 */
	public static int getElementListSize()
	{
		return ipNetToMedia_elemList.length;
	}

}   
