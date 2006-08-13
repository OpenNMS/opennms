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
import java.util.Collection;

import org.opennms.netmgt.dao.AggregateStatusViewDao;
import org.opennms.netmgt.model.AggregateStatusDefinition;
import org.opennms.netmgt.model.AggregateStatusView;
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
public class AggregateStatusViewDaoJdbc extends JdbcDaoSupport implements AggregateStatusViewDao  {

    private static final String STATUS_VIEW_DEF_MAPPING_TABLE = "statusview_statusdef";
    private static final String CATEGORY_DEF_MAPPING_TABLE = "category_statusdef";
    private static String AGGREATE_STATUS_VIEWS_TABLE = "aggregate_status_views";
    private static String AGGREGATE_STATUS_DEFINITIONS_TABLE = "aggregate_status_definitions";
    
    
    /**
     * 
     * @return <code>int</code> representing the number of defined views
     */
    public int countAll() {
        return getJdbcTemplate().queryForInt("select count(*) from "+AGGREATE_STATUS_VIEWS_TABLE);
    }
    
    
    /**
     * Get the status view configuration by view name
     * @param statusViewName
     * @return <code>AggreateStatusView</code> Object
     */
    public AggregateStatusView find(String statusViewName) {
        
        return (AggregateStatusView)getJdbcTemplate().queryForObject("select * " +
                "  from "+AGGREATE_STATUS_VIEWS_TABLE+
                " where name = '"+statusViewName+"'", statusViewMapper);
    }
    
    
    /**
     * Get the status view configuration by view ID
     * @param statusViewName
     * @return <code>AggreateStatusView</code> Object
     */
    public AggregateStatusView find(final int viewId) {
        
        return (AggregateStatusView)getJdbcTemplate().queryForObject("select *" +
                "  from "+AGGREATE_STATUS_VIEWS_TABLE+
                " where id = "+Integer.toString(viewId), statusViewMapper);
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
    private Collection<AggregateStatusDefinition> loadStatusDefs(Integer viewId) {

        final RowMapper statusDefMapper = new RowMapper() {

            public Object mapRow(ResultSet rs, int index) throws SQLException {
                AggregateStatusDefinition statusDef = new AggregateStatusDefinition();
                statusDef.setAggrStatusLabel(rs.getString("name"));
                statusDef.setCategories(loadStatusDefCategories(statusDef.getId()));
                return statusDef;
            }

        };
        
        return getJdbcTemplate().query("select * from "+AGGREGATE_STATUS_DEFINITIONS_TABLE+" asd " +
                "join "+STATUS_VIEW_DEF_MAPPING_TABLE+"+ svsd on (svsd.statusDefId = asd.id) "  +
                " where svsd.statusViewId = "+viewId, statusDefMapper);
    }
    
    private Collection<String> loadStatusDefCategories(Integer statusDefId) {

        final RowMapper rowMapper = new RowMapper() {

            public Object mapRow(ResultSet rs, int index) throws SQLException {
                String category = rs.getString("category");
                return category;
            }

        };
        
        return getJdbcTemplate().query("select c.categoryName as category from categories c " +
                "join "+CATEGORY_DEF_MAPPING_TABLE+" csd on (csd.categoryId = c.categoryId) " +
                "where csd.statusDefId = "+statusDefId, rowMapper);
    }

}
