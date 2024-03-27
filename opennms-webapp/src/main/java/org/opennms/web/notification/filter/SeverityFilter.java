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
package org.opennms.web.notification.filter;

import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.filter.EqualsFilter;
import org.opennms.web.filter.SQLType;




/**
 * Encapsulates severity filtering functionality.
 *
 * @author agalue
 */
public class SeverityFilter extends EqualsFilter<Integer> {
    /** Constant <code>TYPE="severity"</code> */
    public static final String TYPE = "severity";

    /**
     * <p>Constructor for SeverityFilter.</p>
     *
     * @param severity a int.
     */
    public SeverityFilter(int severity) {
        super(TYPE, SQLType.INT, "EVENTSEVERITY", "event.eventSeverity", severity);
    }

    /**
     * <p>Constructor for SeverityFilter.</p>
     *
     * @param severity a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     */
    public SeverityFilter(OnmsSeverity severity) {
        this(severity.getId());
    }

    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getTextDescription() {
        return (TYPE + "=" + OnmsSeverity.get(getSeverity()).getLabel());
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return ("<WebNotificationRepository.SeverityFilter: " + getDescription() + ">");
    }

    /**
     * <p>getSeverity</p>
     *
     * @return a int.
     */
    public int getSeverity() {
        return getValue();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof SeverityFilter)) return false;
        return (this.toString().equals(obj.toString()));
    }
}
