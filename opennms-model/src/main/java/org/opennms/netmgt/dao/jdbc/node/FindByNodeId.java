/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.node;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

public class FindByNodeId extends NodeMappingQuery {

    public FindByNodeId(DataSource ds) {
        super(ds, "FROM node as n WHERE n.nodeid = ?");  // Should the clause include "FROM node as n"
        super.declareParameter(new SqlParameter("nodeid", Types.INTEGER));
        compile();
    }
    
}