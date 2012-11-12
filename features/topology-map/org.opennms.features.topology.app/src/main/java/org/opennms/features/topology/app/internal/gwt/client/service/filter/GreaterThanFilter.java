package org.opennms.features.topology.app.internal.gwt.client.service.filter;

public class GreaterThanFilter extends AttributeComparisonFilter {

    private String m_value;

    public GreaterThanFilter(String attribute, String value) {
        super(attribute);
        m_value = value;
    }

    @Override
    protected boolean valueMatches(String value) {
        try {
            return Double.parseDouble(value) >= Double.parseDouble(m_value);
        } catch (NumberFormatException e) {
            return value.compareToIgnoreCase(m_value) >= 0;
        }
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("(").append(getAttribute()).append(">=").append(m_value).append(")");
        return buf.toString();
    }

}
