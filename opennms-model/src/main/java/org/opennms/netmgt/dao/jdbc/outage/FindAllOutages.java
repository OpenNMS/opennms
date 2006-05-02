/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.outage;

import javax.sql.DataSource;


public class FindAllOutages extends OutageMappingQuery {

    public FindAllOutages(DataSource ds) {
        super(ds, "FROM outages as outages, ifservices as ifservices WHERE outages.nodeID = ifservices.nodeID and outages.ipAddr = ifservices.ipAddr and outages.serviceID = ifservices.serviceID");
        compile();
    }
    
}