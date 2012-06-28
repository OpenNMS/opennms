package org.opennms.mock.snmp.responder;

import org.snmp4j.smi.Variable;

public interface DynamicVariable {
	
	public Variable getVariableForOID(String oidStr);
	
}
