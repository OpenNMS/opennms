package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SurveillanceData implements IsSerializable {
    
    private boolean m_complete = false;
    
    private SurveillanceGroup[] m_columnGroups;
    private SurveillanceGroup[] m_rowGroups;
    
    private String[][] m_data;

    public boolean isComplete() {
        return m_complete;
    }

    public void setComplete(boolean complete) {
        m_complete = complete;
    }

    public String[][] getData() {
        return m_data;
    }

    public void setData(String[][] data) {
        m_data = data;
    }

    public int getColumnCount() {
        return m_columnGroups.length;
    }
    
    public int getRowCount() {
        return m_rowGroups.length;
    }
    
    /**
     * The heading of the column number 'colunmIndex' using zero based index
     * @param columnIndex the index of the column
     * @return the heading for the column
     */
    public String getColumnHeading(int columnIndex) {
        return m_columnGroups[columnIndex].getLabel();
    }
    
    /**
     * The heading of row with index 'rowIndex' using zero based index
     * @param rowIndex the index of the row
     * @return the heading for the row
     */
    public String getRowHeading(int rowIndex) {
        return m_rowGroups[rowIndex].getLabel();
    }

    public String getCell(int row, int col) {
        ensureData();
        return m_data[row][col] == null ? "N/A" :  m_data[row][col];
    }

    private void ensureData() {
        if (m_data == null) {
            m_data = new String[getRowCount()][getColumnCount()];
        }
    }

    public void setCell(int row, int col, String value) {
        ensureData();
        m_data[row][col] = value;
    }
    
    public SurveillanceIntersection getIntersection(int row, int col) {
        return new SurveillanceIntersection(m_rowGroups[row], m_columnGroups[col]);
    }

    public SurveillanceGroup[] getColumnGroups() {
        return m_columnGroups;
    }

    public void setColumnGroups(SurveillanceGroup[] columnGroups) {
        m_columnGroups = columnGroups;
    }

    public SurveillanceGroup[] getRowGroups() {
        return m_rowGroups;
    }

    public void setRowGroups(SurveillanceGroup[] rowGroups) {
        m_rowGroups = rowGroups;
    }
    
    

}
