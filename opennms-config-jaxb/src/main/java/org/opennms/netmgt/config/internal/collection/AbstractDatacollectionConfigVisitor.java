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

public class AbstractDatacollectionConfigVisitor implements DatacollectionConfigVisitor {

    @Override
    public void visitDatacollectionConfig(DatacollectionConfig config) {
    }

    @Override
    public void visitDatacollectionConfigComplete() {
    }

    @Override
    public void visitSnmpCollection(SnmpCollection collection) {
    }

    @Override
    public void visitSnmpCollectionComplete() {
    }

    @Override
    public void visitIncludeCollection(IncludeCollection includeCollection) {
    }

    @Override
    public void visitIncludeCollectionComplete() {
    }

    @Override
    public void visitGroup(Group group) {
    }

    @Override
    public void visitGroupComplete() {
    }

    @Override
    public void visitMibObj(MibObj mibObj) {
    }

    @Override
    public void visitMibObjComplete() {
    }

    @Override
    public void visitSystemDef(SystemDef systemDef) {
    }

    @Override
    public void visitSystemDefComplete() {
    }

    @Override
    public void visitIpList(IpList ipList) {
    }

    @Override
    public void visitIpListComplete() {
    }

    @Override
    public void visitCollect(Collect collect) {
    }

    @Override
    public void visitCollectComplete() {
    }

    @Override
    public void visitResourceType(ResourceType resourceType) {
    }

    @Override
    public void visitResourceTypeComplete() {
    }

}
