/**
 * 
 */
package org.opennms.netmgt.collectd;

import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;

public final class SysUpTimeTracker extends ObjIdMonitor {

    SysUpTimeTracker() {
        super(SnmpObjId.get(SnmpCollector.NODE_SYSUPTIME), SnmpInstId.INST_ZERO);
    }
    
    boolean isChanged(long savedSysUpTime) {
        return (savedSysUpTime != -1) && (getLongValue() < savedSysUpTime);
    }
}