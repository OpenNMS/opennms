package org.opennms.web.svclayer;

import java.util.Date;

import org.opennms.web.svclayer.support.DistributedStatusHistoryModel;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly=true)
public interface DistributedStatusService {
    public SimpleWebTable createStatusTable(String locationName, String applicationLabel);
    
    public SimpleWebTable createFacilityStatusTable(Date startDate, Date endDate);

    public DistributedStatusHistoryModel createHistoryModel(String locationName,
            String monitorId, String applicationName, String timeSpan,
            String previousLocation);
}
