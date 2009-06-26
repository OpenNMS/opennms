/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 2007 Aug 03: Change ArrayList -> List from Castor methods. - dj@opennms.org
 * 2006 Sep 10: Catch some null arguments that might cause problems. - dj@opennms.org
 * 
 * 
 * Created: September 12, 2006
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.svclayer.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.config.siteStatusViews.Category;
import org.opennms.netmgt.config.siteStatusViews.RowDef;
import org.opennms.netmgt.config.siteStatusViews.Rows;
import org.opennms.netmgt.config.siteStatusViews.View;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.AggregateStatusDefinition;
import org.opennms.netmgt.model.AggregateStatusView;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.Util;
import org.opennms.web.svclayer.AggregateStatus;
import org.opennms.web.svclayer.SiteStatusViewService;
import org.opennms.web.svclayer.dao.SiteStatusViewConfigDao;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.orm.ObjectRetrievalFailureException;

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
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 */
public class DefaultSiteStatusViewService implements SiteStatusViewService {
    
    private NodeDao m_nodeDao;
    private CategoryDao m_categoryDao;
    private SiteStatusViewConfigDao m_siteStatusViewConfigDao;
    
    
    /**
     * This creator looks up a configured status view by name and calls the creator that 
     * accepts the AggregateStatusView model object.
     * 
     * @see org.opennms.web.svclayer.SiteStatusViewService#createAggregateStatusView(java.lang.String)
     */
    public AggregateStatusView createAggregateStatusView(String statusViewName) {
        AggregateStatusView statusView = new AggregateStatusView();
        statusViewName = (statusViewName == null ? m_siteStatusViewConfigDao.getDefaultView().getName() : statusViewName);
        
        View view = m_siteStatusViewConfigDao.getView(statusViewName);
        
        statusView.setName(statusViewName);
        statusView.setColumnName(view.getColumnName());
        statusView.setColumnValue(view.getColumnValue());
        statusView.setTableName(view.getTableName());
        
        Set<AggregateStatusDefinition> statusDefs =
            getAggregateStatusDefinitionsForView(view);
        statusView.setStatusDefinitions(statusDefs);
        return statusView;
    }


    @SuppressWarnings("unchecked")
    private Set<AggregateStatusDefinition> getAggregateStatusDefinitionsForView(View view) {
        Set<AggregateStatusDefinition> statusDefs = new LinkedHashSet<AggregateStatusDefinition>();
        List<RowDef> rowDefs = view.getRows().getRowDefCollection();
        
        //Loop over the defined site status rows
        for (RowDef rowDef : rowDefs) {
            AggregateStatusDefinition def = new AggregateStatusDefinition();
            def.setName(rowDef.getLabel());
            def.setReportCategory(rowDef.getReportCategory());
            
            Set<OnmsCategory> categories = getCategoriesForRowDef(rowDef);
            def.setCategories(categories);
            statusDefs.add(def);
        }
        return statusDefs;
    }


