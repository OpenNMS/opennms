package org.opennms.web.event.filter;

import org.opennms.web.filter.EqualsFilter;
import org.opennms.web.filter.SQLType;

public class NodeLocationFilter extends EqualsFilter<String> {
    public static final String TYPE = "nodelocation";
    private String m_location;

    public NodeLocationFilter(final String location) {
        super(TYPE, SQLType.STRING, "NODE.LOCATION", "node.location.locationName", location);
        m_location = location;
    }

    @Override
    public String getTextDescription() {
        return ("Node location is " + m_location);
    }

    @Override
    public String toString() {
        return ("<WebEventRepository.NodeLocationFilter: " + getDescription() + ">");
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof NodeLocationFilter)) return false;
        return (this.toString().equals(obj.toString()));
    }
}
