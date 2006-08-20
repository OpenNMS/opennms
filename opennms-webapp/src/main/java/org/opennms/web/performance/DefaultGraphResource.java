package org.opennms.web.performance;

import java.util.Set;

public class DefaultGraphResource implements GraphResource {

    private String m_name;
    private Set<GraphAttribute> m_attributes;
    private String m_label;
    
    public DefaultGraphResource(String name, String label,
            Set<GraphAttribute> attributes) {
        m_name = name;
        m_label = label;
        m_attributes = attributes;
    }

    public String getName() {
        return m_name;
    }

    public String getLabel() {
        return m_label;
    }

    public Set<GraphAttribute> getAttributes() {
        return m_attributes;
    }

}