    @SuppressWarnings("unchecked")
    private Set<OnmsCategory> getCategoriesForRowDef(RowDef rowDef) {
        Set<OnmsCategory> categories = new LinkedHashSet<OnmsCategory>();
        
        //Loop over the defined categories and create model categories (OnmsCategory)
        List<Category> cats = rowDef.getCategoryCollection();
        for (Category cat : cats) {
            OnmsCategory category = m_categoryDao.findByName(cat.getName());
            
            if (category == null) {
                throw new ObjectRetrievalFailureException(OnmsCategory.class, cat.getName(), "Unable to locate OnmsCategory named: "+cat.getName()+" as specified in the site status view configuration file", null);
            }
            
            categories.add(category);
        }
        return categories;
    }


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
        return createAggregateStatuses(createAggregateStatusView(viewName), node.getAssetRecord().getBuilding());
    }



    /**
     * This creator is used when wanting to use a different value than the defined column value defined
     * for the requested view.
     * 
     * @see org.opennms.web.svclayer.SiteStatusViewService#createAggregateStatuses(org.opennms.netmgt.model.AggregateStatusView, java.lang.String)
     */
    public Collection<AggregateStatus> createAggregateStatuses(AggregateStatusView statusView, String statusSite) {
        if (statusView == null) {
            throw new IllegalArgumentException("statusView argument cannot be null");
        }
        statusView.setColumnValue(statusSite);
        return createAggregateStatusUsingAssetColumn(statusView);
    }

    
    public Collection<AggregateStatus> createAggregateStatusUsingAssetColumn(AggregateStatusView statusView) {
        
        if (statusView == null) {
            throw new IllegalArgumentException("statusView argument cannot be null");
        }
        
        /*
         * We'll return this collection populated with all the aggregated statuss for the
         * devices in the building (site) by for each group of categories.
         */
        Collection<AggregateStatus> stati = new ArrayList<AggregateStatus>();
        
        /*
         * Iterate over the status definitions and create aggregated statuss
         */
        for (AggregateStatusDefinition statusDef : statusView.getStatusDefinitions()) {
            Collection<OnmsNode> nodes = m_nodeDao.findAllByVarCharAssetColumnCategoryList(statusView.getColumnName(), statusView.getColumnValue(), statusDef.getCategories());
            AggregateStatus status = new AggregateStatus(new HashSet<OnmsNode>(nodes));
            status.setLabel(statusDef.getName());
            
            status.setLink(createNodePageUrl(statusView, status));
            stati.add(status);
        }
        
        return stati;
    }
    
    private String createNodePageUrl(AggregateStatusView statusView, AggregateStatus status) {
        
        if (status.getDownEntityCount() == 0) {
            StringBuffer buf = new StringBuffer("element/nodeList.htm?");
            buf.append("statusViewName=");
            buf.append(Util.encode(statusView.getName()));
            buf.append('&');
            buf.append("statusSite=");
            buf.append(Util.encode(statusView.getColumnValue()));
            buf.append('&');
            buf.append("statusRowLabel=");
            buf.append(Util.encode(status.getLabel()));
            return buf.toString();
        } else if (status.getDownEntityCount() == 1) {
            OnmsNode node = status.getDownNodes().iterator().next();
            StringBuffer buf = new StringBuffer("element/node.jsp?");
            buf.append("node=");
            buf.append(node.getId());
            return buf.toString();
        } else {
            StringBuffer buf = new StringBuffer("element/nodeList.htm?");
            buf.append("statusViewName=");
            buf.append(Util.encode(statusView.getName()));
            buf.append('&');
            buf.append("statusSite=");
            buf.append(Util.encode(statusView.getColumnValue()));
            buf.append('&');
            buf.append("statusRowLabel=");
            buf.append(Util.encode(status.getLabel()));
            buf.append('&');
            buf.append("nodesWithDownAggregateStatus");
            return buf.toString();
        }
        
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


    public Collection<AggregateStatus> createAggregateStatuses(AggregateStatusView statusView) {
        if (! "assets".equalsIgnoreCase("assets")) {
            throw new IllegalArgumentException("statusView only currently supports asset table columns");
        }
        return createAggregateStatusUsingAssetColumn(statusView);
    }

    public AggregateStatus getAggregateStatus(String statusViewName, String statusSite, String rowLabel) {
        
        AggregateStatusView statusView = createAggregateStatusView(statusViewName);
        Collection<AggregateStatus> stati = createAggregateStatuses(statusView, statusSite);
        
        for (AggregateStatus status : stati) {
            if (status.getLabel().equals(rowLabel)) {
                return status;
            }
        }
        throw new DataRetrievalFailureException("Unable to locate row: "+rowLabel+" for status view: "+statusViewName);
    }


    public Collection<OnmsNode> getNodes(String statusViewName, String statusSite, String rowLabel) {
        if (statusViewName == null) {
            statusViewName = m_siteStatusViewConfigDao.getDefaultView().getName();
        }
        
        View view = m_siteStatusViewConfigDao.getView(statusViewName);
        RowDef rowDef = getRowDef(view, rowLabel);
        Set<OnmsCategory> categories = getCategoriesForRowDef(rowDef);
        
        return m_nodeDao.findAllByVarCharAssetColumnCategoryList(view.getColumnName(), statusSite, categories);
    }


    @SuppressWarnings("unchecked")
    private RowDef getRowDef(View view, String rowLabel) {
        Rows rows = view.getRows();
        Collection<RowDef> rowDefs = rows.getRowDefCollection();
        for (RowDef rowDef : rowDefs) {
            if (rowDef.getLabel().equals(rowLabel)) {
                return rowDef;
            }
        }
        
        throw new DataRetrievalFailureException("Unable to locate row: "+rowLabel+" for status view: "+view.getName());
    }

}
