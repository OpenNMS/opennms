package org.opennms.netmgt.config.api.collection;

import org.opennms.netmgt.snmp.SnmpObjId;

public interface IMibObject {

    SnmpObjId getOid();
    String getAlias();
    String getType();
    String getInstance();
    IGroup getGroup();

}
