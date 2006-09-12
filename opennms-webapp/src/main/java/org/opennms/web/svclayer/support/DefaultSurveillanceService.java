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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.svclayer.AggregateStatus;
import org.opennms.web.svclayer.ProgressMonitor;
import org.opennms.web.svclayer.SimpleWebTable;
import org.opennms.web.svclayer.SurveillanceService;
import org.opennms.web.svclayer.SurveillanceTable;
import org.opennms.web.svclayer.dao.SurveillanceViewConfigDao;

public class DefaultSurveillanceService implements SurveillanceService {

    private NodeDao m_nodeDao;
    private CategoryDao m_categoryDao;
    private SurveillanceViewConfigDao m_surveillanceConfigDao;
    private OnmsNode m_foundDownNode;
    
    class CellStatusStrategy {

		Collection<OnmsNode> getNodesInCategories(Set<OnmsCategory> categories) {
			return m_nodeDao.findAllByCategoryList(categories);
		}
    	
    }
    
    public SimpleWebTable createSurveillanceTable() {
        return createSurveillanceTable("default", null);
    }
    
    public class SurveillanceView {
        private SurveillanceViewConfigDao m_surveillanceConfigDao;
        private CategoryDao m_categoryDao;
        private String m_viewName;
        private View m_view;
        
        public SurveillanceView(String viewName, SurveillanceViewConfigDao surveillanceConfigDao, CategoryDao categoryDao) {
        	m_surveillanceConfigDao = surveillanceConfigDao;
        	m_categoryDao = categoryDao;
        	m_viewName = viewName;
        	m_view = m_surveillanceConfigDao.getView(viewName);
        }
        
        public int getRowCount() {
        	return m_view.getRows().getRowDefCount();
        }
        
        public int getColumnCount() {
        	return m_view.getColumns().getColumnDefCount();
        }
        
        @SuppressWarnings("unchecked")
		public Set<OnmsCategory> getCategoriesForRow(int rowIndex) {
        	return getOnmsCategoriesFromViewCategories(getRowDef(rowIndex).getCategoryCollection());
        }

		private RowDef getRowDef(int rowIndex) {
			return m_view.getRows().getRowDef(rowIndex);
		}
        
        @SuppressWarnings("unchecked")
		public Set<OnmsCategory> getCategoriesForColumn(int colIndex) {
        	return getOnmsCategoriesFromViewCategories(getColumnDef(colIndex).getCategoryCollection());
        }

		private ColumnDef getColumnDef(int colIndex) {
			return m_view.getColumns().getColumnDef(colIndex);
		}
        
        private Set<OnmsCategory> getOnmsCategoriesFromViewCategories(Collection<Category> viewCats) {
            Set<OnmsCategory> categories = new HashSet<OnmsCategory>();
            for (Category viewCat : viewCats) {
            	
                categories.add(m_categoryDao.findByName(viewCat.getName()));
            }
            
            return categories;
        }
        
        public String getRowLabel(int rowIndex) {
        	return getRowDef(rowIndex).getLabel();
        }
        
        public String getColumnLabel(int colIndex) {
        	return getColumnDef(colIndex).getLabel();
        }
        
    }

