package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SurveillanceGroup implements IsSerializable {
    
    private String m_label;
    private String m_id;
    
    public SurveillanceGroup() {
        this(null, null);
    }
    
    public SurveillanceGroup(String id, String label) {
        m_id = id;
        m_label = label;
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
    
    public String toString() {
        return m_label;
    }
    

}
