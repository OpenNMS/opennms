/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.ipif;

import javax.sql.DataSource;


public class FindAll extends IpInterfaceMappingQuery {

    public FindAll(DataSource ds) {
        super(ds, "FROM ipInterface");
        compile();
    }
}