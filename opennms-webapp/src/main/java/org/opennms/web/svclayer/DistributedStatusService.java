package org.opennms.web.svclayer;

public interface DistributedStatusService {
//  public List<OnmsLocationSpecificStatus> findLocationSpecificStatus(OnmsMonitoringLocationDefinition location, OnmsApplication application);
//    public List<OnmsLocationSpecificStatus> findLocationSpecificStatus(String locationName, String applicationName);
    public SimpleWebTable createStatusTable(String locationName, String applicationLabel);
    
    public SimpleWebTable createFacilityStatusTable();
}
