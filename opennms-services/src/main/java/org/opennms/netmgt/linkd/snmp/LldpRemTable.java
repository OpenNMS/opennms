package org.opennms.netmgt.linkd.snmp;

import java.net.InetAddress;

import org.opennms.netmgt.capsd.snmp.SnmpTable;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;

public class LldpRemTable extends SnmpTable<LldpRemTableEntry>{
    
    public LldpRemTable(InetAddress address) {
        super(address, "lldpRemTable",LldpRemTableEntry.lldpremtable_elemList);
    }
    
    /** {@inheritDoc} */
    protected LldpRemTableEntry createTableEntry(SnmpObjId base, SnmpInstId inst, Object val) {
        return new LldpRemTableEntry();
    }


}
