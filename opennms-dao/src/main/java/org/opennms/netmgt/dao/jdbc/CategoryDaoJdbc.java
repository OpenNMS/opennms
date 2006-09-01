//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/

package org.opennms.netmgt.dao.jdbc;

import java.sql.Types;
import java.util.Collection;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.jdbc.category.CategoryMappingQuery;
import org.opennms.netmgt.dao.jdbc.category.FindByCategoryId;
import org.opennms.netmgt.model.OnmsCategory;
import org.springframework.jdbc.core.SqlParameter;

public class CategoryDaoJdbc extends AbstractDaoJdbc implements CategoryDao {
	
	public static class FindAll extends CategoryMappingQuery {
		public FindAll(DataSource ds) {
			super(ds, "FROM categories");
			compile();
		}
	}
    
    public static class FindByName extends CategoryMappingQuery {
        public FindByName(DataSource ds) {
            super(ds, "FROM categories where categories.categoryName = ?");
            declareParameter(new SqlParameter("categories.categoryName", Types.VARCHAR));
            compile();
        }
    }
    
    public CategoryDaoJdbc() {
        super();
    }
    
    public CategoryDaoJdbc(DataSource ds) {
        super(ds);
    }


	public void clear() {
        Cache.clear();
	}

	public int countAll() {
        return getJdbcTemplate().queryForInt("select count(*) from categories");
	}

	public Collection findAll() {
        return new FindAll(getDataSource()).execute();
	}

	public void flush() {
	}

	public OnmsCategory load(Integer id) {
        return new FindByCategoryId(getDataSource()).findUnique(id);
	}

	public void save(OnmsCategory category) {
        if (category.getId() != null)
            throw new IllegalArgumentException("Cannot save a category that already has an id");
        
        doSave(category);
	}

    private void doSave(OnmsCategory category) {
        category.setId(allocateId());        
        getJdbcTemplate().update("insert into categories (categoryName, categoryDescription, categoryId) values (?, ?, ?)",
                                 new Object[] {
                                    category.getName(),
                                    category.getDescription(),
                                    category.getId()
                                 });
    }

	private Integer allocateId() {
        return new Integer(getJdbcTemplate().queryForInt("SELECT nextval('catNxtId')"));
    }

    public void update(OnmsCategory category) {
        if (category.getId() == null)
            throw new IllegalArgumentException("Cannot update a category that does not have an id");
        
        doUpdate(category);
	}

    private void doUpdate(OnmsCategory category) {
        getJdbcTemplate().update("update categories set categoryName = ?, categoryDescription = ? where categoryId = ?",
                                 new Object[] {
                                    category.getName(),
                                    category.getDescription(),
                                    category.getId()
                                 });
    }
    
    public void saveOrUpdate(OnmsCategory category) {
        if (category.getId() == null)
            doSave(category);
        else
            doUpdate(category);
    }

    public OnmsCategory findByName(String name) {
        return new FindByName(getDataSource()).findUnique(name);
    }

	public void delete(OnmsCategory entity) {
		getJdbcTemplate().update("delete from categoryies where category.categoryid = ?", new Object[] { new Integer(entity.getId())});
	}

	public OnmsCategory get(Integer id) {
		// TODO Auto-generated method stub
		return null;
	}

}
