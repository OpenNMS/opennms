package org.opennms.netmgt.snmp.mock;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpTrapBuilder;
import org.opennms.netmgt.snmp.SnmpValue;

public class NullSnmpTrapBuilder implements SnmpTrapBuilder {
    public void send(String destAddr, int destPort, String community) throws Exception { }
    public void sendTest(String destAddr, int destPort, String community) throws Exception { }
    public void addVarBind(SnmpObjId name, SnmpValue value) { }
}
