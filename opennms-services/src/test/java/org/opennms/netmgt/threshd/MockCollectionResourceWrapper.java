package org.opennms.netmgt.threshd;

import java.io.File;

import org.opennms.netmgt.collectd.CollectionResource;
import org.opennms.netmgt.collectd.CollectionSetVisitor;
import org.opennms.netmgt.collectd.ServiceParameters;
import org.opennms.netmgt.model.RrdRepository;

public class MockCollectionResourceWrapper extends CollectionResourceWrapper {

    public MockCollectionResourceWrapper(final String instance) {
        super(0, 0, null, null, null, new CollectionResource() {
            public String getInstance() {
                return instance;
            }
            public String getLabel() {
                return null;
            }
            public String getResourceTypeName() {
                return null;
            }
            public int getType() {
                return 0;
            }
            public boolean rescanNeeded() {
                return false;
            }
            public boolean shouldPersist(ServiceParameters params) {
                return false;
            }
            public void visit(CollectionSetVisitor visitor) {
            }
            public String getOwnerName() {
                return null;
            }
            public File getResourceDir(RrdRepository repository) {
                return null;
            }
        }, null);
    }

}
