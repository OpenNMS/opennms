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
import org.opennms.web.filter.OneArgFilter;
import org.opennms.web.filter.SQLType;

public class NegativeEventParmLikeFilter extends OneArgFilter<String> {

    public static final String TYPE = "noparmmatchany";

    private final String key;

    public NegativeEventParmLikeFilter(String value) {
        this(NegativeEventParmLikeFilter.getKey(value), NegativeEventParmLikeFilter.getValue(value));
    }

    public NegativeEventParmLikeFilter(String key, String value) {
        super(TYPE, SQLType.STRING, "lastEventId", "lastEvent." + key, value);
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    @Override
    public String getSQLTemplate() {
        return " " + this.getSQLFieldName() + " NOT IN (SELECT eventId FROM event_parameters WHERE name = '" + this.getKey() + "' AND value ILIKE '%s')";
    }

    @Override
    public Criterion getCriterion() {
        return Restrictions.sqlRestriction(" {alias}." + this.getSQLFieldName() + " NOT IN (SELECT eventId FROM event_parameters WHERE name = ? AND value ILIKE ?)",
                new Object[]{this.getKey(), this.getValue()},
                new Type[]{StringType.INSTANCE, StringType.INSTANCE});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof EventParmLikeFilter)) return false;
        return this.toString().equals(obj.toString());
    }

    @Override
    public String getDescription() {
        return String.format("%s is not \"%s\"", this.getKey(), this.getValue());
    }

    private static String getKey(final String s) {
        final int i = s.indexOf('=');
        if (i >= 0) {
            return s.substring(0, i);
        } else {
            return s;
        }
    }

    private static String getValue(final String s) {
        final int i = s.indexOf('=');
        if (i >= 0) {
            return s.substring(i + 1);
        } else {
            return "";
        }
    }
}
