package org.opennms.netmgt.dao.jdbc.outage;
import javax.sql.DataSource;


public class FindResolvedOutages extends OutageMappingQuery {

    public FindResolvedOutages(DataSource ds, Integer offset, Integer limit, String order, String direction, String filter) {
        super(ds, "FROM outages as outages, ifservices as ifservices WHERE outages.ifregainedservice > 1 AND outages.nodeID = ifservices.nodeID and outages.ipAddr = ifservices.ipAddr and outages.serviceID = ifservices.serviceID and  (outages.suppresstime is null or outages.suppresstime < now() ) " + filter + " order by  " + order + " " + direction + " LIMIT + " + limit + " OFFSET " + offset  );
        
        
        compile();
    }
    
}