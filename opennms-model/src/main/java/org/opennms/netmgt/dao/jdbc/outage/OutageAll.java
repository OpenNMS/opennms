/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.outage;

import javax.sql.DataSource;


public class OutageAll extends OutageMappingQuery {

    public OutageAll(DataSource ds) {
        super(ds, "FROM outages as outages, ifservices as ifservices WHERE outages.nodeId = ifservices.nodeId and outages.ipAddr = ifservices.ipAddr and outages.serviceID = ifservices.serviceID");
        compile();
    }
    
}