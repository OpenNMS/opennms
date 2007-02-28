package org.opennms.web.svclayer;

import org.opennms.web.svclayer.support.RtcNodeModel;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface RtcService {
    public RtcNodeModel getNodeList();
}
