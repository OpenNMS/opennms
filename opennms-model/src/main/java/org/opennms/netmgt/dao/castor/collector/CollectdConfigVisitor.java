package org.opennms.netmgt.dao.castor.collector;

import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.Collector;

public interface CollectdConfigVisitor {

        public abstract void visitCollectdConfiguration(CollectdConfiguration collectdConfiguration);

        public abstract void completeCollectdConfiguration(CollectdConfiguration collectdConfiguration);

        public abstract void visitCollectorCollection(Collector collector);

        public abstract void completeCollectorCollection(Collector collector);

}
