/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.svclayer.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.OnmsNode;

/**
 *
 * This class manages a table of AggregateStatus values.
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class SurveillanceTable {
    
    String m_label = null;
    AggregateStatus[][] m_statusTable = null;
    Set<OnmsNode>[] m_rowNodes = null;
    Set<OnmsNode>[] m_columnNodes = null;
    String[] m_rowHeaders = null;
    String[] m_columnHeaders = null;
	private SimpleWebTable m_webTable;
    
    /**
     * <p>Constructor for SurveillanceTable.</p>
     */
    public SurveillanceTable() {
        
    }
    
    // FIXME: Can we get rid of the the @SuppressWarnings?
    /**
     * <p>Constructor for SurveillanceTable.</p>
     *
     * @param rows a int.
     * @param columns a int.
     */
    @SuppressWarnings("unchecked")
    public SurveillanceTable(int rows, int columns) {
        m_statusTable = new AggregateStatus[rows][columns];
        m_rowNodes = new HashSet[rows];
        m_columnNodes = new HashSet[columns];
        m_rowHeaders = new String[rows];
        m_columnHeaders = new String[columns];
    }
    
    /**
     * <p>setWebTable</p>
     *
     * @param webTable a {@link org.opennms.web.svclayer.model.SimpleWebTable} object.
     */
    public void setWebTable(SimpleWebTable webTable) {
    	m_webTable = webTable;
    }
    
    /**
     * <p>getWebTable</p>
     *
     * @return a {@link org.opennms.web.svclayer.model.SimpleWebTable} object.
     */
    public SimpleWebTable getWebTable() {
    	return m_webTable;
    }
    
    /**
     * <p>setStatus</p>
     *
     * @param row a int.
     * @param col a int.
     * @param status a {@link org.opennms.web.svclayer.model.AggregateStatus} object.
     */
    public void setStatus(int row, int col, AggregateStatus status) {
        m_statusTable[row][col] = status;
    }
    
    /**
     * <p>getStatus</p>
     *
     * @param row a int.
     * @param col a int.
     * @return a {@link org.opennms.web.svclayer.model.AggregateStatus} object.
     */
    public AggregateStatus getStatus(int row, int col) {
        return m_statusTable[row][col];
    }
    
    /**
     * <p>getRowCount</p>
     *
     * @return a int.
     */
    public int getRowCount() {
        return m_rowHeaders.length;
    }
    
    /**
     * <p>getColumnCount</p>
     *
     * @return a int.
     */
    public int getColumnCount() {
        return m_columnHeaders.length;
    }
    
    /**
     * <p>getStatusRow</p>
     *
     * @param row a int.
     * @return an array of {@link org.opennms.web.svclayer.model.AggregateStatus} objects.
     */
    public AggregateStatus[] getStatusRow(int row) {
        return m_statusTable[row];
    }
    
    /**
     * This method returns an ordered list of Aggregate Status rows
     * and columns the internal table.  Usefull when needing to
     * work with collections and not arrays.
     *
     * @return List<AggregateStatus[]>
     */
    public List<List<AggregateStatus>> getOrderedRows() {
        List<List<AggregateStatus>> orderedRows = new LinkedList<List<AggregateStatus>>();
        for (AggregateStatus[] statusRow : m_statusTable) {
            orderedRows.add(getColumnOrderedRow(statusRow));
        }
        return orderedRows;
    }
    
    /**
     * Handy method for return a map with the key being the row header and the
     * value being an ordered collection of aggregate stati.
     *
     * @return Map<String, List<AggregateStatus>> map
     */
    public Map<String, List<AggregateStatus>> getColumnOrderedRowsWithHeaders() {
        
        Map<String, List<AggregateStatus>> map = new LinkedHashMap<String, List<AggregateStatus>>();
        
        for (int i = 0; i < m_rowHeaders.length; i++) {
            map.put(m_rowHeaders[i], getColumnOrderedRow(m_statusTable[i]));
        }
        
        return map;
    }
    
    /**
     * This method returns on ordered list of Aggregate Status columns
     * based on the array argument.
     *
     * @param row an array of {@link org.opennms.web.svclayer.model.AggregateStatus} objects.
     * @return List<AggregateStatus> orderedRow
     */
    public List<AggregateStatus> getColumnOrderedRow(AggregateStatus[] row) {
        List<AggregateStatus> orderedRow = new LinkedList<AggregateStatus>();
        /*
         * we received an array (1 row) of aggregate status columns, loop over
         * the array in order and add each status to the list.
         */
        for (AggregateStatus element : row) {
            orderedRow.add(element);
        }
        return orderedRow;
    }
    
    /**
     * This method returns on ordered list of Aggregate Status columns
     * based on the row specified from the status internal table.
     *
     * @param row a int.
     * @return List<AggregateStatus> orderedRow
     */
    public List<AggregateStatus> getColumnOrderedRow(int row) {
        List<AggregateStatus> orderedRow = new LinkedList<AggregateStatus>();
        /*
         * we received a row index into the status table to retrieve an array (1 row)
         * of aggregate status columns, loop over
         * the array in order and add each status to the list.
         */
        for (int i = 0; i < m_statusTable[row].length; i++) {
            orderedRow.add(m_statusTable[row][i]);
        }
        return orderedRow;
    }

    /**
     * <p>getRowHeader</p>
     *
     * @param row a int.
     * @return a {@link java.lang.String} object.
     */
    public String getRowHeader(int row) {
        return m_rowHeaders[row];
    }
    
    /**
     * <p>setRowHeader</p>
     *
     * @param row a int.
     * @param header a {@link java.lang.String} object.
     */
    public void setRowHeader(int row, String header) {
        m_rowHeaders[row] = header;
    }
    
    /**
     * <p>getRowHeaders</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getRowHeaders() {
        return m_rowHeaders;
    }
    
    /**
     * <p>getRowHeaderList</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getRowHeaderList() {
        return asLinkedList(m_rowHeaders);
    }

    /**
     * <p>setColumnHeader</p>
     *
     * @param col a int.
     * @param header a {@link java.lang.String} object.
     */
    public void setColumnHeader(int col, String header) {
        m_columnHeaders[col] = header;
    }

    /**
     * <p>getColumnHeaders</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getColumnHeaders() {
        return m_columnHeaders;
    }
    
    /**
     * <p>getColumnHeaderList</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getColumnHeaderList() {
        return asLinkedList(m_columnHeaders);
    }

    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLabel() {
        return m_label;
    }

    /**
     * <p>setLabel</p>
     *
     * @param label a {@link java.lang.String} object.
     */
    public void setLabel(String label) {
        m_label = label;
    }

    /**
     * <p>getStatusTable</p>
     *
     * @return an array of {@link org.opennms.web.svclayer.model.AggregateStatus} objects.
     */
    public AggregateStatus[][] getStatusTable() {
        return m_statusTable;
    }
    
    private List<String> asLinkedList(String[] headers) {
        List<String> headerList = new LinkedList<String>();
        for (int i = 0; i < m_rowHeaders.length; i++) {
            headerList.add(headers[i]);
        }
        return headerList;
    }

    /**
     * <p>getRowNodes</p>
     *
     * @return an array of {@link java.util.Set} objects.
     */
    public Set<OnmsNode>[] getRowNodes() {
        return m_rowNodes;
    }

    /**
     * <p>setRowNodes</p>
     *
     * @param rowNodes an array of {@link java.util.Set} objects.
     */
    public void setRowNodes(Set<OnmsNode>[] rowNodes) {
        m_rowNodes = rowNodes;
    }

    /**
     * <p>setRowHeaders</p>
     *
     * @param rowHeaders an array of {@link java.lang.String} objects.
     */
    public void setRowHeaders(String[] rowHeaders) {
        m_rowHeaders = rowHeaders;
    }

    /**
     * <p>getColumnNodes</p>
     *
     * @return an array of {@link java.util.Set} objects.
     */
    public Set<OnmsNode>[] getColumnNodes() {
        return m_columnNodes;
    }

    /**
     * <p>setColumnNodes</p>
     *
     * @param columnNodes an array of {@link java.util.Set} objects.
     */
    public void setColumnNodes(Set<OnmsNode>[] columnNodes) {
        m_columnNodes = columnNodes;
    }

    /**
     * <p>setColumnHeaders</p>
     *
     * @param columnHeaders an array of {@link java.lang.String} objects.
     */
    public void setColumnHeaders(String[] columnHeaders) {
        m_columnHeaders = columnHeaders;
    }

    /**
     * <p>setStatusTable</p>
     *
     * @param statusTable an array of {@link org.opennms.web.svclayer.model.AggregateStatus} objects.
     */
    public void setStatusTable(AggregateStatus[][] statusTable) {
        m_statusTable = statusTable;
    }

    /**
     * <p>getNodesForRow</p>
     *
     * @param row a int.
     * @return a {@link java.util.Set} object.
     */
    public Set<OnmsNode> getNodesForRow(int row) {
        return m_rowNodes[row];
    }
    
    /**
     * <p>setNodesForRow</p>
     *
     * @param row a int.
     * @param nodes a {@link java.util.Collection} object.
     */
    public void setNodesForRow(int row, Collection<OnmsNode>nodes) {
        m_rowNodes[row] = new HashSet<OnmsNode>(nodes);        
    }

    /**
     * <p>getNodesForColumn</p>
     *
     * @param col a int.
     * @return a {@link java.util.Set} object.
     */
    public Set<OnmsNode> getNodesForColumn(int col) {
        return m_columnNodes[col];
    }
    
    /**
     * <p>setNodesForColumn</p>
     *
     * @param col a int.
     * @param columnNodes a {@link java.util.Collection} object.
     */
    public void setNodesForColumn(int col, Collection<OnmsNode> columnNodes) {
        m_columnNodes[col] = new HashSet<OnmsNode>(columnNodes);
    }
    
}
