package org.opennms.web.outage;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * <p>OutageType class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public enum OutageType {
    CURRENT("current"),
    RESOLVED("resolved"),
    BOTH("both"),
    SUPPRESSED("suppressed");

    /** Constant <code>s_outageTypesString</code> */
    private static final Map<String, OutageType> s_outageTypesString;

    private String m_shortName;

    static {
        s_outageTypesString = new HashMap<String, OutageType>();

        for (OutageType outageType : OutageType.values()) {
            s_outageTypesString.put(outageType.getShortName(), outageType);
        }
    }

    private OutageType(String shortName) {
        m_shortName = shortName;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return "Outage." + getName();
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
     * Convenience method for getting the SQL <em>ORDER BY</em> clause related
     * to a given sort style.
     *
     * @return a {@link java.lang.String} object.
     */
    protected String getClause() {
        String clause = null;

        switch (this) {
        case CURRENT:
            clause = " IFREGAINEDSERVICE IS NULL AND SUPPRESSTIME IS NULL ";
            break;
        case RESOLVED:
            clause = " IFREGAINEDSERVICE IS NOT NULL AND SUPPRESSTIME IS NULL ";
            break;
        case SUPPRESSED:
            clause = " ((SUPPRESSEDTIME IS NOT NULL) AND (SUPPRESSTIME > NOW())) AND IFREGAINEDSERVICE IS NULL";
            break;
        case BOTH:
            clause = " TRUE AND SUPPRESSTIME IS NULL "; // will return both!
            break;
        default:
            throw new IllegalArgumentException("Unknown OutageType: " + this.getName());
        }

        return clause;
    }

    /**
     * <p>getOutageType</p>
     *
     * @param outageTypeString a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.outage.OutageType} object.
     */
    public static OutageType getOutageType(String outageTypeString) {
        Assert.notNull(outageTypeString, "Cannot take null parameters.");

        return s_outageTypesString.get(outageTypeString.toLowerCase());
    }
}
