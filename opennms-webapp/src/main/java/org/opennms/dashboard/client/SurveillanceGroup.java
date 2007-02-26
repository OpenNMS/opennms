package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SurveillanceGroup extends SurveillanceSet implements IsSerializable {
    
    private String m_label;
    private String m_id;
    private boolean m_column;
    
    public SurveillanceGroup() {
        this(null, null, false);
    }
    
    public SurveillanceGroup(String id, String label, boolean isColumn) {
        m_id = id;
        m_label = label;
        m_column = isColumn;
    }

    public String getId() {
        return m_id;
    }

    public String getLabel() {
        return m_label;
    }

    public void setId(String id) {
        m_id = id;
    }

    public void setLabel(String label) {
        m_label = label;
    }

    public boolean isColumn() {
        return m_column;
    }

    public void setColumn(boolean isColumn) {
        m_column = isColumn;
    }
    
    public String toString() {
        return m_label;
    }

    public void visit(Visitor v) {
        v.visitGroup(this);
    }
}
