package org.opennms.web.alarm;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * Convenience class to determine sort style of a query.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public enum SortStyle {
    SEVERITY("severity"),
    LASTEVENTTIME("lasteventtime"),
    FIRSTEVENTTIME("firsteventtime"),
    NODE("node"),
    INTERFACE("interface"),
    SERVICE("service"),
    POLLER("poller"),
    ID("id"),
    COUNT("count"),
    REVERSE_SEVERITY("rev_severity"),
    REVERSE_LASTEVENTTIME("rev_lasteventtime"),
    REVERSE_FIRSTEVENTTIME("rev_firsteventtime"),
    REVERSE_NODE("rev_node"),
    REVERSE_INTERFACE("rev_interface"),
    REVERSE_SERVICE("rev_service"),
    REVERSE_POLLER("rev_poller"),
    REVERSE_ID("rev_id"),
    REVERSE_COUNT("rev_count");

    /** Constant <code>m_sortStylesString</code> */
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

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return ("SortStyle." + getName());
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return name();
    }

    /**
     * <p>getShortName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getShortName() {
        return m_shortName;
    }

    /**
     * <p>getSortStyle</p>
     *
     * @param sortStyleString a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.alarm.SortStyle} object.
     */
    public static SortStyle getSortStyle(String sortStyleString) {
        Assert.notNull(sortStyleString, "Cannot take null parameters.");

        return m_sortStylesString.get(sortStyleString.toLowerCase());
    }

    /**
     * Convenience method for getting the SQL <em>ORDER BY</em> clause related
     * to a given sort style.
     *
     * @return a {@link java.lang.String} object.
     */
    protected String getOrderByClause() {
        String clause = null;
    
        switch (this) {
        case SEVERITY:
            clause = " ORDER BY SEVERITY DESC";
            break;
    
        case REVERSE_SEVERITY:
            clause = " ORDER BY SEVERITY ASC";
            break;
    
        case LASTEVENTTIME:
            clause = " ORDER BY LASTEVENTTIME DESC";
            break;
    
        case REVERSE_LASTEVENTTIME:
            clause = " ORDER BY LASTEVENTTIME ASC";
            break;
    
        case FIRSTEVENTTIME:
            clause = " ORDER BY FIRSTEVENTTIME DESC";
            break;
    
        case REVERSE_FIRSTEVENTTIME:
            clause = " ORDER BY FIRSTEVENTTIME ASC";
            break;
    
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
    
        case POLLER:
            clause = " ORDER BY EVENTDPNAME ASC";
            break;
    
        case REVERSE_POLLER:
            clause = " ORDER BY EVENTDPNAME DESC";
            break;
    
        case ID:
            clause = " ORDER BY ALARMID DESC";
            break;
    
        case REVERSE_ID:
            clause = " ORDER BY ALARMID ASC";
            break;
    
        case COUNT:
            clause = " ORDER BY COUNTER DESC";
            break;
    
        case REVERSE_COUNT:
            clause = " ORDER BY COUNTER ASC";
            break;
    
        default:
            throw new IllegalArgumentException("Unknown SortStyle: " + this);
        }
    
        return clause;
    }
}
