package org.opennms.features.topology.app.internal.gwt.client.service.filter;

import java.util.Map;

public abstract class AttributeComparisonFilter extends AbstractFilter {

    private String m_attribute;
    
    protected AttributeComparisonFilter(String attribute) {
        m_attribute = attribute;
    }
    
    protected String getAttribute() {
        return m_attribute;
    }

    @Override
    public boolean match(Map<String, String> properties) {
        if (properties == null || !properties.containsKey(m_attribute)) {
            return false;
        } else {
            return valueMatches(properties.get(m_attribute));
        }
    }

    abstract protected boolean valueMatches(String value);

    @Override
    abstract public String toString();

}
