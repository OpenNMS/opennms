/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc. All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

package org.opennms.web.event;

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
    TIME("time"),
    NODE("node"),
    INTERFACE("interface"),
    SERVICE("service"),
    POLLER("poller"),
    ID("id"),
    REVERSE_SEVERITY("rev_severity"),
    REVERSE_TIME("rev_time"),
    REVERSE_NODE("rev_node"),
    REVERSE_INTERFACE("rev_interface"),
    REVERSE_SERVICE("rev_service"),
    REVERSE_POLLER("rev_poller"),
    REVERSE_ID("rev_id");

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
     * @return a {@link org.opennms.web.event.SortStyle} object.
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
            clause = " ORDER BY EVENTSEVERITY DESC";
            break;
    
        case REVERSE_SEVERITY:
            clause = " ORDER BY EVENTSEVERITY ASC";
            break;
    
        case TIME:
            clause = " ORDER BY EVENTTIME DESC";
            break;
    
        case REVERSE_TIME:
            clause = " ORDER BY EVENTTIME ASC";
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
            clause = " ORDER BY EVENTID DESC";
            break;
    
        case REVERSE_ID:
            clause = " ORDER BY EVENTID ASC";
            break;
    
        default:
            throw new IllegalArgumentException("Unknown SortStyle: " + this);
        }
    
        return clause;
    }
}
