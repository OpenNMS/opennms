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

package org.opennms.web.svclayer.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.config.surveillanceViews.Category;
import org.opennms.netmgt.config.surveillanceViews.ColumnDef;
import org.opennms.netmgt.config.surveillanceViews.Columns;
import org.opennms.netmgt.config.surveillanceViews.RowDef;
import org.opennms.netmgt.config.surveillanceViews.Rows;
import org.opennms.netmgt.config.surveillanceViews.View;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.svclayer.AggregateStatus;
import org.opennms.web.svclayer.SimpleWebTable;
import org.opennms.web.svclayer.SurveillanceService;
import org.opennms.web.svclayer.SurveillanceTable;
import org.opennms.web.svclayer.dao.SurveillanceViewConfigDao;

public class DefaultSurveillanceService implements SurveillanceService {

    private NodeDao m_nodeDao;
    private CategoryDao m_categoryDao;
    private SurveillanceViewConfigDao m_surveillanceConfigDao;
    private OnmsNode m_foundDownNode;
    
    
    public SurveillanceTable createSurveillanceTable() {
        return createSurveillanceTable("default");
    }

    /**
     * Creates a custom table object containing intersected rows and
     * columns and categories.
     */
    public SurveillanceTable createSurveillanceTable(String surveillanceViewName) {

        View view = m_surveillanceConfigDao.getView(surveillanceViewName);
        
        final Rows rows = view.getRows();
        final Columns columns = view.getColumns();
        
        /*
         * Initialize a status table 
         */
        final SurveillanceTable statusTable = new SurveillanceTable(rows.getRowDefCount(), columns.getColumnDefCount());
        SimpleWebTable webTable = new SimpleWebTable();
        statusTable.setLabel(view.getName());
        webTable.setTitle(view.getName());
        statusTable.setWebTable(webTable);
        
        webTable.addColumn("Nodes", "simpleWebTableHeader");
        
        List<Category> viewRowCats = new ArrayList<Category>();
        List<Category> viewColCats = new ArrayList<Category>();
        List rowDefs = rows.getRowDefCollection();
        List columnDefs = columns.getColumnDefCollection();

        /*
         * Iterate of the requested view's configuration (row's and columns) and set an aggreated status into each table
         * cell.
         */
        
        for (Iterator rowDefIter = rowDefs.iterator(); rowDefIter.hasNext();) {
            RowDef rowDef = (RowDef) rowDefIter.next();
            statusTable.setRowHeader(rowDef.getRow()-1, rowDef.getLabel());
            viewRowCats.addAll(rowDef.getCategoryCollection());
            statusTable.setNodesForRow(rowDef.getRow()-1, createNodes(viewRowCats));
            viewRowCats.removeAll(rowDef.getCategoryCollection());
        }

        for (Iterator colDefIter = columnDefs.iterator(); colDefIter.hasNext();) {
            ColumnDef colDef = (ColumnDef) colDefIter.next();
            viewColCats.addAll(colDef.getCategoryCollection());
            statusTable.setNodesForColumn(colDef.getCol()-1, createNodes(viewColCats));
            statusTable.setColumnHeader(colDef.getCol()-1, colDef.getLabel());
            viewColCats.removeAll(colDef.getCategoryCollection());
            
            webTable.addColumn(colDef.getLabel(), "simpleWebTableHeader");
        }

        for (Iterator rowDefIter = rowDefs.iterator(); rowDefIter.hasNext();) {
            RowDef rowDef = (RowDef) rowDefIter.next();
            
            webTable.newRow();
            webTable.addCell(rowDef.getLabel(), "simpleWebTableRowLabel");
            
            for (Iterator colDefIter = columnDefs.iterator(); colDefIter.hasNext();) {
                ColumnDef colDef = (ColumnDef) colDefIter.next();
                final Set<OnmsNode> intersectNodes = new HashSet<OnmsNode>(statusTable.getNodesForRow(rowDef.getRow()-1));
                intersectNodes.retainAll(statusTable.getNodesForColumn(colDef.getCol()-1));
                AggregateStatus aggStatus = createAggregateStatus(intersectNodes);
				statusTable.setStatus(rowDef.getRow()-1, colDef.getCol()-1, aggStatus);
				
				webTable.addCell(aggStatus.getDownEntityCount()+" of "+aggStatus.getTotalEntityCount(), aggStatus.getStatus());

                if (statusTable.getStatus(rowDef.getRow()-1, colDef.getCol()-1).getDownEntityCount() > 0) {
                    statusTable.setRowHeader(rowDef.getRow()-1, createNodePageUrl(rowDef.getLabel()));
                    m_foundDownNode = null; //what a hack
                }
            }
                
        }
        return statusTable;
    }