    /**
     * Creates a custom table object containing intersected rows and
     * columns and categories.
     */
    public SimpleWebTable createSurveillanceTable(String surveillanceViewName, ProgressMonitor progressMonitor) {

        View view = m_surveillanceConfigDao.getView(surveillanceViewName);
        
        SurveillanceView sView = new SurveillanceView(surveillanceViewName, m_surveillanceConfigDao, m_categoryDao);
        
        final Rows rows = view.getRows();
        final Columns columns = view.getColumns();
        
        progressMonitor.setPhaseCount(rows.getRowDefCount()+columns.getColumnDefCount()+2);
        
        /*
         * Initialize a status table 
         */
        SimpleWebTable webTable = new SimpleWebTable();
        webTable.setTitle(view.getName());
        
        webTable.addColumn("Nodes Down", "simpleWebTableHeader");
        
        // set up the column headings
        for(int colIndex = 0; colIndex < sView.getColumnCount(); colIndex++) {
            webTable.addColumn(sView.getColumnLabel(colIndex), "simpleWebTableHeader");
        }
        

        // build the set of nodes for each cell
        
        CellStatusStrategy strategy = new CellStatusStrategy();
        
        AggregateStatus[][] cellStatus = calculateCellStatus(sView, progressMonitor, strategy);
        
        progressMonitor.beginNextPhase("Calculating Status Values");
        
        for(int rowIndex = 0; rowIndex < sView.getRowCount(); rowIndex++) {
            
            webTable.newRow();
            webTable.addCell(sView.getRowLabel(rowIndex), "simpleWebTableRowLabel");
            

            for(int colIndex = 0; colIndex < sView.getColumnCount(); colIndex++) {

				AggregateStatus aggStatus = cellStatus[rowIndex][colIndex];
				
				SimpleWebTable.Cell cell = webTable.addCell(aggStatus.getDownEntityCount()+" of "+aggStatus.getTotalEntityCount(), aggStatus.getStatus());

                if (aggStatus.getDownEntityCount() > 0) {
					cell.setLink(createNodePageUrl(aggStatus));
                    m_foundDownNode = null; //what a hack
                }
            }
                
        }
        progressMonitor.finished(webTable);
        
        return webTable;
    }

	private AggregateStatus[][] calculateCellStatus(SurveillanceView sView, ProgressMonitor progressMonitor, CellStatusStrategy strategy) {
		
        List<Collection<OnmsNode>> nodesByRowIndex = new ArrayList<Collection<OnmsNode>>();
        List<Collection<OnmsNode>> nodesByColIndex = new ArrayList<Collection<OnmsNode>>();

        /*
         * Iterate of the requested view's configuration (row's and columns) and set an aggreated status into each table
         * cell.
         */
        for(int rowIndex = 0; rowIndex < sView.getRowCount(); rowIndex++) {
        	progressMonitor.beginNextPhase("Loading Nodes for "+sView.getRowLabel(rowIndex));
            Collection<OnmsNode> nodesForRow = strategy.getNodesInCategories(sView.getCategoriesForRow(rowIndex));
            nodesByRowIndex.add(rowIndex, nodesForRow);
        }

        for(int colIndex = 0; colIndex < sView.getColumnCount(); colIndex++) {
        	progressMonitor.beginNextPhase("Loading Nodes for "+sView.getColumnLabel(colIndex));
            Collection<OnmsNode> nodesForCol = strategy.getNodesInCategories(sView.getCategoriesForColumn(colIndex));
            nodesByColIndex.add(colIndex, nodesForCol);
        }
        
        AggregateStatus[][] cellStatus = new AggregateStatus[sView.getRowCount()][sView.getColumnCount()];

        
        progressMonitor.beginNextPhase("Intersecting Rows and Columns");
        
        for(int rowIndex = 0; rowIndex < sView.getRowCount(); rowIndex++) {
            
            Collection<OnmsNode> nodesForRow = nodesByRowIndex.get(rowIndex);

            for(int colIndex = 0; colIndex < sView.getColumnCount(); colIndex++) {

            	Collection<OnmsNode> nodesForCol = nodesByColIndex.get(colIndex);

                Set<OnmsNode> cellNodes = new HashSet<OnmsNode>(nodesForRow);
				cellNodes.retainAll(nodesForCol);
				
				cellStatus[rowIndex][colIndex] = new AggregateStatus(cellNodes);
				
				
                
            }
                
        }
		return cellStatus;
	}

	/*
     * This creates a relative url to the node page and sets the node parameter
     * FIXME: this code should move to the jsp after the status table is enhanced to support
     * this requirement.
     */
    private String createNodePageUrl(AggregateStatus aggStatus) {
    	if (m_foundDownNode != null) {
            return "element/node.jsp?node="+m_foundDownNode.getId();
        }
        return null;
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
