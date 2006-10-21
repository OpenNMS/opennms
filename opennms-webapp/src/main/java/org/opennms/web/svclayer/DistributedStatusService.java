package org.opennms.web.svclayer;

import java.util.Date;

import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly=true)
public interface DistributedStatusService {
    public SimpleWebTable createStatusTable(String locationName, String applicationLabel);
    
    public SimpleWebTable createFacilityStatusTable(Date startDate, Date endDate);
}
