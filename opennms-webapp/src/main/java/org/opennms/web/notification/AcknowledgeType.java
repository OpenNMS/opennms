package org.opennms.web.notification;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * Convenience class to determine what sort of notices to include in a
 * query.
 */
public enum AcknowledgeType {
    ACKNOWLEDGED("ack"), UNACKNOWLEDGED("unack"), BOTH("both");
    
    private static final Map<String, AcknowledgeType> s_ackTypesString;
    
    private String m_shortName;

    static {
        s_ackTypesString = new HashMap<String, AcknowledgeType>();

        for (AcknowledgeType ackType : AcknowledgeType.values()) {
            s_ackTypesString.put(ackType.getShortName(), ackType);
        }
    }

    private AcknowledgeType(String shortName) {
        m_shortName = shortName;
    }

    public String toString() {
        return "AcknowledgeType." + getName();
    }

    public String getName() {
        return name();
    }

    public String getShortName() {
        return m_shortName;
    }
    
    /**
     * Convenience method for getting the SQL <em>ORDER BY</em> clause related
     * this sort style.
     */
    protected String getAcknowledgeTypeClause() {
        switch (this) {
        case ACKNOWLEDGED:
            return " RESPONDTIME IS NOT NULL";
    
        case UNACKNOWLEDGED:
            return " RESPONDTIME IS NULL";
    
        case BOTH:
            return " (RESPONDTIME IS NULL OR RESPONDTIME IS NOT NULL)";
            
        default:
            throw new IllegalArgumentException("Cannot get clause for AcknowledgeType " + this);
        }
    }

    public static AcknowledgeType getAcknowledgeType(String ackTypeString) {
        Assert.notNull(ackTypeString, "Cannot take null parameters.");

        return s_ackTypesString.get(ackTypeString.toLowerCase());
    }
}