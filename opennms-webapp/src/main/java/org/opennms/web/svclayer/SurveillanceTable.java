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

package org.opennms.web.svclayer;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class manages a table of AggregateStatus values.
 * 
 * @author david
 *
 */
public class SurveillanceTable {
    
    String m_label = null;
    AggregateStatus[][] m_statusTable = null;
    String[] m_rowHeaders = null;
    String[] m_columnHeaders = null;
    
    public SurveillanceTable() {
        
    }
    
    public SurveillanceTable(int rows, int columns) {
        m_statusTable = new AggregateStatus[rows][columns];
        m_rowHeaders = new String[rows];
        m_columnHeaders = new String[columns];
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
        for (int i = 0; i < m_statusTable.length; i++) {
            AggregateStatus[] statusRow = m_statusTable[i];
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
        for(AggregateStatus s : row) {
            orderedRow.add(s);
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
        for(AggregateStatus s : m_statusTable[row]) {
            orderedRow.add(s);
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
    
    public List getColHeaderList() {
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


    
}
