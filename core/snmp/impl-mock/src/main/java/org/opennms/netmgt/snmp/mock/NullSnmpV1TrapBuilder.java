package org.opennms.netmgt.snmp.mock;

import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpV1TrapBuilder;
import org.opennms.netmgt.snmp.SnmpValue;

public class NullSnmpV1TrapBuilder implements SnmpV1TrapBuilder {
    public void send(String destAddr, int destPort, String community) throws Exception { }
    public void sendTest(String destAddr, int destPort, String community) throws Exception { }
    public void addVarBind(SnmpObjId name, SnmpValue value) { }
    public void setEnterprise(SnmpObjId enterpriseId) { }
    public void setAgentAddress(InetAddress agentAddress) { }
    public void setGeneric(int generic) { }
    public void setSpecific(int specific) { }
    public void setTimeStamp(long timeStamp) { }
}
