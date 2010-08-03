package org.opennms.netmgt.linkd.snmp;

import java.net.InetAddress;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.snmp.SnmpTable;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;

/**
 * <P>IpCidrRouteTable uses a SnmpSession to collect the ipRouteTable entries
 * It implements the SnmpHandler to receive notifications when a reply is 
 * received/error occurs in the SnmpSession used to send requests /recieve 
 * replies.</P>
 *
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio Russo</A>
 *
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 */
public class IpCidrRouteTable extends SnmpTable<IpCidrRouteTableEntry>
{
   /**
    * <P>Constructs an InetCidrRouteTable object that is used to collect
    * the address elements from the remote agent. Once all
    * the elements are collected, or there is an error in
    * the collection the signaler object is <EM>notified</EM>
    * to inform other threads.</P>
    *
    * @param session   The session with the remote agent.
    * @param signaler  The object to notify waiters.
    *
    * @see InetCidrRouteTableEntry
    */
   public IpCidrRouteTable(InetAddress address)
   {
        super(address, "ipRouteTable", IpCidrRouteTableEntry.ms_elemList);
   }
   
    protected IpCidrRouteTableEntry createTableEntry(SnmpObjId base, SnmpInstId inst, Object val) {
        return new IpCidrRouteTableEntry();
    }

    protected final ThreadCategory log() {
        return ThreadCategory.getInstance(IpCidrRouteTable.class);
    }

}
