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

import org.opennms.web.filter.NormalizedAcknowledgeType;
import org.springframework.util.Assert;

/**
 * Convenience class to determine what sort of alarms to include in a query.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public enum AcknowledgeType {
    ACKNOWLEDGED("ack"), UNACKNOWLEDGED("unack"), BOTH("both");

    /** Constant <code>s_ackTypesString</code> */
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

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return "AcknowledgeType." + getName();
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
     * this sort style.
     *
     * @return a {@link java.lang.String} object.
     */
    protected String getAcknowledgeTypeClause() {
        switch (this) {
        case ACKNOWLEDGED:
            return " EVENTACKUSER IS NOT NULL";
    
        case UNACKNOWLEDGED:
            return " EVENTACKUSER IS NULL";
    
        case BOTH:
            return " (EVENTACKUSER IS NULL OR EVENTACKUSER IS NOT NULL)";
            
        default:
            throw new IllegalArgumentException("Cannot get clause for AcknowledgeType " + this);
        }
    }

    public NormalizedAcknowledgeType toNormalizedAcknowledgeType() {
        return NormalizedAcknowledgeType.createFrom(this);
    }

    /**
     * <p>getAcknowledgeType</p>
     *
     * @param ackTypeString a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.event.AcknowledgeType} object.
     */
    public static AcknowledgeType getAcknowledgeType(String ackTypeString) {
        Assert.notNull(ackTypeString, "Cannot take null parameters.");

        return s_ackTypesString.get(ackTypeString.toLowerCase());
    }
}
