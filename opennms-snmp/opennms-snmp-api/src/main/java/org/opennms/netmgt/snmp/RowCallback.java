package org.opennms.netmgt.snmp;


public interface RowCallback {

    public void rowCompleted(SnmpRowResult result);
}
