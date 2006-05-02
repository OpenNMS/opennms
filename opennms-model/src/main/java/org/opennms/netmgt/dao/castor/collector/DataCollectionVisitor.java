package org.opennms.netmgt.dao.castor.collector;

import org.opennms.netmgt.config.datacollection.DatacollectionConfig;

public interface DataCollectionVisitor {

        public abstract void visitDataCollectionConfig(DatacollectionConfig dataCollectionConfig);

        public abstract void completeDataCollectionConfig(DatacollectionConfig dataCollectionConfig);

}
