package org.opennms.web.outage;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

public enum SortStyle {
    NODE("node"),
    INTERFACE("interface"),
    SERVICE("service"),
    IFLOSTSERVICE("iflostservice"),
    IFREGAINEDSERVICE("ifregainedservice"),
    ID("id"),
    REVERSE_NODE("rev_node"),
    REVERSE_INTERFACE("rev_interface"),
    REVERSE_SERVICE("rev_service"),
    REVERSE_IFLOSTSERVICE("rev_iflostservice"),
    REVERSE_IFREGAINEDSERVICE("rev_ifregainedservice"),
    REVERSE_ID("rev_id");

    public static final SortStyle DEFAULT_SORT_STYLE = SortStyle.ID;

    private static final Map<String, SortStyle> m_sortStylesString;

    private String m_shortName;

    static {
        m_sortStylesString = new HashMap<String, SortStyle>();
        for (SortStyle sortStyle : SortStyle.values()) {
            m_sortStylesString.put(sortStyle.getShortName(), sortStyle);

        }
    }

    private SortStyle(String shortName) {
        m_shortName = shortName;
    }

    public String toString() {
        return ("SortStyle." + getName());
    }

    public String getName() {
        return name();
    }

    public String getShortName() {
        return m_shortName;
    }

    public static SortStyle getSortStyle(String sortStyleString) {
        Assert.notNull(sortStyleString, "Cannot take null parameters.");

        return m_sortStylesString.get(sortStyleString.toLowerCase());
    }

    /**
     * Convenience method for getting the SQL <em>ORDER BY</em> clause related
     * to a given sort style.
     */
    protected String getOrderByClause() {
        String clause = null;

        switch (this) {
        case NODE:
            clause = " ORDER BY NODELABEL ASC";
            break;
        case REVERSE_NODE:
            clause = " ORDER BY NODELABEL DESC";
            break;
        case INTERFACE:
            clause = " ORDER BY IPADDR ASC";
            break;
        case REVERSE_INTERFACE:
            clause = " ORDER BY IPADDR DESC";
            break;
        case SERVICE:
            clause = " ORDER BY SERVICENAME ASC";
            break;
        case REVERSE_SERVICE:
            clause = " ORDER BY SERVICENAME DESC";
            break;
        case IFLOSTSERVICE:
            clause = " ORDER BY IFLOSTSERVICE DESC";
            break;
        case REVERSE_IFLOSTSERVICE:
            clause = " ORDER BY IFLOSTSERVICE ASC";
            break;
        case IFREGAINEDSERVICE:
            clause = " ORDER BY IFREGAINEDSERVICE DESC";
            break;
        case REVERSE_IFREGAINEDSERVICE:
            clause = " ORDER BY IFREGAINEDSERVICE ASC";
            break;
        case ID:
            clause = " ORDER BY OUTAGEID DESC";
            break;
        case REVERSE_ID:
            clause = " ORDER BY OUTAGEID ASC";
            break;
        default:
            throw new IllegalArgumentException("Unknown SortStyle: " + this);
        }
        return clause;
    }
}
