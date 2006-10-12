package org.opennms.web.svclayer;

import java.util.Date;

public interface DistributedStatusService {
    public SimpleWebTable createStatusTable(String locationName, String applicationLabel);
    
    public SimpleWebTable createFacilityStatusTable(Date startDate, Date endDate);
}
