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
 *<P>The IpRouteTableEntry class is designed to hold all the MIB-II
 * information for one entry in the ipRouteTable. The table effectively
 * contains a list of these entries, each entry having information
 * about ip route. The entry contains:
 *	ipRouteDest,
 *	ipRouteIfIndex,
 *	ipRouteMetric1,
 *	ipRouteMetric2,
 *	ipRouteMetric3,
 *	ipRouteMetric4,
 *	ipRouteNextHop,
 *	ipRouteType,
 *	ipRouteProto,
 *	ipRouteAge,
 *	ipRouteMask,
 *	ipRouteMetric5,
 *	ipRouteInfo.</P>
 *
 * <P>This object is used by the IpRouteTable to hold infomation
 * single entries in the table. See the IpRouteTable documentation
 * form more information.</P>
 *
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio</A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya</A>
 * @author <A HREF="mailto:weave@oculan.com">Weave</A>
 * @author <A>Jon Whetzel</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 *
 * @see IpRouteTable
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 */
public final class IpRouteTableEntry
	extends java.util.TreeMap
{
	// Lookup strings for specific table entries
	//
	public final static	String	IP_ROUTE_DEST		= "ipRouteDest";
	public final static	String	IP_ROUTE_IFINDEX	= "ipRouteIfIndex";
	public final static	String	IP_ROUTE_METRIC1	= "ipRouteMetric1";
	public final static	String	IP_ROUTE_METRIC2	= "ipRouteMetric2";
	public final static	String	IP_ROUTE_METRIC3	= "ipRouteMetric3";
	public final static	String	IP_ROUTE_METRIC4	= "ipRouteMetric4";
	public final static	String	IP_ROUTE_NXTHOP 	= "ipRouteNextHop";
	public final static	String	IP_ROUTE_TYPE   	= "ipRouteType";
	public final static	String	IP_ROUTE_PROTO  	= "ipRouteProto";
	public final static	String	IP_ROUTE_AGE    	= "ipRouteAge";
	public final static	String	IP_ROUTE_MASK   	= "ipRouteMask";
	public final static	String	IP_ROUTE_METRIC5	= "ipRouteMetric5";
	public final static	String	IP_ROUTE_INFO   	= "ipRouteInfo";
	
	
	
	
	/**
	 * <P>The keys that will be supported by default from the 
	 * TreeMap base class. Each of the elements in the list
	 * are an instance of the IpRoutetable. Objects
	 * in this list should be used by multiple instances of
	 * this class.</P>
	 */
	private static NamedSnmpVar[]	ipRoute_elemList = null;
	
	/**
	 * <P>Initialize the element list for the class. This
	 * is class wide data, but will be used by each instance.</P>
	 */
	static
	{
		ipRoute_elemList = new NamedSnmpVar[13];
		int ndx = 0;
		
	/** The destination IP address of this route. An
	 * entry with a value of 0.0.0.0 is considered a
	 * default route. Multiple routes to a single
	 * destination can appear in the table, but access to
	 * such multiple entries is dependent on the table-
	 * access mechanisms defined by the network
	 * management protocol in use.
	 */
		ipRoute_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPIPADDRESS,	IP_ROUTE_DEST,		".1.3.6.1.2.1.4.21.1.1",  1);
	
	/**
	 * The index value which uniquely identifies the
	 * local interface through which the next hop of this
	 * route should be reached. The interface identified
	 * by a particular value of this index is the same
	 * interface as identified by the same value of
	 * ifIndex.
	 */
		ipRoute_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,	    IP_ROUTE_IFINDEX,	".1.3.6.1.2.1.4.21.1.2",  2);
		
	/**
	 * The primary routing metric for this route. The
	 * semantics of this metric are determined by the
 	 * routing-protocol specified in the route's
 	 * ipRouteProto value. If this metric is not used,
 	 * its value should be set to -1.
	 */
		ipRoute_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,		IP_ROUTE_METRIC1,	".1.3.6.1.2.1.4.21.1.3",  3);

	/**
	 * An alternate routing metric for this route. The
	 * semantics of this metric are determined by the
	 * routing-protocol specified in the route's
	 * ipRouteProto value. If this metric is not used,
 	 * its value should be set to -1.
	 */
		ipRoute_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,		IP_ROUTE_METRIC2,	".1.3.6.1.2.1.4.21.1.4",  4);

		/**
		 * An alternate routing metric for this route. The
		 * semantics of this metric are determined by the
		 * routing-protocol specified in the route's
		 * ipRouteProto value. If this metric is not used,
	 	 * its value should be set to -1.
		 */

		ipRoute_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,		IP_ROUTE_METRIC3,	".1.3.6.1.2.1.4.21.1.5",  5);

		/**
		 * An alternate routing metric for this route. The
		 * semantics of this metric are determined by the
		 * routing-protocol specified in the route's
		 * ipRouteProto value. If this metric is not used,
	 	 * its value should be set to -1.
		 */
		ipRoute_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,	    IP_ROUTE_METRIC4,	".1.3.6.1.2.1.4.21.1.6",  6);

		/**
		 * The IP address of the next hop of this route.
		 * (In the case of a route bound to an interface
		 * which is realized via a broadcast media, the value
		 * of this field is the agent's IP address on that
		 * interface.)
		 */
		ipRoute_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPIPADDRESS,	IP_ROUTE_NXTHOP,	".1.3.6.1.2.1.4.21.1.7",  7);
		
		/**
		 * The type of route. Note that the values
		 * direct(3) and indirect(4) refer to the notion of
		 * direct and indirect routing in the IP
 		 * architecture.
 		 * Setting this object to the value invalid(2) has
 		 * the effect of invalidating the corresponding entry
 		 * in the ipRouteTable object. That is, it
 		 * effectively disassociates the destination
 		 * identified with said entry from the route
 		 * identified with said entry. It is an
 		 * implementation-specific matter as to whether the
 		 * agent removes an invalidated entry from the table.
 		 * Accordingly, management stations must be prepared
 		 * to receive tabular information from agents that
 		 * corresponds to entries not currently in use.
 		 * Proper interpretation of such entries requires
 		 * examination of the relevant ipRouteType object.
		 */
		ipRoute_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,		IP_ROUTE_TYPE,		".1.3.6.1.2.1.4.21.1.8",  8);

		/**
		 * The routing mechanism via which this route was
 		 * learned. Inclusion of values for gateway routing
 		 * protocols is not intended to imply that hosts
 		 * should support those protocols.
		 */
		ipRoute_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,		IP_ROUTE_PROTO,		".1.3.6.1.2.1.4.21.1.9",  9);
		
		/**
		 * The number of seconds since this route was last
		 * updated or otherwise determined to be correct.
 		 * Note that no semantics of `too old' can be implied
 		 * except through knowledge of the routing protocol
 		 * by which the route was learned.
		 */
		ipRoute_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,	    IP_ROUTE_AGE,	    ".1.3.6.1.2.1.4.21.1.10",  10);
		
		/**
		 * Indicate the mask to be logical-ANDed with the
 		 * destination address before being compared to the
 		 * value in the ipRouteDest field. For those systems
 		 * that do not support arbitrary subnet masks, an
 		 * agent constructs the value of the ipRouteMask by
 		 * determining whether the value of the correspondent
 		 * ipRouteDest field belong to a class-A, B, or C
 		 * network, and then using one of:
 		 * mask network
 		 * 255.0.0.0 class-A
 		 * 255.255.0.0 class-B
 		 * 255.255.255.0 class-C
 		 * If the value of the ipRouteDest is 0.0.0.0 (a
 		 * default route), then the mask value is also
 		 * 0.0.0.0. It should be noted that all IP routing
 		 * subsystems implicitly use this mechanism.
		 */
		ipRoute_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPIPADDRESS,	IP_ROUTE_MASK,  	".1.3.6.1.2.1.4.21.1.11",  11);

		/**
		 * An alternate routing metric for this route. The
		 * semantics of this metric are determined by the
		 * routing-protocol specified in the route's
		 * ipRouteProto value. If this metric is not used,
	 	 * its value should be set to -1.
		 */
		ipRoute_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,		IP_ROUTE_METRIC5,	".1.3.6.1.2.1.4.21.1.12",  12);

		/**
		 * A reference to MIB definitions specific to the
		 * particular routing protocol which is responsible
 		 * for this route, as determined by the value
  		 * specified in the route's ipRouteProto value. If
  		 * this information is not present, its value should
  		 * be set to the OBJECT IDENTIFIER { 0 0 }, which is
  		 * a syntactically valid object identifier, and any
  		 * conformant implementation of ASN.1 and BER must be
  		 * able to generate and recognize this value.
		 */
		ipRoute_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOBJECTID,		IP_ROUTE_INFO,		".1.3.6.1.2.1.4.21.1.13",  13);
	}
	

	/**
	 * <P>The TABLE_OID is the object identifier that represents
	 * the root of the IP ROUTE table in the MIB forest.</P>
	 */
	public static final String	TABLE_OID	= ".1.3.6.1.2.1.4.21.1";	// start of table (GETNEXT)

	/**
	 * <P>The SnmpObjectId that represents the root of the 
	 * ipNetToMediaTable tree. It is created when the class is 
	 * initialized and contains the value of TABLE_OID.
	 *
	 * @see #TABLE_OID
	 */
	public static final SnmpObjectId ROOT = new SnmpObjectId(TABLE_OID);

	/**
	 * <P>Creates a default instance of the ipROUTE
	 * table entry map. The map represents a singular
	 * instance of the routing table. Each column in
	 * the table for the loaded instance may be retreived
	 * either through its name or object identifier.</P>
	 *
	 * <P>The initial table is constructied with zero
	 * elements in the map.</P>
	 */
	public IpRouteTableEntry( )
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
	public IpRouteTableEntry(SnmpVarBind[] vars)
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
		for(int x = 0; x < ipRoute_elemList.length; x++)
		{
			SnmpObjectId id = new SnmpObjectId(ipRoute_elemList[x].getOid());

			for(int y = 0; y < vars.length; y++)
			{
				if(id.isRootOf(vars[y].getName()))
				{
					try 
					{
						//
						// Retrieve the class object of the expected SNMP data type for this element
						//
						Class classObj = ipRoute_elemList[x].getTypeClass();
						
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
									  + ipRoute_elemList[x].getAlias() + "  Vars[y]: " + vars[y].toString());
        						}
							put(ipRoute_elemList[x].getAlias(), vars[y].getValue());
							put(ipRoute_elemList[x].getOid(), vars[y].getValue());
						}
						else
						{
							if (log.isDebugEnabled())
							{
								log.debug("update: variable '" + vars[y].toString() 
									  + "' does NOT match expected type '" + ipRoute_elemList[x].getType() + "'");
							}
							put(ipRoute_elemList[x].getAlias(), null);
							put(ipRoute_elemList[x].getOid(), null);
						}
					}
					catch (ClassNotFoundException e)
					{
						log.error("Failed to retreive SNMP type class for element: " + ipRoute_elemList[x].getAlias(), e);
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
			for(int x = 0; x < ipRoute_elemList.length; x++)
			{
				SnmpObjectId   oid = new SnmpObjectId(ipRoute_elemList[x].getOid());
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
		Integer endindex = new Integer(ipRoute_elemList.length+1);
		String endoid = new String(TABLE_OID + "." + endindex.toString());
		SnmpObjectId oid = new SnmpObjectId(endoid);

	    	return oid;
	}
	
	/** 
	 * <P>Returns the number of entries in the MIB-II ipNetToMediaTable element list.</P>
	 */
	public static int getElementListSize()
	{
		return ipRoute_elemList.length;
	}

}   
