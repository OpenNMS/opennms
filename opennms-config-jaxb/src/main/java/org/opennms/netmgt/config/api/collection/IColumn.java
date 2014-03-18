package org.opennms.netmgt.config.api.collection;

import org.opennms.netmgt.snmp.SnmpObjId;

public interface IColumn {

    SnmpObjId getOid();
    String getAlias();
    String getType();
    String getDisplayHint();

}
