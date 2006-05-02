package org.opennms.netmgt.dao.castor.collector;

import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.SnmpCollection;

public interface DataCollectionVisitor {

        public abstract void visitDataCollectionConfig(DatacollectionConfig dataCollectionConfig);

        public abstract void completeDataCollectionConfig(DatacollectionConfig dataCollectionConfig);

        public abstract void visitSnmpCollection(SnmpCollection snmpCollection);

        public abstract void completeSnmpCollection(SnmpCollection snmpCollection);

}
