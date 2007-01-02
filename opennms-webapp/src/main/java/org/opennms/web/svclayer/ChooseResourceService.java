package org.opennms.web.svclayer;

import org.opennms.web.svclayer.support.ChooseResourceModel;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface ChooseResourceService {
    public ChooseResourceModel findChildResources(String resourceId, String endUrl);
}
