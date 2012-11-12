package org.opennms.features.topology.app.internal.gwt.client.service.filter;

public class PresenceFilter extends AttributeComparisonFilter {

    public PresenceFilter(String attribute) {
        super(attribute);
    }

    @Override
    protected boolean valueMatches(String value) {
        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("(").append(getAttribute()).append("=*)").toString();
    }

}
