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
package org.opennms.web.outage;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * <p>SortStyle class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public enum SortStyle {
    NODE("node"),
    FOREIGNSOURCE("foreignsource"),
    INTERFACE("interface"),
    SERVICE("service"),
    IFLOSTSERVICE("iflostservice"),
    IFREGAINEDSERVICE("ifregainedservice"),
    ID("id"),
    LOCATION("location"),
    PERSPECTIVE("perspective"),
    REVERSE_NODE("rev_node"),
    REVERSE_FOREIGNSOURCE("rev_foreignsource"),
    REVERSE_INTERFACE("rev_interface"),
    REVERSE_SERVICE("rev_service"),
    REVERSE_IFLOSTSERVICE("rev_iflostservice"),
    REVERSE_IFREGAINEDSERVICE("rev_ifregainedservice"),
    REVERSE_ID("rev_id"),
    REVERSE_LOCATION("rev_location"),
    REVERSE_PERSPECTIVE("rev_perspective");


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
     * @return a {@link org.opennms.web.outage.SortStyle} object.
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
        case NODE:
            clause = " ORDER BY NODELABEL ASC";
            break;
        case REVERSE_NODE:
            clause = " ORDER BY NODELABEL DESC";
            break;
        case FOREIGNSOURCE:
            clause = " ORDER BY FOREIGNSOURCE ASC";
            break;
        case REVERSE_FOREIGNSOURCE:
            clause = " ORDER BY FOREIGNSOURCE DESC";
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
        case LOCATION:
            clause = " ORDER BY LOCATION DESC";
            break;
        case REVERSE_LOCATION:
            clause = " ORDER BY LOCATION ASC";
            break;
        case PERSPECTIVE:
            clause = " ORDER BY PERSPECTIVE DESC";
            break;
        case REVERSE_PERSPECTIVE:
            clause = " ORDER BY PERSPECTIVE ASC";
            break;
        default:
            throw new IllegalArgumentException("Unknown SortStyle: " + this);
        }
        return clause;
    }
}
