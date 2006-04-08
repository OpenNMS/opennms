/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.node;

import javax.sql.DataSource;


public class FindAll extends NodeMappingQuery {

    public FindAll(DataSource ds) {
        super(ds, "FROM node as n");
        compile();
    }
    
}