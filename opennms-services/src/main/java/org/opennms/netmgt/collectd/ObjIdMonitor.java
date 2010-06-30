package org.opennms.netmgt.collectd;

import org.opennms.netmgt.snmp.SingleInstanceTracker;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;

/**
 * <p>ObjIdMonitor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ObjIdMonitor extends SingleInstanceTracker {
    SnmpValue value;

    /**
     * <p>Constructor for ObjIdMonitor.</p>
     *
     * @param base a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @param inst a {@link org.opennms.netmgt.snmp.SnmpInstId} object.
     */
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

    /** {@inheritDoc} */
    @Override
    protected void storeResult(SnmpObjId base, SnmpInstId inst, SnmpValue val) {
        value = val;
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        
        buffer.append(getClass().getName());
        buffer.append("@");
        buffer.append(Integer.toHexString(hashCode()));
        buffer.append(": value: " + getValue());
        
        return buffer.toString();
    }

}
