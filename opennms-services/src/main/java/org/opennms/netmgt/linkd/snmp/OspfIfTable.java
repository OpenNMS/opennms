package org.opennms.netmgt.linkd.snmp;

import java.net.InetAddress;

import org.opennms.netmgt.capsd.snmp.SnmpTable;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;

public class OspfIfTable extends SnmpTable<OspfIfTableEntry>{
    
    public OspfIfTable(InetAddress address) {
        super(address, "ospfIfTable",OspfIfTableEntry.ospfiftable_elemList);
    }
    
    /** {@inheritDoc} */
    protected OspfIfTableEntry createTableEntry(SnmpObjId base, SnmpInstId inst, Object val) {
        return new OspfIfTableEntry();
    }


}
