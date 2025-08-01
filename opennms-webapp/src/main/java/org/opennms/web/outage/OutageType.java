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
    @Override
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
