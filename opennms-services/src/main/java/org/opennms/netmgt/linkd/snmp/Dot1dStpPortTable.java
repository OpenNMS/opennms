package org.opennms.netmgt.linkd.snmp;

import java.net.InetAddress;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.snmp.SnmpTable;
import org.opennms.netmgt.capsd.snmp.SnmpTableEntry;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;

/**
 * <P>Dot1dStpPortTable uses a SnmpSession to collect the dot1dBridge.dot1dStp 
 * tableentries.
 * It implements the SnmpHandler to receive notifications when a reply is 
 * received/error occurs in the SnmpSession used to send requests /recieve 
 * replies.</P>
 *
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio Russo</A>
 * @author <A HREF="mailto:jamesz@opennms.org">James Zuo</A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya</A>
 * @author <A HREF="mailto:weave@oculan.com">Weave</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 */
public class Dot1dStpPortTable extends SnmpTable {

	public Dot1dStpPortTable(InetAddress address) {
        super(address, "dot1dStpPortTable", Dot1dStpPortTableEntry.stpport_elemList);
    }
    
    protected SnmpTableEntry createTableEntry(SnmpObjId base, SnmpInstId inst, Object val) {
        return new Dot1dStpPortTableEntry();
    }


    protected final Category log() {
        return ThreadCategory.getInstance(Dot1dStpPortTable.class);
    }
	
}
				



