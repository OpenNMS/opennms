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
    NODE_LOCATION("nodelocation"),
    INTERFACE("interface"),
    SERVICE("service"),
    POLLER("poller"),
    ID("id"),
    SYSTEMID("systemid"),
    LOCATION("location"),
    ALARMID("alarmid"),
    REVERSE_SEVERITY("rev_severity"),
    REVERSE_TIME("rev_time"),
    REVERSE_NODE("rev_node"),
    REVERSE_NODE_LOCATION("rev_nodelocation"),
    REVERSE_INTERFACE("rev_interface"),
    REVERSE_SERVICE("rev_service"),
    REVERSE_POLLER("rev_poller"),
    REVERSE_ID("rev_id"),
    REVERSE_SYSTEMID("rev_systemid"),
    REVERSE_LOCATION("rev_location"),
    REVERSE_ALARMID("rev_alarm");

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
    
        case NODE_LOCATION:
            clause = " ORDER BY NODE.LOCATION ASC, NODELABEL ASC";
            break;
            
        case REVERSE_NODE_LOCATION:
            clause = " ORDER BY NODE.LOCATION DESC, NODELABEL ASC";
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
            clause = " ORDER BY EVENTID DESC, EVENTTIME DESC"; // Include the eventtime in the order by clause to help the query planner - see NMS-10506
            break;
    
        case REVERSE_ID:
            clause = " ORDER BY EVENTID ASC, EVENTTIME DESC"; // Include the eventtime in the order by clause to help the query planner - see NMS-10506
            break;

        case SYSTEMID:
            clause = " ORDER BY SYSTEMID DESC";
            break;

        case REVERSE_SYSTEMID:
            clause = " ORDER BY SYSTEMID ASC";
            break;

        case LOCATION:
            clause = " ORDER BY LOCATION DESC";
            break;

        case REVERSE_LOCATION:
            clause = " ORDER BY LOCATION ASC";
            break;

        case ALARMID:
            clause = " ORDER BY ALARM_ID DESC";
            break;

        case REVERSE_ALARMID:
            clause = " ORDER BY ALARM_ID ASC";
            break;

        default:
            throw new IllegalArgumentException("Unknown SortStyle: " + this);
        }
    
        return clause;
    }
}
