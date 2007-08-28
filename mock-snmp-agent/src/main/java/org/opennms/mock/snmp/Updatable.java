package org.opennms.mock.snmp;

import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;

public interface Updatable {
    public void updateValue(OID oid, Variable value);

}
