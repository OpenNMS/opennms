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
// Modifications:
//
// 2006 Sep 10: Catch some null arguments that might cause problems. - dj@opennms.org
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

package org.opennms.web.svclayer.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.opennms.netmgt.config.siteStatusViews.Category;
import org.opennms.netmgt.config.siteStatusViews.RowDef;
import org.opennms.netmgt.config.siteStatusViews.View;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.AggregateStatusDefinition;
import org.opennms.netmgt.model.AggregateStatusView;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.svclayer.AggregateStatus;
import org.opennms.web.svclayer.SiteStatusViewService;
import org.opennms.web.svclayer.dao.SiteStatusViewConfigDao;

/**
 * This service layer class creates a collection that represents the current
 * status of devices per site (a column from the asset table such as building,
 * floor, etc.)  The status per site is broken down into rows of categories from
 * the categories table.
 * 
 * example:
 * 
 *              site: HQBLDB
 * 
 *  |Routers/Switches |   1 of  20 |
 *  |Servers          |   0 of 200 |
 *  |Hubs/APs         |   5 of  30 |
 *  
 *  
 * @author david hustace
 *
 */
public class DefaultSiteStatusViewService implements SiteStatusViewService {
    
    private NodeDao m_nodeDao;
    private CategoryDao m_categoryDao;
    private SiteStatusViewConfigDao m_siteStatusViewConfigDao;
    private OnmsNode m_foundDownNode;

    
    
    /**
     * Use the node id to find the value assciated with column defined in the view.  The view defines a column
     * and column value to be used by default.  This method determines the column value using the value associated
     * with the asset record for the given nodeid.
     * 
     * @see org.opennms.web.svclayer.SiteStatusViewService#createAggregateStatusesUsingNodeId(int, java.lang.String)
     */
    public Collection<AggregateStatus> createAggregateStatusesUsingNodeId(int nodeId, String viewName) {

        OnmsNode node = m_nodeDao.load(nodeId);
        
        //TODO this is a hack.  need to use reflection to get the right column instead of building.
        return createAggreateStatuses(createAggregateStatusView(viewName), node.getAssetRecord().getBuilding());
    }

    /**
     * This creator looks up a configured status view by name and calls the creator that 
     * accepts the AggregateStatusView model object.
     * 
     * @see org.opennms.web.svclayer.SiteStatusViewService#createAggregateStatusView(java.lang.String)
     */
    public AggregateStatusView createAggregateStatusView(String statusViewName) {
        
        AggregateStatusView statusView = new AggregateStatusView();
        View view = m_siteStatusViewConfigDao.getView(statusViewName);
        
        statusViewName = (statusViewName == null ? m_siteStatusViewConfigDao.getDefaultView().getName() : statusViewName);

        statusView.setName(statusViewName);
        statusView.setColumnName(view.getColumnName());
        statusView.setColumnValue(view.getColumnValue());
        statusView.setTableName(view.getTableName());
        
        Set<AggregateStatusDefinition> statusDefs = new LinkedHashSet<AggregateStatusDefinition>();
        final ArrayList rowDefs = m_siteStatusViewConfigDao.getView(statusViewName).getRows().getRowDefCollection();
        for (Iterator it = rowDefs.iterator(); it.hasNext();) {
            RowDef rowDef = (RowDef) it.next();
            AggregateStatusDefinition def = new AggregateStatusDefinition();
            def.setName(rowDef.getLabel());
            
            Set<OnmsCategory> categories = new LinkedHashSet<OnmsCategory>();
            
            for (Iterator catIter = rowDef.getCategoryCollection().iterator(); catIter.hasNext();) {
                Category cat = (Category) catIter.next();
                OnmsCategory category = m_categoryDao.findByName(cat.getName());
                
                if (category == null) {
                    throw new IllegalArgumentException("Site status configured category not found: "+cat.getName());
                }
                
                categories.add(category);
            }
            def.setCategories(categories);
        }
        
        statusView.setStatusDefinitions(statusDefs);
        return statusView;
    }


    /**
     * Creates the collection of aggregated statuses by calling the creator with data filled from 
     * the passed in AggregateStatusView model object.

     * @see org.opennms.web.svclayer.SiteStatusViewService#createAggreateStatuses(org.opennms.netmgt.model.AggregateStatusView)
     */
    public Collection<AggregateStatus> createAggreateStatuses(AggregateStatusView statusView) {
        if (statusView == null) {
            throw new IllegalArgumentException("statusView argument cannot be null");
        }
        return createAggregateStatus(statusView.getTableName(), statusView.getColumnName(), statusView.getColumnValue(), statusView.getStatusDefinitions());
    }

    
    /**
     * This creator is used when wanting to use a different value than the defined column value defined
     * for the requested view.
     * 
     * @see org.opennms.web.svclayer.SiteStatusViewService#createAggreateStatuses(org.opennms.netmgt.model.AggregateStatusView, java.lang.String)
     */
    public Collection<AggregateStatus> createAggreateStatuses(AggregateStatusView statusView, String statusSite) {
        if (statusView == null) {
            throw new IllegalArgumentException("statusView argument cannot be null");
        }
        return createAggregateStatus(statusView.getTableName(), statusView.getColumnName(), statusSite, statusView.getStatusDefinitions());
    }

    
    private Collection<AggregateStatus> createAggregateStatus(String tableName, String columnName, String columnValue, Collection<AggregateStatusDefinition> statusDefinitions) {
        if (tableName != null && !tableName.equalsIgnoreCase("assets")) {
            throw new UnsupportedOperationException("This service currently only implmented for aggregation on asset columns.");
        }
        return createAggregateStatusUsingAssetColumn(columnName, columnValue, statusDefinitions);
    }

    public Collection<AggregateStatus> createAggregateStatusUsingAssetColumn(String assetColumn,
            String columnValue, Collection<AggregateStatusDefinition> categoryGrouping) {
        
        /*
         * We'll return this collection populated with all the aggregated statuss for the
         * devices in the building (site) by for each group of categories.
         */
        Collection<AggregateStatus> stati = new ArrayList<AggregateStatus>();
        
        /*
         * Iterate over the status definitions and create aggregated statuss
         */
        for (AggregateStatusDefinition statusDef : categoryGrouping) {
            
            Collection<OnmsNode> nodes = m_nodeDao.findAllByVarCharAssetColumnCategoryList(assetColumn, columnValue, statusDef.getCategories());
            
//            for (OnmsNode node : nodes) {
//                m_nodeDao.getHierarchy(node.getId());
//            }
            
            
            AggregateStatus status = new AggregateStatus(new HashSet<OnmsNode>(nodes));
            
            status.setLabel((m_foundDownNode == null ? statusDef.getName(): createNodePageUrl(statusDef.getName())));
            m_foundDownNode = null; //what a hack  make the model (as in MAV) better
            
            stati.add(status);
        }
        
        return stati;
    }
    
    /*
     * This creates a relative url to the node page and sets the node parameter
     * FIXME: this code should move to the jsp after the status table is enhanced to support
     * this requirement.
     */
    private String createNodePageUrl(String label) {
        if (m_foundDownNode != null) {
            label = "<a href=\"element/node.jsp?node="+m_foundDownNode.getId()+"\">"+label+"</a>";
        }
        return label;
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }
    
    public void setCategoryDao(CategoryDao dao) {
        m_categoryDao = dao;
    }
    
    public void setSiteStatusViewConfigDao(SiteStatusViewConfigDao dao) {
        m_siteStatusViewConfigDao = dao;
    }

}
