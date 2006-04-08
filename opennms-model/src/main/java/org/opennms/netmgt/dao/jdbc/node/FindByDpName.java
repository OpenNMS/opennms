/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.node;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

public class FindByDpName extends NodeMappingQuery {

    public FindByDpName(DataSource ds) {
        super(ds, "FROM node as n WHERE dpName = ?");
        super.declareParameter(new SqlParameter("dpName", Types.VARCHAR));
        compile();
    }
    
}