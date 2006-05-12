package org.opennms.netmgt.collectd;

import java.util.Collection;

import org.opennms.netmgt.snmp.SnmpInstId;

public abstract class DbResourceType extends ResourceType {

    public DbResourceType(CollectionAgent agent, OnmsSnmpCollection snmpCollection) {
        super(agent, snmpCollection);
    }
    public abstract CollectionResource findResource(SnmpInstId inst);
    public abstract int getType();
    
    public Collection getAttributeTypes() {
        return getCollection().getAttributeTypes(getAgent(), getType());
    }
    

}
