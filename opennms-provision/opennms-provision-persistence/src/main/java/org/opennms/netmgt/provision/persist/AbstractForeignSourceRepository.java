package org.opennms.netmgt.provision.persist;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public class AbstractForeignSourceRepository implements
        ForeignSourceRepository {

    public OnmsRequisition createRequisition(Resource resource) {
        Assert.notNull(resource);
        OnmsRequisition r = new OnmsRequisition();
        r.loadResource(resource);
        return r;
    }

    public OnmsForeignSource get(String foreignSourceName) {
        return null;
    }

    public OnmsRequisition getRequisition(String foreignSourceName) {
        return null;
    }

    public OnmsRequisition getRequisition(OnmsForeignSource foreignSource) {
        return null;
    }

    public void save(OnmsForeignSource foreignSource) {
    }

    public void save(OnmsRequisition requisition) {
    }

}
