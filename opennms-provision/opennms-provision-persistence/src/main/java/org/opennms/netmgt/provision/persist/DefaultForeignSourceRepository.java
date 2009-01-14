package org.opennms.netmgt.provision.persist;

import org.springframework.core.io.Resource;

public class DefaultForeignSourceRepository extends AbstractForeignSourceRepository {

    public OnmsRequisition createRequisition(Resource resource) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public OnmsForeignSource get(String foreignSourceName) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public OnmsRequisition getRequisition(String foreignSourceName) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public OnmsRequisition getRequisition(OnmsForeignSource foreignSource) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void save(OnmsForeignSource foreignSource) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void save(OnmsRequisition requisition) {
        throw new UnsupportedOperationException("not yet implemented");
    }

}
