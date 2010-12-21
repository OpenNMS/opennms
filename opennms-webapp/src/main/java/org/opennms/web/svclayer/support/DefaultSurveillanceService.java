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
 * 
 * Created: September 10, 2006
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.config.surveillanceViews.Category;
import org.opennms.netmgt.config.surveillanceViews.ColumnDef;
import org.opennms.netmgt.config.surveillanceViews.RowDef;
import org.opennms.netmgt.config.surveillanceViews.View;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.SurveillanceViewConfigDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.api.Util;
import org.opennms.netmgt.model.SurveillanceStatus;
import org.opennms.web.svclayer.AggregateStatus;
import org.opennms.web.svclayer.ProgressMonitor;
import org.opennms.web.svclayer.SimpleWebTable;
import org.opennms.web.svclayer.SurveillanceService;
import org.opennms.web.svclayer.support.DefaultSurveillanceService.CellStatusStrategy;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.util.StringUtils;

/**
 * <p>DefaultSurveillanceService class.</p>
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class DefaultSurveillanceService implements SurveillanceService {

    private NodeDao m_nodeDao;
    private CategoryDao m_categoryDao;
    private SurveillanceViewConfigDao m_surveillanceConfigDao;
    
    interface CellStatusStrategy {
        public SurveillanceStatus[][] calculateCellStatus(SurveillanceView sView, ProgressMonitor progressMonitor);

        public int getPhaseCount(SurveillanceView sView);
    }

    class DefaultCellStatusStrategy implements CellStatusStrategy {

        private Collection<OnmsNode> getNodesInCategories(Set<OnmsCategory> categories) {
            return m_nodeDao.findAllByCategoryList(categories);
        }

        public SurveillanceStatus[][] calculateCellStatus(SurveillanceView sView, ProgressMonitor progressMonitor) {

            List<Collection<OnmsNode>> nodesByRowIndex = new ArrayList<Collection<OnmsNode>>();
            List<Collection<OnmsNode>> nodesByColIndex = new ArrayList<Collection<OnmsNode>>();

            /*
             * Iterate of the requested view's configuration (row's and columns) and set an aggreated status into each table
             * cell.
             */
            for(int rowIndex = 0; rowIndex < sView.getRowCount(); rowIndex++) {
                progressMonitor.beginNextPhase("Loading nodes for row '"+sView.getRowLabel(rowIndex) + "'");
                Collection<OnmsNode> nodesForRow = getNodesInCategories(sView.getCategoriesForRow(rowIndex));
                nodesByRowIndex.add(rowIndex, nodesForRow);
            }

            for(int colIndex = 0; colIndex < sView.getColumnCount(); colIndex++) {
                progressMonitor.beginNextPhase("Loading nodes for column '"+sView.getColumnLabel(colIndex) + "'");
                Collection<OnmsNode> nodesForCol = getNodesInCategories(sView.getCategoriesForColumn(colIndex));
                nodesByColIndex.add(colIndex, nodesForCol);
            }

            SurveillanceStatus[][] cellStatus = new SurveillanceStatus[sView.getRowCount()][sView.getColumnCount()];


            progressMonitor.beginNextPhase("Intersecting rows and columns");

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

        public int getPhaseCount(SurveillanceView sView) {
            return sView.getRowCount()+sView.getColumnCount()+1;
        }

    }

    class LowMemCellStatusStrategy implements CellStatusStrategy {
        
        private String toString(Collection<OnmsCategory> categories) {
            StringBuilder buf = new StringBuilder();
            
            buf.append("{");
            
            boolean first = true;
            for(OnmsCategory cat : categories) {
                if (first) {
                    first = !first;
                } else {
                    buf.append(", ");
                }
                buf.append(cat.getName());
            }
            
            buf.append("}");
            
            return buf.toString();
        }
        

        public SurveillanceStatus[][] calculateCellStatus(SurveillanceView sView, ProgressMonitor progressMonitor) {

            SurveillanceStatus[][] cellStatus = new SurveillanceStatus[sView.getRowCount()][sView.getColumnCount()];

            for(int rowIndex = 0; rowIndex < sView.getRowCount(); rowIndex++) {

                for(int colIndex = 0; colIndex < sView.getColumnCount(); colIndex++) {
                    
                    Collection<OnmsCategory> rowCategories = sView.getCategoriesForRow(rowIndex);
                    Collection<OnmsCategory> columnCategories = sView.getCategoriesForColumn(colIndex);
                    
                    progressMonitor.beginNextPhase(String.format("Finding nodes in %s intersect %s", toString(rowCategories), toString(columnCategories)));

                    Collection<OnmsNode> cellNodes = m_nodeDao.findAllByCategoryLists(rowCategories, columnCategories);

                    cellStatus[rowIndex][colIndex] = new AggregateStatus(cellNodes);

                }

            }
            return cellStatus;
        }


        public int getPhaseCount(SurveillanceView sView) {
            return sView.getRowCount()*sView.getColumnCount();
        }

    }

    class VeryLowMemCellStatusStrategy implements CellStatusStrategy {
        
        private String toString(Collection<OnmsCategory> categories) {
            StringBuilder buf = new StringBuilder();
            
            buf.append("{");
            
            boolean first = true;
            for(OnmsCategory cat : categories) {
                if (first) {
                    first = !first;
                } else {
                    buf.append(", ");
                }
                buf.append(cat.getName());
            }
            
            buf.append("}");
            
            return buf.toString();
        }
        

        public SurveillanceStatus[][] calculateCellStatus(SurveillanceView sView, ProgressMonitor progressMonitor) {

            SurveillanceStatus[][] cellStatus = new SurveillanceStatus[sView.getRowCount()][sView.getColumnCount()];

            for(int rowIndex = 0; rowIndex < sView.getRowCount(); rowIndex++) {

                for(int colIndex = 0; colIndex < sView.getColumnCount(); colIndex++) {
                    
                    Collection<OnmsCategory> rowCategories = sView.getCategoriesForRow(rowIndex);
                    Collection<OnmsCategory> columnCategories = sView.getCategoriesForColumn(colIndex);
                    
                    progressMonitor.beginNextPhase(String.format("Finding status for nodes in %s intersect %s", toString(rowCategories), toString(columnCategories)));
                    
                    SurveillanceStatus status = m_nodeDao.findSurveillanceStatusByCategoryLists(rowCategories, columnCategories);
                    
                    cellStatus[rowIndex][colIndex] = status;

                }

            }
            return cellStatus;
        }


        public int getPhaseCount(SurveillanceView sView) {
            return sView.getRowCount()*sView.getColumnCount();
        }

    }

    /**
     * <p>createSurveillanceTable</p>
     *
     * @return a {@link org.opennms.web.svclayer.SimpleWebTable} object.
     */
    public SimpleWebTable createSurveillanceTable() {
        return createSurveillanceTable("default", new ProgressMonitor());
    }

    public class SurveillanceView {
        private SurveillanceViewConfigDao m_surveillanceConfigDao;
        private CategoryDao m_categoryDao;
        private View m_view;

        public SurveillanceView(String viewName, SurveillanceViewConfigDao surveillanceConfigDao, CategoryDao categoryDao) {
            m_surveillanceConfigDao = surveillanceConfigDao;
            m_categoryDao = categoryDao;
            m_view = m_surveillanceConfigDao.getView(viewName);
        }

        public int getRowCount() {
            return m_view.getRows().getRowDefCount();
        }

        public int getColumnCount() {
            return m_view.getColumns().getColumnDefCount();
        }

        public Set<OnmsCategory> getCategoriesForRow(int rowIndex) {
            return getOnmsCategoriesFromViewCategories(getRowDef(rowIndex).getCategoryCollection());
        }

        private RowDef getRowDef(int rowIndex) {
            return m_view.getRows().getRowDef(rowIndex);
        }

        public Set<OnmsCategory> getCategoriesForColumn(int colIndex) {
            return getOnmsCategoriesFromViewCategories(getColumnDef(colIndex).getCategoryCollection());
        }

        private ColumnDef getColumnDef(int colIndex) {
            return m_view.getColumns().getColumnDef(colIndex);
        }

        private Set<OnmsCategory> getOnmsCategoriesFromViewCategories(Collection<Category> viewCats) {
            Set<OnmsCategory> categories = new HashSet<OnmsCategory>();
            for (Category viewCat : viewCats) {

                OnmsCategory category = m_categoryDao.findByName(viewCat.getName());
                if (category == null)
                    throw new ObjectRetrievalFailureException(OnmsCategory.class, viewCat.getName(), "Unable to locate OnmsCategory named: "+viewCat.getName()+" as specified in the surveillance view configuration file", null);
                categories.add(category);
            }

            return categories;
        }

        public String getRowLabel(int rowIndex) {
            return getRowDef(rowIndex).getLabel();
        }

        public String getColumnLabel(int colIndex) {
            return getColumnDef(colIndex).getLabel();
        }

        public String getColumnReportCategory(int colIndex) {
            return getColumnDef(colIndex).getReportCategory();
        }

        public String getRowReportCategory(int rowIndex) {
            return getRowDef(rowIndex).getReportCategory();
        }

    }

    /**
     * {@inheritDoc}
     *
     * Creates a custom table object containing intersected rows and
     * columns and categories.
     */
    public SimpleWebTable createSurveillanceTable(String surveillanceViewName, ProgressMonitor progressMonitor) {
        
        CellStatusStrategy strategy = getCellStatusStrategy();

        surveillanceViewName = (surveillanceViewName == null ? m_surveillanceConfigDao.getDefaultView().getName() : surveillanceViewName);
        View view = m_surveillanceConfigDao.getView(surveillanceViewName);

        SurveillanceView sView = new SurveillanceView(surveillanceViewName, m_surveillanceConfigDao, m_categoryDao);

        progressMonitor.setPhaseCount(strategy.getPhaseCount(sView) + 1);

        /*
         * Initialize a status table 
         */
        SimpleWebTable webTable = new SimpleWebTable();
        webTable.setTitle(view.getName());

        webTable.addColumn("Nodes Down", "simpleWebTableHeader");

        // set up the column headings
        for(int colIndex = 0; colIndex < sView.getColumnCount(); colIndex++) {
            webTable.addColumn(sView.getColumnLabel(colIndex), "simpleWebTableHeader")
            .setLink(computeReportCategoryLink(sView.getColumnReportCategory(colIndex)));
        }


        // build the set of nodes for each cell


        SurveillanceStatus[][] cellStatus = strategy.calculateCellStatus(sView, progressMonitor);

        progressMonitor.beginNextPhase("Calculating Status Values");

        for(int rowIndex = 0; rowIndex < sView.getRowCount(); rowIndex++) {

            webTable.newRow();
            webTable.addCell(sView.getRowLabel(rowIndex),
                    "simpleWebTableRowLabel").setLink(computeReportCategoryLink(sView.getRowReportCategory(rowIndex)));


            for(int colIndex = 0; colIndex < sView.getColumnCount(); colIndex++) {

                SurveillanceStatus survStatus = cellStatus[rowIndex][colIndex];

                System.err.println(String.format("Text: %s, Style %s", survStatus.getDownEntityCount()+" of "+survStatus.getTotalEntityCount(), survStatus.getStatus()));
                SimpleWebTable.Cell cell = webTable.addCell(survStatus.getDownEntityCount()+" of "+survStatus.getTotalEntityCount(), survStatus.getStatus());

                if (survStatus.getDownEntityCount() > 0) {
                    cell.setLink(createNodePageUrl(sView, colIndex, rowIndex));
                }
            }

        }
        progressMonitor.finished(webTable);

        return webTable;
    }

    private CellStatusStrategy getCellStatusStrategy() {
        return new VeryLowMemCellStatusStrategy();
    }

    private String computeReportCategoryLink(String reportCategory) {
        String link = null;

        if (reportCategory != null) {
            link = "rtc/category.jsp?category=" + Util.encode(reportCategory);
        }
        return link;
    }

    /*
     * This creates a relative url to the node list page and sets the category
     * parameters to show the categories for this cell.
     * FIXME: this code should move to the jsp after the status table is
     * enhanced to support this requirement.
     */
    private String createNodePageUrl(SurveillanceView view, int colIndex, int rowIndex) {
        Set<OnmsCategory> columns = view.getCategoriesForColumn(colIndex); 
        Set<OnmsCategory> rows = view.getCategoriesForRow(rowIndex);

        List<String> params = new ArrayList<String>(columns.size() + rows.size());
        for (OnmsCategory category : columns) {
            params.add("category1=" + Util.encode(category.getName()));
        }
        for (OnmsCategory category : rows) {
            params.add("category2=" + Util.encode(category.getName()));
        }
        params.add("nodesWithDownAggregateStatus=true");
        return "element/nodeList.htm"
        + "?"
        + StringUtils.collectionToDelimitedString(params, "&");
    }

    /**
     * <p>getNodeDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.NodeDao} object.
     */
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    /**
     * <p>setNodeDao</p>
     *
     * @param nodeDao a {@link org.opennms.netmgt.dao.NodeDao} object.
     */
    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    /**
     * <p>getCategoryDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.CategoryDao} object.
     */
    public CategoryDao getCategoryDao() {
        return m_categoryDao;
    }

    /**
     * <p>setCategoryDao</p>
     *
     * @param categoryDao a {@link org.opennms.netmgt.dao.CategoryDao} object.
     */
    public void setCategoryDao(CategoryDao categoryDao) {
        m_categoryDao = categoryDao;
    }

    /**
     * <p>getSurveillanceConfigDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.SurveillanceViewConfigDao} object.
     */
    public SurveillanceViewConfigDao getSurveillanceConfigDao() {
        return m_surveillanceConfigDao;
    }

    /**
     * <p>setSurveillanceConfigDao</p>
     *
     * @param surveillanceConfigDao a {@link org.opennms.netmgt.dao.SurveillanceViewConfigDao} object.
     */
    public void setSurveillanceConfigDao(SurveillanceViewConfigDao surveillanceConfigDao) {
        m_surveillanceConfigDao = surveillanceConfigDao;
    }

    /** {@inheritDoc} */
    public String getHeaderRefreshSeconds(String viewName) {
        viewName = (viewName == null ? m_surveillanceConfigDao.getDefaultView().getName() : viewName);
        return m_surveillanceConfigDao.getView(viewName).getRefreshSeconds();
    }

    /** {@inheritDoc} */
    public boolean isViewName(String viewName) {
        View view;
        view = ( viewName == null ? m_surveillanceConfigDao.getDefaultView() : m_surveillanceConfigDao.getView(viewName) );
        return (view == null) ? false : true;
    }

    /**
     * <p>getViewNames</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getViewNames() {
        List<String> viewNames = new ArrayList<String>(m_surveillanceConfigDao.getViews().getViewCount());
        for (View view : getViewCollection()) {
            viewNames.add(view.getName());
        }
        Collections.sort(viewNames);
        return viewNames;
    }
    
    private Collection<View> getViewCollection() {
        return m_surveillanceConfigDao.getViews().getViewCollection();
    }

}
