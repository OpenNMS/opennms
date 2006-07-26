package org.opennms.web.svclayer;

import java.util.Collection;

import org.opennms.netmgt.model.OnmsOutage;

import java.sql.SQLException;
import java.util.Date;

public interface OutageService {

    Collection<OnmsOutage> getCurrentOutages();
    
    Collection<OnmsOutage> getCurrenOutagesByRange(Integer Offset, Integer Limit);

    Collection<OnmsOutage> getSuppressedOutages();

    Integer getCurrentOutageCount() ;

    Integer getSuppressedOutageCount() ;

    Collection<OnmsOutage> getCurrentOutagesForNode(int nodeId)
            ;

    Collection<OnmsOutage> getNonCurrentOutagesForNode(int nodeId)
            ;

    Collection<OnmsOutage> getOutagesForNode(int nodeId) ;

    Collection<OnmsOutage> getOutagesForNode(int nodeId, Date time)
            ;

    Collection<OnmsOutage> getOutagesForInterface(int nodeId, String ipInterface)
            ;

    Collection<OnmsOutage> getOutagesForInterface(int nodeId, String ipAddr,
            Date time) ;

    Collection<OnmsOutage> getOutagesForService(int nodeId, String ipInterface,
            int serviceId) ;
    
    Collection<OnmsOutage>  getOutagesForService(int nodeId, String ipAddr, int serviceId,
            Date time) ;

    // This we may have to define 
    /*
    OutageSummary[] getCurrentOutageSummaries() ;

    OutageSummary[] getCurrentSDSOutageSummaries() ;
    */
}
