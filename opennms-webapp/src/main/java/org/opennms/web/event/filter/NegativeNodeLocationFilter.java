package org.opennms.web.event.filter;

import org.opennms.web.filter.NotEqualOrNullFilter;
import org.opennms.web.filter.SQLType;

public class NegativeNodeLocationFilter extends NotEqualOrNullFilter<String> {
    public static final String TYPE = "nodelocationnot";
    private String m_location;

    public NegativeNodeLocationFilter(final String location) {
        super(TYPE, SQLType.STRING, "NODE.LOCATION", "node.location.locationName", location);
        m_location = location;
    }

    @Override
    public String getTextDescription() {
        return ("Node location is not " + m_location);
    }

    @Override
    public String toString() {
        return ("<WebEventRepository.NegativeNodeLocationFilter: " + getDescription() + ">");
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof NegativeNodeLocationFilter)) return false;
        return (this.toString().equals(obj.toString()));
    }
}
