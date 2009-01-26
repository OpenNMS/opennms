package org.opennms.netmgt.snmp;

import java.util.List;

public interface RowCallback {

    public void rowCompleted(List<SnmpResult> results);
}
