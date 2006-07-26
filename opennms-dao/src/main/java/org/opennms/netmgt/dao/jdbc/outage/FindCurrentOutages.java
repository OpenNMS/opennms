package org.opennms.netmgt.dao.jdbc.outage;

import javax.sql.DataSource;

public class FindCurrentOutages extends OutageMappingQuery {

    public FindCurrentOutages(DataSource ds) {
        super(ds,"FROM outages as outages, ifservices as ifservices WHERE outages.nodeID = ifservices.nodeID and outages.ipAddr = ifservices.ipAddr and outages.serviceID = ifservices.serviceID");
        compile();
    }
    
    public FindCurrentOutages(DataSource ds, Integer offset, Integer limit) {
        super(ds,"FROM outages as outages, ifservices as ifservices WHERE outages.nodeID = ifservices.nodeID and outages.ipAddr = ifservices.ipAddr and outages.serviceID = ifservices.serviceID OFFSET " + offset + " LIMIT " + limit);
        compile();
    }

}