    private Collection<OnmsNode> createNodes(List<Category> viewCats) {
        
        return m_nodeDao.findAllByCategoryList(createCategories(viewCats));
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

    /**
     * This method takes list of configured surveillance view categories
     * and returns a list of OnmsCategories
     * @param viewCats
     * @return
     */
    private Collection<OnmsCategory> createCategories(List<Category> viewCats) {
        List<String> catNames = new ArrayList<String>();
        for (Category category : viewCats) {
            catNames.add(category.getName());
        }
        
        return createCategoryNameCollection(catNames);
    }

    /**
     * This method takes a list of category names and returns a
     * list of OnmsCategories
     * 
     * @param categoryNames
     * @return
     */
    private Collection<OnmsCategory> createCategoryNameCollection(List<String> categoryNames) {
        
        Collection<OnmsCategory> categories = new ArrayList<OnmsCategory>();
        for (String catName : categoryNames) {
            categories.add(m_categoryDao.findByName(catName));
        }
        return categories;
    }

    private AggregateStatus createAggregateStatus(Set<OnmsNode> nodes) {
        AggregateStatus status;
        status = new AggregateStatus();
        if (nodes == null || nodes.isEmpty()) {
            status.setDownEntityCount(0);
            status.setTotalEntityCount(0);
            status.setStatus(AggregateStatus.ALL_NODES_UP);
        } else {
            status.setDownEntityCount(computeDownCount(nodes));
            status.setTotalEntityCount(nodes.size());
            status.setStatus(computeStatus(nodes, status));
        }
        return status;
    }

    private String computeStatus(Collection<OnmsNode> nodes, AggregateStatus status) {
        
        String color = AggregateStatus.ALL_NODES_UP;
        
        if (status.getDownEntityCount() >= 1) {
            color = AggregateStatus.NODES_ARE_DOWN;
            return color;
        }
        
        for (Iterator it = nodes.iterator(); it.hasNext();) {
            OnmsNode node = (OnmsNode) it.next();
            Set<OnmsIpInterface> ifs = node.getIpInterfaces();
            for (Iterator ifIter = ifs.iterator(); ifIter.hasNext();) {
                OnmsIpInterface ipIf = (OnmsIpInterface) ifIter.next();
                Set<OnmsMonitoredService> svcs = ipIf.getMonitoredServices();
                for (Iterator svcIter = svcs.iterator(); svcIter.hasNext();) {
                    OnmsMonitoredService svc = (OnmsMonitoredService) svcIter.next();
                    if (svc.isDown()) {
                        color = AggregateStatus.ONE_SERVICE_DOWN;
                        return color;  //quick exit this mess
                    }
                }
            }
        }
        return color;
    }
    
    
    private Integer computeDownCount(Collection<OnmsNode> nodes) {
        int totalNodesDown = 0;
        
        for (OnmsNode node : nodes) {
            if (node.isDown()) {
                
                //FIXME: this is a hack to meet a requirement to build a URL
                //that takes you do a node page when a node is down in this
                //status class.
                m_foundDownNode = node;
                
                totalNodesDown += 1;
            }
        }
        return new Integer(totalNodesDown);
    }


    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public CategoryDao getCategoryDao() {
        return m_categoryDao;
    }

    public void setCategoryDao(CategoryDao categoryDao) {
        m_categoryDao = categoryDao;
    }

    public SurveillanceViewConfigDao getSurveillanceConfigDao() {
        return m_surveillanceConfigDao;
    }

    public void setSurveillanceConfigDao(SurveillanceViewConfigDao surveillanceConfigDao) {
        m_surveillanceConfigDao = surveillanceConfigDao;
    }

}
