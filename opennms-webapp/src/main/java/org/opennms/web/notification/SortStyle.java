/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
    LOCATION("location"),
    RESPONDER("responder"),
    PAGETIME("pagetime"),
    RESPONDTIME("respondtime"),
    NODE("node"),
    NODE_LOCATION("nodelocation"),
    INTERFACE("interface"),
    SERVICE("service"),
    ID("id"),
    SEVERITY("severity"),
    REVERSE_LOCATION("rev_location"),
    REVERSE_RESPONDER("rev_responder"),
    REVERSE_PAGETIME("rev_pagetime"),
    REVERSE_RESPONDTIME("rev_respondtime"),
    REVERSE_NODE("rev_node"),
    REVERSE_NODE_LOCATION("rev_nodelocation"),
    REVERSE_INTERFACE("rev_interface"),
    REVERSE_SERVICE("rev_service"),
    REVERSE_ID("rev_id"),
    REVERSE_SEVERITY("rev_severity");

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

        case SEVERITY:
            clause = " ORDER BY EVENT.EVENTSEVERITY DESC";
            break;

        case REVERSE_SEVERITY:
            clause = " ORDER BY EVENT.EVENTSEVERITY ASC";
            break;

        case LOCATION:
            clause = " ORDER BY MONITORINGSYSTEMS.LOCATION ASC";
            break;
            
        case REVERSE_LOCATION:
            clause = " ORDER BY MONITORINGSYSTEMS.LOCATION DESC";
            break;

        case NODE_LOCATION:
            clause = " ORDER BY NODE.LOCATION ASC";
            break;

        case REVERSE_NODE_LOCATION:
            clause = " ORDER BY NODE.LOCATION DESC";
            break;

        default:
            throw new IllegalArgumentException("Unknown SortStyle: " + getName());
        }

        return clause;
    }

}
