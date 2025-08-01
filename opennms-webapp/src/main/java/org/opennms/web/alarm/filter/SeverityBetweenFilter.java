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

import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.filter.BetweenFilter;
import org.opennms.web.filter.SQLType;

/**
 * <p>SeverityBetweenFilter class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class SeverityBetweenFilter extends BetweenFilter<OnmsSeverity> {
    /** Constant <code>TYPE="severityBetween"</code> */
    public static final String TYPE = "severityBetween";
    
    /**
     * <p>Constructor for SeverityBetweenFilter.</p>
     *
     * @param rangeBegin a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     * @param rangeEnd a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     */
    public SeverityBetweenFilter(OnmsSeverity rangeBegin, OnmsSeverity rangeEnd){
        super(TYPE, SQLType.SEVERITY, "SEVERITY", "severity", rangeBegin, rangeEnd);
    }
    
    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getTextDescription() {
        return "alarm severity between " + getSeverityLabel(getFirst()) + " and " + getSeverityLabel(getLast());
    }
    
    private String getSeverityLabel(final OnmsSeverity severity) {
        return severity.getLabel();
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return ("<AlarmCriteria.SeverityBetweenFilter: " + this.getDescription() + ">");
    }

}
