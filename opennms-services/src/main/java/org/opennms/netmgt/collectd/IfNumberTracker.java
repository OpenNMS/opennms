/**
 * 
 */
package org.opennms.netmgt.collectd;

import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;

public final class IfNumberTracker extends ObjIdMonitor {

    IfNumberTracker() {
        super(SnmpObjId.get(SnmpCollector.INTERFACES_IFNUMBER), SnmpInstId.INST_ZERO);
    }

    boolean isChanged(int savedIfCount) {
        return (savedIfCount != -1) && (getIntValue() != savedIfCount);
    }

}