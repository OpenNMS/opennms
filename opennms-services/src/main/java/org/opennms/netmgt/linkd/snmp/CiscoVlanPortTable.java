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
 * CiscoVlanPortTable uses a SnmpSession to collect the vtpVlanTable Port table
 * entries. It implements the SnmpHandler to receive notifications when a reply
 * is received/error occurs in the SnmpSession used to send requests /recieve
 * replies.
 * </P>
 * 
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio Russo </A>
 * @author <A HREF="mailto:jamesz@opennms.org">James Zuo </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213 </A>
 */
public class CiscoVlanPortTable extends SnmpTable {

	/**
	 * <P>
	 * Constructs an CiscoVlanPortTable object that is used to collect the vlan
	 * elements from the remote agent. Once all the elements are collected, or
	 * there is an error in the collection the signaler object is <EM>notified
	 * </EM> to inform other threads.
	 * </P>
	 * 
	 * @param session
	 *            The session with the remote agent.
	 * @param signaler
	 *            The object to notify waiters.
	 * 
	 * @see CiscoVlanPortTableEntry
	 */
	public CiscoVlanPortTable(InetAddress address) {
        super(address, "ciscoVlanPortTable", CiscoVlanPortTableEntry.ciscoVlan_elemList);
    }
    
    protected SnmpTableEntry createTableEntry(SnmpObjId base, SnmpInstId inst, Object val) {
        return new CiscoVlanPortTableEntry();
    }


    protected final Category log() {
        return ThreadCategory.getInstance(CiscoVlanPortTable.class);
    }
}
