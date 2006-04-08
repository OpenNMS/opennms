package org.opennms.netmgt.dao.jdbc.node;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

public class FindByAssetNumber extends NodeMappingQuery {

	public FindByAssetNumber(DataSource ds) {
		super(ds, "from node as n, assets as a where n.nodeId = a.nodeId and a.assetNumber = ?");
		declareParameter(new SqlParameter("assetNumber", Types.VARCHAR));
		compile();
	}

}
