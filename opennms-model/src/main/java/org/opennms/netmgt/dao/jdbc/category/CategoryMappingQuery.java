package org.opennms.netmgt.dao.jdbc.category;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Cache;
import org.opennms.netmgt.dao.jdbc.JdbcSet;
import org.opennms.netmgt.model.OnmsCategory;
import org.springframework.jdbc.object.MappingSqlQuery;

public class CategoryMappingQuery extends MappingSqlQuery {

	public CategoryMappingQuery(DataSource ds, String clause) {
		super(ds, "SELECT categories.categoryId as catId, categories.categoryName as catName, categories.categoryDescription as catDescr "+clause);
	}

	protected Object mapRow(ResultSet rs, int rowNum) throws SQLException {
	    Integer id = (Integer) rs.getObject("catId");
	    LazyCategory category = (LazyCategory)Cache.obtain(OnmsCategory.class, id);
	    category.setLoaded(true);
	    category.setName(rs.getString("catName"));
	    category.setDescription(rs.getString("catDescr"));
	    category.setDirty(false);
	    return category;
	}
	
	public OnmsCategory findUnique() {
		return findUnique((Object[])null);
	}
	
	public OnmsCategory findUnique(Object obj) {
		return findUnique(new Object[] { obj });
	}
	
	public OnmsCategory findUnique(Object[] objs) {
		List types = execute(objs);
		if (types.size() > 0)
			return (OnmsCategory) types.get(0);
		else
			return null;
	}
	
	public Set findSet() {
		return findSet((Object[])null);
	}
	
    public Set findSet(Object obj) {
        return findSet(new Object[] { obj });
    }
	
	public Set findSet(Object[] objs) {
		List types = execute(objs);
		Set results = new JdbcSet(types);
		return results;
	}

}
