package org.opennms.netmgt.dao.jdbc.node;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

public class FindByNodeLabel extends NodeMappingQuery {

	public FindByNodeLabel(DataSource ds) {
		super(ds, "from node as n where n.nodeLabel = ?");
		declareParameter(new SqlParameter("nodeLabel", Types.VARCHAR));
		compile();
	}

}
