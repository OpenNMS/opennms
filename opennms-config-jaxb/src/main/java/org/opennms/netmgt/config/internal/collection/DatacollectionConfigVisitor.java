package org.opennms.netmgt.config.internal.collection;

import org.opennms.netmgt.config.datacollection.Collect;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.IncludeCollection;
import org.opennms.netmgt.config.datacollection.IpList;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.config.datacollection.SystemDef;

public interface DatacollectionConfigVisitor {

    void visitDatacollectionConfig(DatacollectionConfig config);
    void visitDatacollectionConfigComplete();

    void visitSnmpCollection(SnmpCollection collection);
    void visitSnmpCollectionComplete();

    void visitIncludeCollection(IncludeCollection includeCollection);
    void visitIncludeCollectionComplete();
    
    void visitGroup(Group group);
    void visitGroupComplete();
    
    void visitMibObj(MibObj mibObj);
    void visitMibObjComplete();
    
    void visitSystemDef(SystemDef systemDef);
    void visitSystemDefComplete();
    
    void visitIpList(IpList ipList);
    void visitIpListComplete();
    
    void visitCollect(Collect collect);
    void visitCollectComplete();
    
    void visitResourceType(ResourceType resourceType);
    void visitResourceTypeComplete();
}
