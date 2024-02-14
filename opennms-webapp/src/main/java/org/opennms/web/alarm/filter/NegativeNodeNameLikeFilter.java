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
package org.opennms.web.alarm.filter;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.opennms.web.filter.NoSubstringFilter;

public class NegativeNodeNameLikeFilter extends NoSubstringFilter {
    /** Constant <code>TYPE="nodenamelikeNOT"</code> */
    public static final String TYPE = "nodenamelikeNOT";

    /**
     * <p>Constructor for NodeNameLikeFilter.</p>
     *
     * @param substring a {@link String} object.
     */
    public NegativeNodeNameLikeFilter(String substring) {
        super(TYPE, "NODELABEL", "node.label", substring);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getSQLTemplate() {
        return " ALARMID NOT IN (SELECT ALARMID FROM ALARMS JOIN NODE ON ALARMS.NODEID=NODE.NODEID WHERE NODE.NODELABEL ILIKE %s) ";
    }

    /** {@inheritDoc} */
    @Override
    public Criterion getCriterion() {
        return Restrictions.sqlRestriction(" {alias}.alarmid NOT IN (SELECT alarmid FROM alarms JOIN node ON alarms.nodeid=node.nodeid WHERE node.nodelabel ILIKE ?)",
                new Object[]{getBoundValue(this.getValue())}, new Type[]{StringType.INSTANCE});
    }

    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link String} object.
     */
    @Override
    public String getTextDescription() {
        return ("Node name not containing \"" + getValue() + "\"");
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link String} object.
     */
    @Override
    public String toString() {
        return ("<NegativeNodeNameLikeFilter: " + this.getDescription() + ">");
    }

    /**
     * <p>getSubstring</p>
     *
     * @return a {@link String} object.
     */
    public String getSubstring() {
        return getValue();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof NegativeNodeNameLikeFilter)) return false;
        return (this.toString().equals(obj.toString()));
    }
    
}
