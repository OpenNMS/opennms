package org.opennms.netmgt.linkd.snmp;

import java.net.InetAddress;

import org.opennms.netmgt.capsd.snmp.SnmpTable;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;

public class LldpLocTable extends SnmpTable<LldpLocTableEntry>{
    
    public LldpLocTable(InetAddress address) {
        super(address, "lldplocTable",LldpLocTableEntry.lldploctable_elemList);
    }
    
    /** {@inheritDoc} */
    protected LldpLocTableEntry createTableEntry(SnmpObjId base, SnmpInstId inst, Object val) {
        return new LldpLocTableEntry();
    }


}
