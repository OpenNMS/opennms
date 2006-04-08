/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.asset;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

public class FindByAssetId extends AssetMappingQuery {
    
    public FindByAssetId(DataSource ds) {
        super(ds, "FROM assets where nodeid = ?");
        super.declareParameter(new SqlParameter("id", Types.INTEGER));
        compile();
    }

}