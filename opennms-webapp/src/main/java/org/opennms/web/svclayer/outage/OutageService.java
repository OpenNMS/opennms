package org.opennms.web.svclayer.outage;

import java.util.Collection;
import java.util.Date;

import org.opennms.netmgt.model.OnmsOutage;

public interface OutageService {

    Collection<OnmsOutage> getCurrentOutages();
    
    Collection<OnmsOutage> getCurrentOutagesByRange(Integer Offset, Integer Limit, String Order, String Direction);

    Collection<OnmsOutage> getOutagesByRange(Integer Offset, Integer Limit, String Order, String Direction, String Filter);
    
    Collection<OnmsOutage> getSuppressedOutagesByRange(Integer Offset, Integer Limit, String Order, String Direction);    
    
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

	Collection<OnmsOutage> getCurrentOutages(String ordering);

	OnmsOutage load(Integer outageid);
	
	void update(OnmsOutage Outage);
	
	Integer getOutageCount() ;
	
	Integer outageCountFiltered(String Filter);

	Collection<OnmsOutage> getResolvedOutagesByRange(Integer Offset, Integer Limit, String Order, String Direction, String Filter);

	Integer outageResolvedCountFiltered(String searchFilter);
	
    // This we may have to define 
    /*
    OutageSummary[] getCurrentOutageSummaries() ;

    OutageSummary[] getCurrentSDSOutageSummaries() ;
    */
    
}
