package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SurveillanceIntersection extends SurveillanceSet implements IsSerializable {
    
    private SurveillanceGroup m_rowGroup;
    private SurveillanceGroup m_columnGroup;
    private String m_data;
    private String m_status;
    
    /**
     * Default constructor used for serialization
     */
    public SurveillanceIntersection() {
        this(null, null);
    }
    
    public SurveillanceIntersection(SurveillanceGroup rowGroup, SurveillanceGroup columnGroup) {
        m_rowGroup = rowGroup;
        m_columnGroup = columnGroup;
    }

    public SurveillanceGroup getColumnGroup() {
        return m_columnGroup;
    }

    public void setColumnGroup(SurveillanceGroup columnGroup) {
        m_columnGroup = columnGroup;
    }

    public SurveillanceGroup getRowGroup() {
        return m_rowGroup;
    }

    public void setRowGroup(SurveillanceGroup rowGroup) {
        m_rowGroup = rowGroup;
    }
    
    public String toString() {
        return m_columnGroup.getLabel() + " " + m_rowGroup.getLabel();
    }

    public void visit(Visitor v) {
        v.visitIntersection(m_rowGroup, m_columnGroup);
    }

    public String getData() {
        return m_data == null ? "N/A" : m_data;
    }

    public void setData(String data) {
        m_data = data;
    }

    public String getStatus() {
        return m_status == null ? "Unknown" : m_status;
    }

    public void setStatus(String status) {
        m_status = status;
    }
    
    

}
