package org.opennms.netmgt.dao.castor.collector;

import org.opennms.netmgt.config.datacollection.Collect;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.IpList;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.Rrd;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.config.datacollection.SystemDef;

public interface DataCollectionVisitor {

        public abstract void visitDataCollectionConfig(DatacollectionConfig dataCollectionConfig);

        public abstract void completeDataCollectionConfig(DatacollectionConfig dataCollectionConfig);

        public abstract void visitSnmpCollection(SnmpCollection snmpCollection);

        public abstract void completeSnmpCollection(SnmpCollection snmpCollection);

        public abstract void visitRrd(Rrd rrd);

        public abstract void completeRrd(Rrd rrd);

        public abstract void visitRra(String rra);

        public abstract void completeRra(String rra);

        public abstract void visitSystemDef(SystemDef systemDef);

        public abstract void completeSystemDef(SystemDef systemDef);

        public abstract void visitSysOid(String sysoid);

        public abstract void completeSysOid(String sysoid);

        public abstract void visitSysOidMask(String sysoidMask);

        public abstract void completeSysOidMask(String sysoidMask);

        public abstract void visitIpList(IpList ipList);

        public abstract void completeIpList(IpList ipList);

        public abstract void visitCollect(Collect collect);

        public abstract void completeCollect(Collect collect);

        public abstract void visitIncludeGroup(String includeGroup);

        public abstract void completeIncludeGroup(String includeGroup);

        public abstract void visitGroup(Group group);

        public abstract void completeGroup(Group group);

        public abstract void visitSubGroup(String subGroup);

        public abstract void completeSubGroup(String subGroup);

        public abstract void visitMibObj(MibObj mibObj);

        public abstract void completeMibObj(MibObj mibObj);

        
}
