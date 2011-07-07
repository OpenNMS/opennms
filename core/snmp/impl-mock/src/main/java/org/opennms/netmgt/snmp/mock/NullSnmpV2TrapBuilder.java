package org.opennms.netmgt.snmp.mock;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpV2TrapBuilder;
import org.opennms.netmgt.snmp.SnmpValue;

public class NullSnmpV2TrapBuilder implements SnmpV2TrapBuilder {
    public void send(String destAddr, int destPort, String community) throws Exception { }
    public void sendTest(String destAddr, int destPort, String community) throws Exception { }
    public void addVarBind(SnmpObjId name, SnmpValue value) { }
    public SnmpValue[] sendInform(String destAddr, int destPort, int timeout, int retries, String community) throws Exception {
        return new SnmpValue[0];
    }
}
