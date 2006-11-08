package org.opennms.web.svclayer;

import org.opennms.web.svclayer.support.ChooseResourceModel;

public interface ChooseResourceService {
    public ChooseResourceModel findChildResources(String resourceType,
            String resource, String endUrl);
}
