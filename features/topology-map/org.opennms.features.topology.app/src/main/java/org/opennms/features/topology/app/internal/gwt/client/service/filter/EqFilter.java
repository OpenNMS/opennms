package org.opennms.features.topology.app.internal.gwt.client.service.filter;

public class EqFilter extends AttributeComparisonFilter {

    private String m_value;

    public EqFilter(String attribute, String value) {
        super(attribute);
        m_value = value;
    }
    
    

    @Override
    protected boolean valueMatches(String value) {
        return m_value.equals(value);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("(");
        buf.append(getAttribute());
        buf.append("=");
        buf.append(escaped(m_value));
        buf.append(")");
        return buf.toString();
        
    }
    
    private String escaped(String value) {
        return value
            .replace("\\", "\\\\")
            .replace("*", "\\*")
            .replace(")", "\\)")
            .replace("(", "\\(")
            ;
    }

}
