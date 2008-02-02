package org.opennms.netmgt.collectd;

import org.opennms.netmgt.snmp.SingleInstanceTracker;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;

public class ObjIdMonitor extends SingleInstanceTracker {
    SnmpValue value;

    public ObjIdMonitor(SnmpObjId base, SnmpInstId inst) {
        super(base, inst);
        value = null;
    }
    
    SnmpValue getValue() {
        return value;
    }
    
    int getIntValue() {
        return (value == null ? -1 : value.toInt());
    }
    
    long getLongValue() {
        return (value == null ? -1L : value.toLong());
    }

    @Override
    protected void storeResult(SnmpObjId base, SnmpInstId inst, SnmpValue val) {
        value = val;
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        
        buffer.append(getClass().getName());
        buffer.append("@");
        buffer.append(Integer.toHexString(hashCode()));
        buffer.append(": value: " + getValue());
        
        return buffer.toString();
    }

}
