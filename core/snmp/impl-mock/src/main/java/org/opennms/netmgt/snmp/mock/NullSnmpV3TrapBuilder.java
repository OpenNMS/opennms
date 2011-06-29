package org.opennms.netmgt.snmp.mock;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpV3TrapBuilder;
import org.opennms.netmgt.snmp.SnmpValue;

public class NullSnmpV3TrapBuilder implements SnmpV3TrapBuilder {
    public SnmpValue[] sendInform(String destAddr, int destPort, int timeout, int retries, String community) throws Exception {
        return new SnmpValue[0];
    }
    public void send(String destAddr, int destPort, String community) throws Exception { }
    public void sendTest(String destAddr, int destPort, String community) throws Exception { }
    public void addVarBind(SnmpObjId name, SnmpValue value) { }
    public void send(String destAddr, int destPort, int securityLevel, String securityname, String authPassPhrase, String authProtocol, String privPassPhrase, String privprotocol) throws Exception { }
    public SnmpValue[] sendInform(String destAddr, int destPort, int timeout, int retries, int securityLevel, String securityname, String authPassPhrase, String authProtocol, String privPassPhrase, String privprotocol) throws Exception {
        return new SnmpValue[0];
    }
}
