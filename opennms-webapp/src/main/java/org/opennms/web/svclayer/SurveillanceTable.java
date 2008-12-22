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
 * Created: September 9, 2006
 * Modifications:
 * 
 *  2007 Jul 24: Suppress warnings on unused constructor. - dj@opennms.org
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


package org.opennms.web.svclayer;

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
    
    public SurveillanceTable() {
        
    }
    
    // FIXME: Can we get rid of the the supress warnings?
    @SuppressWarnings("unchecked")
    public SurveillanceTable(int rows, int columns) {
        m_statusTable = new AggregateStatus[rows][columns];
        m_rowNodes = new HashSet[rows];
        m_columnNodes = new HashSet[columns];
        m_rowHeaders = new String[rows];
        m_columnHeaders = new String[columns];
    }
    
    public void setWebTable(SimpleWebTable webTable) {
    	m_webTable = webTable;
    }
    
    public SimpleWebTable getWebTable() {
    	return m_webTable;
    }
    
    public void setStatus(int row, int col, AggregateStatus status) {
        m_statusTable[row][col] = status;
    }
    
    public AggregateStatus getStatus(int row, int col) {
        return m_statusTable[row][col];
    }
    
    public int getRowCount() {
        return m_rowHeaders.length;
    }
    
    public int getColumnCount() {
        return m_columnHeaders.length;
    }
    
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
     * @param row
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
     * @param row
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

    public String getRowHeader(int row) {
        return m_rowHeaders[row];
    }
    
    public void setRowHeader(int row, String header) {
        m_rowHeaders[row] = header;
    }
    
    public String[] getRowHeaders() {
        return m_rowHeaders;
    }
    
    public List getRowHeaderList() {
        return asLinkedList(m_rowHeaders);
    }

    public void setColumnHeader(int col, String header) {
        m_columnHeaders[col] = header;
    }

    public String[] getColumnHeaders() {
        return m_columnHeaders;
    }
    
    public List getColumnHeaderList() {
        return asLinkedList(m_columnHeaders);
    }

    public String getLabel() {
        return m_label;
    }

    public void setLabel(String label) {
        m_label = label;
    }

    public AggregateStatus[][] getStatusTable() {
        return m_statusTable;
    }
    
    private List asLinkedList(String[] headers) {
        List<String> headerList = new LinkedList<String>();
        for (int i = 0; i < m_rowHeaders.length; i++) {
            headerList.add(headers[i]);
        }
        return headerList;
    }

    public Set<OnmsNode>[] getRowNodes() {
        return m_rowNodes;
    }

    public void setRowNodes(Set<OnmsNode>[] rowNodes) {
        m_rowNodes = rowNodes;
    }

    public void setRowHeaders(String[] rowHeaders) {
        m_rowHeaders = rowHeaders;
    }

    public Set<OnmsNode>[] getColumnNodes() {
        return m_columnNodes;
    }

    public void setColumnNodes(Set<OnmsNode>[] columnNodes) {
        m_columnNodes = columnNodes;
    }

    public void setColumnHeaders(String[] columnHeaders) {
        m_columnHeaders = columnHeaders;
    }

    public void setStatusTable(AggregateStatus[][] statusTable) {
        m_statusTable = statusTable;
    }

    public Set<OnmsNode> getNodesForRow(int row) {
        return m_rowNodes[row];
    }
    
    public void setNodesForRow(int row, Collection<OnmsNode>nodes) {
        m_rowNodes[row] = new HashSet<OnmsNode>(nodes);        
    }

    public Set<OnmsNode> getNodesForColumn(int col) {
        return m_columnNodes[col];
    }
    
    public void setNodesForColumn(int col, Collection<OnmsNode> columnNodes) {
        m_columnNodes[col] = new HashSet<OnmsNode>(columnNodes);
    }
    
}
