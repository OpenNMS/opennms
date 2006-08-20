package org.opennms.web.performance;

public class RrdGraphAttribute implements GraphAttribute {

    private String m_name;
    
    public RrdGraphAttribute(String name) {
        m_name = name;
    }

    public String getName() {
        return m_name;
    }

}
