/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: February 20, 2007
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

package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * <p>SurveillanceData class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 * @since 1.6.12
 */
public class SurveillanceData implements IsSerializable {
    
    private boolean m_complete = false;
    
    private String m_name;
    
    private SurveillanceGroup[] m_columnGroups;
    private SurveillanceGroup[] m_rowGroups;
    
    private SurveillanceIntersection[][] m_cells;
    
    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }

    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * <p>isComplete</p>
     *
     * @return a boolean.
     */
    public boolean isComplete() {
        return m_complete;
    }

    /**
     * <p>setComplete</p>
     *
     * @param complete a boolean.
     */
    public void setComplete(boolean complete) {
        m_complete = complete;
    }

    /**
     * <p>getColumnCount</p>
     *
     * @return a int.
     */
    public int getColumnCount() {
        return m_columnGroups.length;
    }
    
    /**
     * <p>getRowCount</p>
     *
     * @return a int.
     */
    public int getRowCount() {
        return m_rowGroups.length;
    }
    
    /**
     * The heading of the column number 'colunmIndex' using zero based index
     *
     * @param columnIndex the index of the column
     * @return the heading for the column
     */
    public String getColumnHeading(int columnIndex) {
        return m_columnGroups[columnIndex].getLabel();
    }
    
    /**
     * The heading of row with index 'rowIndex' using zero based index
     *
     * @param rowIndex the index of the row
     * @return the heading for the row
     */
    public String getRowHeading(int rowIndex) {
        return m_rowGroups[rowIndex].getLabel();
    }

    /**
     * <p>getCell</p>
     *
     * @param row a int.
     * @param col a int.
     * @return a {@link org.opennms.dashboard.client.SurveillanceIntersection} object.
     */
    public SurveillanceIntersection getCell(int row, int col) {
        ensureData();
        return m_cells[row][col];
    }
    
    

    private void ensureData() {
        if (m_cells == null) {
            m_cells = new SurveillanceIntersection[getRowCount()][getColumnCount()];
            for(int row = 0; row < getRowCount(); row++) {
                for(int col = 0; col < getColumnCount(); col++) {
                    m_cells[row][col] = new SurveillanceIntersection(m_rowGroups[row], m_columnGroups[col]);
                }
            }
        }
    }
    
    /**
     * <p>setCell</p>
     *
     * @param row a int.
     * @param col a int.
     * @param cell a {@link org.opennms.dashboard.client.SurveillanceIntersection} object.
     */
    public void setCell(int row, int col, SurveillanceIntersection cell) {
        ensureData();
        cell.setRowGroup(m_rowGroups[col]);
        cell.setColumnGroup(m_columnGroups[col]);
        m_cells[row][col] = cell;
    }
    
    /**
     * <p>setCell</p>
     *
     * @param row a int.
     * @param col a int.
     * @param data a {@link java.lang.String} object.
     * @param status a {@link java.lang.String} object.
     */
    public void setCell(int row, int col, String data, String status) {
        ensureData();
        m_cells[row][col].setData(data);
        m_cells[row][col].setStatus(status);
    }
    /**
     * <p>setCell</p>
     *
     * @param row a int.
     * @param col a int.
     * @param value a {@link java.lang.String} object.
     */
    public void setCell(int row, int col, String value) {
        setCell(row, col, value, null);
    }
    
    /**
     * <p>getIntersection</p>
     *
     * @param row a int.
     * @param col a int.
     * @return a {@link org.opennms.dashboard.client.SurveillanceIntersection} object.
     */
    public SurveillanceIntersection getIntersection(int row, int col) {
        return m_cells[row][col];
    }

    /**
     * <p>getColumnGroups</p>
     *
     * @return an array of {@link org.opennms.dashboard.client.SurveillanceGroup} objects.
     */
    public SurveillanceGroup[] getColumnGroups() {
        return m_columnGroups;
    }

    /**
     * <p>setColumnGroups</p>
     *
     * @param columnGroups an array of {@link org.opennms.dashboard.client.SurveillanceGroup} objects.
     */
    public void setColumnGroups(SurveillanceGroup[] columnGroups) {
        m_columnGroups = columnGroups;
    }

    /**
     * <p>getRowGroups</p>
     *
     * @return an array of {@link org.opennms.dashboard.client.SurveillanceGroup} objects.
     */
    public SurveillanceGroup[] getRowGroups() {
        return m_rowGroups;
    }

    /**
     * <p>setRowGroups</p>
     *
     * @param rowGroups an array of {@link org.opennms.dashboard.client.SurveillanceGroup} objects.
     */
    public void setRowGroups(SurveillanceGroup[] rowGroups) {
        m_rowGroups = rowGroups;
    }
    
    

}
