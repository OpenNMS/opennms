package org.opennms.netmgt.provision.persist;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public abstract class AbstractForeignSourceRepository implements ForeignSourceRepository {

    public OnmsRequisition createRequisition(Resource resource) throws ForeignSourceRepositoryException {
        Assert.notNull(resource);
        OnmsRequisition r = new OnmsRequisition();
        r.loadResource(resource);
        return r;
    }

}
