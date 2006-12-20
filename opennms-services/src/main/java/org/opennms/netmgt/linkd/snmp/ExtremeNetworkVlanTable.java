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
 * ExtremeNetworkVlanTable uses a SnmpSession to collect Extreme Network devices specific Vlan Table
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
public class ExtremeNetworkVlanTable extends SnmpTable {

	public ExtremeNetworkVlanTable(InetAddress address) {
        super(address, "ExtremeNetworkVlanTable", ExtremeNetworkVlanTableEntry.enVlan_elemList);
    }
    
    protected SnmpTableEntry createTableEntry(SnmpObjId base, SnmpInstId inst, Object val) {
        return new ExtremeNetworkVlanTableEntry();
    }


    protected final Category log() {
        return ThreadCategory.getInstance(ExtremeNetworkVlanTable.class);
    }
}

