package org.opennms.netmgt.model;

public class RrdGraphAttribute implements OnmsAttribute {

    private String m_name;
    
    public RrdGraphAttribute(String name) {
        m_name = name;
    }

    public String getName() {
        return m_name;
    }

}
