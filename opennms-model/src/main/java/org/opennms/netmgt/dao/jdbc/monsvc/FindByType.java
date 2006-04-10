/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.monsvc;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

public class FindByType extends MonitoredServiceMappingQuery {
    public FindByType(DataSource ds) {
        super(ds, "FROM ifservices, service where ifservices.serviceid = service.serviceid and service.servicename = ? and ifservices.status != 'D'");
        declareParameter(new SqlParameter("serviceName", Types.VARCHAR));
        compile();
    }
    
    
}