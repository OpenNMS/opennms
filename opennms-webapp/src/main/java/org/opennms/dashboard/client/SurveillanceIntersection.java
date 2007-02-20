package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SurveillanceIntersection implements IsSerializable {
    
    private SurveillanceGroup m_rowGroup;
    private SurveillanceGroup m_columnGroup;
    
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
        return m_rowGroup.getLabel() + " X " + m_columnGroup.getLabel();
    }
    
    

}
