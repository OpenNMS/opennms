package org.opennms.netmgt.dao.jdbc.category;

import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlParameter;

public class FindByCategoryId extends CategoryMappingQuery {
	
	public FindByCategoryId(DataSource ds) {
		super(ds, "FROM categories where categoryies.categoryId = ?");
		super.declareParameter(new SqlParameter("id", Types.INTEGER));
		compile();
	}

}
