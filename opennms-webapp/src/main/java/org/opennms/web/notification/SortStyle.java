package org.opennms.web.notification;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

/** Convenience class to determine sort style of a query. */
public enum SortStyle {
    USER("user"),
    RESPONDER("responder"),
    PAGETIME("pagetime"),
    RESPONDTIME("respondtime"),
    NODE("node"),
    INTERFACE("interface"),
    SERVICE("service"),
    ID("id"),
    REVERSE_USER("rev_user"),
    REVERSE_RESPONDER("rev_responder"),
    REVERSE_PAGETIME("rev_pagetime"),
    REVERSE_RESPONDTIME("rev_respondtime"),
    REVERSE_NODE("rev_node"),
    REVERSE_INTERFACE("rev_interface"),
    REVERSE_SERVICE("rev_service"),
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
        case USER:
            clause = " ORDER BY USERID DESC";
            break;

        case REVERSE_USER:
            clause = " ORDER BY USERID ASC";
            break;

        case RESPONDER:
            clause = " ORDER BY ANSWEREDBY DESC";
            break;

        case REVERSE_RESPONDER:
            clause = " ORDER BY ANSWEREDBY ASC";
            break;

        case PAGETIME:
            clause = " ORDER BY PAGETIME DESC";
            break;

        case REVERSE_PAGETIME:
            clause = " ORDER BY PAGETIME ASC";
            break;

        case RESPONDTIME:
            clause = " ORDER BY RESPONDTIME DESC";
            break;

        case REVERSE_RESPONDTIME:
            clause = " ORDER BY RESPONDTIME ASC";
            break;

        case NODE:
            clause = " ORDER BY NODEID ASC";
            break;

        case REVERSE_NODE:
            clause = " ORDER BY NODEID DESC";
            break;

        case INTERFACE:
            clause = " ORDER BY INTERFACEID ASC";
            break;

        case REVERSE_INTERFACE:
            clause = " ORDER BY INTERFACEID DESC";
            break;

        case SERVICE:
            clause = " ORDER BY SERVICEID ASC";
            break;

        case REVERSE_SERVICE:
            clause = " ORDER BY SERVICEID DESC";
            break;

        case ID:
            clause = " ORDER BY NOTIFYID DESC";
            break;

        case REVERSE_ID:
            clause = " ORDER BY NOTIFYID ASC";
            break;

        default:
            throw new IllegalArgumentException("Unknown SortStyle: " + getName());
        }
        
        return clause;
    }

}