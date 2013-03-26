/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.linkd.snmp;

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
 * @see IpRouteTable
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 * @version $Id: $
 */
public final class IpRouteTableEntry extends IpRouteCollectorEntry
{	
	
	/**
	 * <P>The keys that will be supported by default from the 
	 * TreeMap base class. Each of the elements in the list
	 * are an instance of the IpRoutetable. Objects
	 * in this list should be used by multiple instances of
	 * this class.</P>
	 */
	public static NamedSnmpVar[]	ms_elemList = new NamedSnmpVar[] {
	    /** The destination IP address of this route. An
	     * entry with a value of 0.0.0.0 is considered a
	     * default route. Multiple routes to a single
	     * destination can appear in the table, but access to
	     * such multiple entries is dependent on the table-
	     * access mechanisms defined by the network
	     * management protocol in use.
	     */
	    new NamedSnmpVar(NamedSnmpVar.SNMPIPADDRESS,   IP_ROUTE_DEST,      ".1.3.6.1.2.1.4.21.1.1",  1),

	    /**
	     * The index value which uniquely identifies the
	     * local interface through which the next hop of this
	     * route should be reached. The interface identified
	     * by a particular value of this index is the same
	     * interface as identified by the same value of
	     * ifIndex.
	     */
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       IP_ROUTE_IFINDEX,   ".1.3.6.1.2.1.4.21.1.2",  2),
	    
	    /**
	     * The primary routing metric for this route. The
	     * semantics of this metric are determined by the
	     * routing-protocol specified in the route's
	     * ipRouteProto value. If this metric is not used,
	     * its value should be set to -1.
	     */
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       IP_ROUTE_METRIC1,   ".1.3.6.1.2.1.4.21.1.3",  3),

	    /**
	     * An alternate routing metric for this route. The
	     * semantics of this metric are determined by the
	     * routing-protocol specified in the route's
	     * ipRouteProto value. If this metric is not used,
	     * its value should be set to -1.
	     */
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       IP_ROUTE_METRIC2,   ".1.3.6.1.2.1.4.21.1.4",  4),

        /**
         * An alternate routing metric for this route. The
         * semantics of this metric are determined by the
         * routing-protocol specified in the route's
         * ipRouteProto value. If this metric is not used,
         * its value should be set to -1.
         */
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       IP_ROUTE_METRIC3,   ".1.3.6.1.2.1.4.21.1.5",  5),

        /**
         * An alternate routing metric for this route. The
         * semantics of this metric are determined by the
         * routing-protocol specified in the route's
         * ipRouteProto value. If this metric is not used,
         * its value should be set to -1.
         */
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       IP_ROUTE_METRIC4,   ".1.3.6.1.2.1.4.21.1.6",  6),

        /**
         * The IP address of the next hop of this route.
         * (In the case of a route bound to an interface
         * which is realized via a broadcast media, the value
         * of this field is the agent's IP address on that
         * interface.)
         */
	    new NamedSnmpVar(NamedSnmpVar.SNMPIPADDRESS,   IP_ROUTE_NXTHOP,    ".1.3.6.1.2.1.4.21.1.7",  7),

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
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       IP_ROUTE_TYPE,      ".1.3.6.1.2.1.4.21.1.8",  8),

        /**
         * The routing mechanism via which this route was
         * learned. Inclusion of values for gateway routing
         * protocols is not intended to imply that hosts
         * should support those protocols.
         */
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       IP_ROUTE_PROTO,     ".1.3.6.1.2.1.4.21.1.9",  9),

        /**
         * The number of seconds since this route was last
         * updated or otherwise determined to be correct.
         * Note that no semantics of `too old' can be implied
         * except through knowledge of the routing protocol
         * by which the route was learned.
         */
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       IP_ROUTE_AGE,       ".1.3.6.1.2.1.4.21.1.10",  10),

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
	    new NamedSnmpVar(NamedSnmpVar.SNMPIPADDRESS,   IP_ROUTE_MASK,      ".1.3.6.1.2.1.4.21.1.11",  11),

        /**
         * An alternate routing metric for this route. The
         * semantics of this metric are determined by the
         * routing-protocol specified in the route's
         * ipRouteProto value. If this metric is not used,
         * its value should be set to -1.
         */
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       IP_ROUTE_METRIC5,   ".1.3.6.1.2.1.4.21.1.12",  12),

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
	    new NamedSnmpVar(NamedSnmpVar.SNMPOBJECTID,        IP_ROUTE_INFO,      ".1.3.6.1.2.1.4.21.1.13",  13)
	};
	
		
	/**
	 * <P>The TABLE_OID is the object identifier that represents
	 * the root of the IP ROUTE table in the MIB forest.</P>
	 */
	public static final String	TABLE_OID	= ".1.3.6.1.2.1.4.21.1";	// start of table (GETNEXT)

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
	public IpRouteTableEntry() {
		super(ms_elemList);
	}
	
}   
