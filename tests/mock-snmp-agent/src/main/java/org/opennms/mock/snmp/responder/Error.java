package org.opennms.mock.snmp.responder;

import org.snmp4j.smi.Variable;

/**
 * Throws an SNMP error with a status code matching
 * the requested instance.
 *
 * See org.snmp4j.mp.SnmpConstants.SNMP_ERROR_* for known error codes.
 *
 * @author jwhite
 */
public class Error implements DynamicVariable {

    @Override
    public Variable getVariableForOID(String oidStr) throws SnmpErrorStatusException {
        String[] oids = oidStr.split("\\.");
        Integer instance = Integer.parseInt(oids[oids.length-1]);
        throw new SnmpErrorStatusException(instance);
    }

}
