/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.outage;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

public class FindByOutageId extends OutageMappingQuery {

    public FindByOutageId(DataSource ds) {
        super(ds, "FROM outages as outages, ifservices as ifservices WHERE outages.nodeID = ifservices.nodeID and outages.ipAddr = ifservices.ipAddr and outages.serviceID = ifservices.serviceID and outages.outageid = ?");
        super.declareParameter(new SqlParameter("outageid", Types.INTEGER));
        compile();
    }
    
}