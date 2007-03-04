package org.opennms.web.svclayer;

import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.web.svclayer.support.RtcNodeModel;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface RtcService {
    public RtcNodeModel getNodeList();
    public RtcNodeModel getNodeListForCriteria(OnmsCriteria serviceCriteria, OnmsCriteria outageCriteria);
    public OnmsCriteria createServiceCriteria();
    public OnmsCriteria createOutageCriteria();
}
