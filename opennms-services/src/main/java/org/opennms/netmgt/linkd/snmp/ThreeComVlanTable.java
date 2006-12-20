package org.opennms.netmgt.linkd.snmp;

import java.net.InetAddress;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.snmp.SnmpTable;
import org.opennms.netmgt.capsd.snmp.SnmpTableEntry;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;

/**
 * <P>
 * ThreeComVlanTable uses a SnmpSession to collect 3Com specific vendor vtp Vlan Table
 * entries. It implements the SnmpHandler to receive notifications when a reply
 * is received/error occurs in the SnmpSession used to send requests /recieve
 * replies.
 * </P>
 * 
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio Russo </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213 </A>
 */
public class ThreeComVlanTable extends SnmpTable {

	public ThreeComVlanTable(InetAddress address) {
        super(address, "threeComVlanTable", ThreeComVlanTableEntry.threeComVlan_elemList);
    }
    
    protected SnmpTableEntry createTableEntry(SnmpObjId base, SnmpInstId inst, Object val) {
        return new ThreeComVlanTableEntry();
    }


    protected final Category log() {
        return ThreadCategory.getInstance(ThreeComVlanTable.class);
    }
}

