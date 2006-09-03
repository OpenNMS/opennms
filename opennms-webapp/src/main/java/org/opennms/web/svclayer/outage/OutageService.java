package org.opennms.web.svclayer.outage;

import java.util.Collection;
import java.util.Date;

import org.opennms.netmgt.model.OnmsOutage;

public interface OutageService {

    Collection<OnmsOutage> getCurrentOutages();
    
    Collection<OnmsOutage> getCurrentOutagesByRange(Integer offset, Integer oimit, String orderProperty, String direction);

    Collection<OnmsOutage> getOutagesByRange(Integer offset, Integer limit, String orderProperty, String direction, String filter);
    
    Collection<OnmsOutage> getSuppressedOutagesByRange(Integer offset, Integer limit, String orderProperty, String direction);    
    
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

    Collection<OnmsOutage> getOutagesForInterface(int nodeId, String ipAddr, Date time) ;

    Collection<OnmsOutage> getOutagesForService(int nodeId, String ipInterface, int serviceId) ;
    
    Collection<OnmsOutage>  getOutagesForService(int nodeId, String ipAddr, int serviceId, Date time) ;

	Collection<OnmsOutage> getCurrentOutages(String orderProperty);

	OnmsOutage load(Integer outageid);
	
	void update(OnmsOutage outage);
	
	Integer getOutageCount() ;
	
	Integer outageCountFiltered(String filter);

	Collection<OnmsOutage> getResolvedOutagesByRange(Integer offset, Integer limit, String orderProperty, String direction, String filter);

	Integer outageResolvedCountFiltered(String searchFilter);
	
    // This we may have to define 
    /*
    OutageSummary[] getCurrentOutageSummaries() ;

    OutageSummary[] getCurrentSDSOutageSummaries() ;
    */
    
}
