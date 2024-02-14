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
import org.opennms.web.filter.NotEqualsFilter;
import org.opennms.web.filter.SQLType;

/**
 * Encapsulates negative severity filtering functionality, that is filtering OUT
 * this value instead of only filtering IN this value.
 */
public class NegativeSeverityFilter extends NotEqualsFilter<OnmsSeverity> {
    /** Constant <code>TYPE="severitynot"</code> */
    public static final String TYPE = "severitynot";

    public NegativeSeverityFilter(final OnmsSeverity severity) {
        super(TYPE, SQLType.SEVERITY, "ALARMS.SEVERITY", "severity", severity);
    }

    @Override
    public String getTextDescription() {
        return (TYPE + " is not " + getValue().getLabel());
    }

    @Override
    public String toString() {
        return ("<AlarmFactory.NegativeSeverityFilter: " + this.getDescription() + ">");
    }

    public int getSeverity() {
        return getValue().getId();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof NegativeSeverityFilter)) return false;
        return (this.toString().equals(obj.toString()));
    }
}
