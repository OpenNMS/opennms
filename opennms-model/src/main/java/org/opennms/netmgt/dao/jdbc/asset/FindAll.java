/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.asset;

import javax.sql.DataSource;


public class FindAll extends AssetMappingQuery {
    public FindAll(DataSource ds) {
        super(ds, "FROM assets");
        compile();
    }
}