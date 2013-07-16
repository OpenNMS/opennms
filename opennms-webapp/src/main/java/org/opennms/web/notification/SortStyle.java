/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.notification;

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
    RESPONDER("responder"),
    PAGETIME("pagetime"),
    RESPONDTIME("respondtime"),
    NODE("node"),
    INTERFACE("interface"),
    SERVICE("service"),
    ID("id"),
    REVERSE_RESPONDER("rev_responder"),
    REVERSE_PAGETIME("rev_pagetime"),
    REVERSE_RESPONDTIME("rev_respondtime"),
    REVERSE_NODE("rev_node"),
    REVERSE_INTERFACE("rev_interface"),
    REVERSE_SERVICE("rev_service"),
    REVERSE_ID("rev_id");

    /** Constant <code>DEFAULT_SORT_STYLE</code> */
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

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
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
     * @return a {@link org.opennms.web.notification.SortStyle} object.
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
