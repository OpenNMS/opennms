package org.opennms.netmgt.linkd.snmp;

import java.net.InetAddress;

import org.opennms.netmgt.capsd.snmp.NamedSnmpVar;

/**
 *<p>The {@link InetCidrRouteTableEntry} class is designed to hold all the MIB-II
 * information for one entry in the ipRouteTable. The table effectively
 * contains a list of these entries, each entry having information
 * about IP route. The entry contains:</p>
 * <ul>
 * <li>ipRouteDest</li>
 * <li>ipRouteIfIndex</li>
 * <li>ipRouteMetric1</li>
 * <li>ipRouteMetric2</li>
 * <li>ipRouteMetric3</li>
 * <li>ipRouteMetric4</li>
 * <li>ipRouteNextHop</li>
 * <li>ipRouteType</li>
 * <li>ipRouteProto</li>
 * <li>ipRouteAge</li>
 * <li>ipRouteMask</li>
 * <li>ipRouteMetric5</li>
 * <li><s>ipRouteInfo</s> (not available in the inetCidr table)</li>
 * </ul>
 *
 * <p>This object is used by the {@link InetCidrRouteTable} to hold information
 * single entries in the table. See the {@link InetCidrRouteTable} documentation
 * form more information.</p>
 *
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio</A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya</A>
 * @author <A HREF="mailto:weave@oculan.com">Weave</A>
 * @author <A>Jon Whetzel</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 *
 * @see InetCidrRouteTable
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 */

public final class InetCidrRouteTableEntry extends IpRouteCollectorEntry
{
   // Lookup strings for specific table entries
   //
   public final static String  IP_ROUTE_PFX_LEN    = "ipRoutePfxLen";   
   
   /**
    * <P>The keys that will be supported by default from the 
    * TreeMap base class. Each of the elements in the list
    * are an instance of the InetCidrRoutetable. Objects
    * in this list should be used by multiple instances of
    * this class.</P>
    */
   public static NamedSnmpVar[]    ms_elemList = null;
   
   /**
    * <P>Initialize the element list for the class. This
    * is class wide data, but will be used by each instance.</P>
    */
   static
   {
       ms_elemList = new NamedSnmpVar[12];
       int ndx = 0;
       
   /** The destination IP address of this route. An
    * entry with a value of 0.0.0.0 is considered a
    * default route. Multiple routes to a single
    * destination can appear in the table, but access to
    * such multiple entries is dependent on the table-
    * access mechanisms defined by the network
    * management protocol in use.
    */
       ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPIPADDRESS,   IP_ROUTE_DEST,      ".1.3.6.1.2.1.4.24.7.1.2",  1);
   
   /**
    * The index value which uniquely identifies the
    * local interface through which the next hop of this
    * route should be reached. The interface identified
    * by a particular value of this index is the same
    * interface as identified by the same value of
    * ifIndex.
    */
       ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       IP_ROUTE_IFINDEX,   ".1.3.6.1.2.1.4.24.7.1.7",  2);
       
   /**
    * The primary routing metric for this route. The
    * semantics of this metric are determined by the
    * routing-protocol specified in the route's
    * ipRouteProto value. If this metric is not used,
    * its value should be set to -1.
    */
       ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       IP_ROUTE_METRIC1,   ".1.3.6.1.2.1.4.24.7.1.12",  3);

   /**
    * An alternate routing metric for this route. The
    * semantics of this metric are determined by the
    * routing-protocol specified in the route's
    * ipRouteProto value. If this metric is not used,
    * its value should be set to -1.
    */
       ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       IP_ROUTE_METRIC2,   ".1.3.6.1.2.1.4.24.7.1.13",  4);

       /**
        * An alternate routing metric for this route. The
        * semantics of this metric are determined by the
        * routing-protocol specified in the route's
        * ipRouteProto value. If this metric is not used,
        * its value should be set to -1.
        */

       ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       IP_ROUTE_METRIC3,   ".1.3.6.1.2.1.4.24.7.1.14",  5);

       /**
        * An alternate routing metric for this route. The
        * semantics of this metric are determined by the
        * routing-protocol specified in the route's
        * ipRouteProto value. If this metric is not used,
        * its value should be set to -1.
        */
       ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       IP_ROUTE_METRIC4,   ".1.3.6.1.2.1.4.24.7.1.15",  6);

       /**
        * The IP address of the next hop of this route.
        * (In the case of a route bound to an interface
        * which is realized via a broadcast media, the value
        * of this field is the agent's IP address on that
        * interface.)
        */
       ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPIPADDRESS,   IP_ROUTE_NXTHOP,    ".1.3.6.1.2.1.4.24.7.1.6",  7);
       
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
       ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       IP_ROUTE_TYPE,      ".1.3.6.1.2.1.4.24.7.1.8",  8);

       /**
        * The routing mechanism via which this route was
        * learned. Inclusion of values for gateway routing
        * protocols is not intended to imply that hosts
        * should support those protocols.
        */
       ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       IP_ROUTE_PROTO,     ".1.3.6.1.2.1.4.24.7.1.9",  9);
       
       /**
        * The number of seconds since this route was last
        * updated or otherwise determined to be correct.
        * Note that no semantics of `too old' can be implied
        * except through knowledge of the routing protocol
        * by which the route was learned.
        */
       ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       IP_ROUTE_AGE,       ".1.3.6.1.2.1.4.24.7.1.10",  10);
       
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
//
// AlanE : There is no ROUTE_MASK in ipV6 (inetCidr) instead we should use Prefix length
// I've changed the getRouteMask function to convert the /prefix into a netmask
//
       ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,   IP_ROUTE_PFX_LEN,   ".1.3.6.1.2.1.4.24.7.1.3",  11);

       /**
        * An alternate routing metric for this route. The
        * semantics of this metric are determined by the
        * routing-protocol specified in the route's
        * ipRouteProto value. If this metric is not used,
        * its value should be set to -1.
        */
       ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       IP_ROUTE_METRIC5,   ".1.3.6.1.2.1.4.24.7.1.16",  12);

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
//
// AlanE: It doesn't look like there is an equivalent of ipRouteInfo in the inetCidr table
// Just comment out for now
//

