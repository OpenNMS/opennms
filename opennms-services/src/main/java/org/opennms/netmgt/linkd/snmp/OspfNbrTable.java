package org.opennms.netmgt.linkd.snmp;

import java.net.InetAddress;

import org.opennms.netmgt.capsd.snmp.SnmpTable;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;

public class OspfNbrTable extends SnmpTable<OspfNbrTableEntry>{
    
    public OspfNbrTable(InetAddress address) {
        super(address, "ospfNbrTable",OspfNbrTableEntry.ospfnbrtable_elemList);
    }
    
    /** {@inheritDoc} */
    protected OspfNbrTableEntry createTableEntry(SnmpObjId base, SnmpInstId inst, Object val) {
        return new OspfNbrTableEntry();
    }


}
