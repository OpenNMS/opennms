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
//

package org.opennms.netmgt.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.AggregateStatusViewDao;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.model.AggregateStatusDefinition;
import org.opennms.netmgt.model.AggregateStatusView;
import org.opennms.netmgt.model.OnmsCategory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * JDBC implementation of the DAO for the configuration of aggregate status 
 * views and definitions.
 * 
 * Note: Didn't get "lazy" on this one (actually got developer lazy by not implmementing
 * lazy DAOs... waiting on Hibernate).
 * 
 * Having read that note, this DAO loads up all the dependent collections.  This should be
 * very fast and not actually needing support for Lazy loading because this is configuration
 * only.
 * 
 * @author david
 *
 */
public class AggregateStatusViewDaoJdbc extends AbstractDaoJdbc implements AggregateStatusViewDao  {

    private static final String STATUS_VIEW_DEF_MAPPING_TABLE = "statusview_statusdef";
    private static final String CATEGORY_DEF_MAPPING_TABLE = "category_statusdef";
    private static final String AGGREATE_STATUS_VIEWS_TABLE = "aggregate_status_views";
    private static final String AGGREGATE_STATUS_DEFINITIONS_TABLE = "aggregate_status_definitions";
    
    private CategoryDao m_categoryDao = null;
    
    private CategoryDao getCategoryDao() {
    	if (m_categoryDao == null) {
    		m_categoryDao = new CategoryDaoJdbc(getDataSource());
    	}
    	return m_categoryDao;
    }
    
    /**
     * 
     * @return <code>int</code> representing the number of defined views
     */
    public int countAll() {
        return getJdbcTemplate().queryForInt("select count(*) from "+AGGREATE_STATUS_VIEWS_TABLE);
    }
    
    /**
     * Get the status view configuration by view name
     * @param viewName
     * @return <code>AggreateStatusView</code> Object
     */
    public AggregateStatusView findByName(final String viewName) {
        log().debug("find (viewName): begin finding aggregate status view.");
        AggregateStatusView view = (AggregateStatusView)getJdbcTemplate().queryForObject("select * " +
                        "  from "+AGGREATE_STATUS_VIEWS_TABLE+
                        " where name = '"+viewName+"'", statusViewMapper);
        log().debug("find: completed finding aggregate staus view.  View found: "+view);
        return view;
    }
    
    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    /**
     * Get the status view configuration by view ID
     * @param statusViewName
     * @return <code>AggreateStatusView</code> Object
     */
    public AggregateStatusView get(Integer viewId) {
        log().debug("find (int): begin finding aggregate status view.");
        AggregateStatusView view = (AggregateStatusView)getJdbcTemplate().queryForObject("select *" +
                        "  from "+AGGREATE_STATUS_VIEWS_TABLE+
                        " where id = "+Integer.toString(viewId), statusViewMapper);
        log().debug("find: completed finding aggregate staus view.  View found: "+view);
        return view;
    }

    
    private final RowMapper statusViewMapper = new RowMapper() {

        public Object mapRow(ResultSet rs, int index) throws SQLException {
            AggregateStatusView view = new AggregateStatusView();
            view.setId(rs.getInt("id"));
            view.setName(rs.getString("name"));
            view.setTableName(rs.getString("tableName"));
            view.setColumnName(rs.getString("columnName"));
            view.setColumnValue(rs.getString("columnValue"));
            view.setStatusDefinitions(loadStatusDefs(view.getId()));
            return view;
        }

    };

    /**
     * Get the status defintions for a view by view ID
     * @param viewId
     * @return
     */
    @SuppressWarnings("unchecked")
	private Set<AggregateStatusDefinition> loadStatusDefs(final Integer viewId) {

        final RowMapper statusDefMapper = new RowMapper() {

            public Object mapRow(ResultSet rs, int index) throws SQLException {
                AggregateStatusDefinition statusDef = new AggregateStatusDefinition();
                statusDef.setId(rs.getInt("id"));
                statusDef.setName(rs.getString("name"));
                statusDef.setCategories(loadStatusDefCategories(statusDef.getId()));
                return statusDef;
            }

        };
        
        return new LinkedHashSet<AggregateStatusDefinition>(getJdbcTemplate().query("select * from "+AGGREGATE_STATUS_DEFINITIONS_TABLE+" asd " +
                "join "+STATUS_VIEW_DEF_MAPPING_TABLE+" svsd on (svsd.statusDefId = asd.id) "  +
                " where svsd.statusViewId = "+viewId, statusDefMapper));
    }
    
    @SuppressWarnings("unchecked")
	private Set<OnmsCategory> loadStatusDefCategories(final Integer statusDefId) {

        final RowMapper rowMapper = new RowMapper() {

            public Object mapRow(ResultSet rs, int index) throws SQLException {
                int categoryId = rs.getInt("category");
                return getCategoryDao().get(categoryId);
            }

        };
        
        return new LinkedHashSet<OnmsCategory>(getJdbcTemplate().query("select c.categoryId as category from categories c " +
                "join "+CATEGORY_DEF_MAPPING_TABLE+" csd on (csd.categoryId = c.categoryId) " +
                "where csd.statusDefId = "+statusDefId, rowMapper));
    }


	public List<AggregateStatusView> findAll() {
		throw new UnsupportedOperationException("not yet implemented!");
	}


	public AggregateStatusView getByName(String statusViewName) {
		throw new UnsupportedOperationException("not yet implemented!");
	}


	public void saveOrUpdate(AggregateStatusView view) {
		throw new UnsupportedOperationException("not yet implemented!");
	}


	public void save(AggregateStatusView view) {
		throw new UnsupportedOperationException("not yet implemented!");
	}


	public void delete(int id) {
		throw new UnsupportedOperationException("not yet implemented!");
	}


	public void update(AggregateStatusView view) {
		throw new UnsupportedOperationException("not yet implemented!");
	}


	public void clear() {
		throw new UnsupportedOperationException("not yet implemented!");
	}


	public void delete(AggregateStatusView entity) {
		delete(entity.getId());
	}


	public void flush() {
		throw new UnsupportedOperationException("not yet implemented!");
	}


	public AggregateStatusView load(Integer id) {
		return get(id);
	}

}