//     ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOBJECTID,        IP_ROUTE_INFO,      ".1.3.6.1.2.1.4.24.7.1.9",  13);
   }
   

   /**
    * <P>The TABLE_OID is the object identifier that represents
    * the root of the IP ROUTE table in the MIB forest.</P>
    */
   public static final String  TABLE_OID   = ".1.3.6.1.2.1.4.24.7.1";  // start of table (GETNEXT)

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
   public InetCidrRouteTableEntry( )
   {
       super(ms_elemList);
   }
   
    public InetAddress getInetCidrRouteDest() {
        return getIPAddress(InetCidrRouteTableEntry.IP_ROUTE_DEST); 
    }

   public int getInetCidrRouteIfIndex() {
       Integer val = getInt32(InetCidrRouteTableEntry.IP_ROUTE_IFINDEX);
       if (val == null ) return -1;
       return val;
   }
   
   public int getInetCidrRouteMetric1() {
       Integer val = getInt32(InetCidrRouteTableEntry.IP_ROUTE_METRIC1);
       if (val == null ) return -1;
       return val;
   }

   public int getInetCidrRouteMetric2() {
       Integer val = getInt32(InetCidrRouteTableEntry.IP_ROUTE_METRIC2);
       if (val == null ) return -1;
       return val;
       
   }

   public int getInetCidrRouteMetric3() {
       Integer val = getInt32(InetCidrRouteTableEntry.IP_ROUTE_METRIC3);
       if (val == null ) return -1;
       return val;

   }

   public int getInetCidrRouteMetric4() {
       Integer val = getInt32(InetCidrRouteTableEntry.IP_ROUTE_METRIC4);
       if (val == null ) return -1;
       return val;

   }

   public InetAddress getInetCidrRouteNextHop() {
       return getIPAddress(InetCidrRouteTableEntry.IP_ROUTE_NXTHOP);
   }

   public int getInetCidrRouteType() {
       Integer val = getInt32(InetCidrRouteTableEntry.IP_ROUTE_TYPE);
       if (val == null ) return -1;
       return val;
       
   }

   public int getInetCidrRouteProto() {
       Integer val = getInt32(InetCidrRouteTableEntry.IP_ROUTE_PROTO);
       if (val == null ) return -1;
       return val;
       
   }

   public int getInetCidrRouteAge() {
       Integer val = getInt32(InetCidrRouteTableEntry.IP_ROUTE_AGE);
       if (val == null ) return -1;
       return val;
       
   }

   public InetAddress getInetCidrRouteMask() throws java.net.UnknownHostException {

           Integer prefix = getInt32(InetCidrRouteTableEntry.IP_ROUTE_PFX_LEN);
           Integer mask = 0xffffffff << (32 - prefix);
           Integer value = mask;
           byte[] bytes = new byte[]{ 
                   (byte)(value >>> 24), (byte)(value >> 16 & 0xff), (byte)(value >> 8 & 0xff), (byte)(value & 0xff) };
           InetAddress netAddr = InetAddress.getByAddress(bytes);
       return netAddr;
   }

   public int getInetCidrRouteMetric5() {
       Integer val = getInt32(InetCidrRouteTableEntry.IP_ROUTE_METRIC5);
       if (val == null ) return -1;
       return val;

   }

   public String getInetCidrRouteInfo() {
       return getObjectID(InetCidrRouteTableEntry.IP_ROUTE_INFO);
   }
}
