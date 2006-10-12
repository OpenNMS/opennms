package org.opennms.web.svclayer;

public interface DistributedStatusService {
    public SimpleWebTable createStatusTable(String locationName, String applicationLabel);
    
    public SimpleWebTable createFacilityStatusTable();
}
