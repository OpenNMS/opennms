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

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.BooleanType;
import org.opennms.web.filter.Filter;
import org.opennms.web.filter.SQLType;

import com.google.common.base.Objects;

public class SituationFilter implements Filter {
    public static final String TYPE = "situation";

    private final boolean value;

    public SituationFilter(final boolean situation) {
        this.value = situation;
    }

    @Override
    public String getSql() {
        return " (SELECT COUNT(*)>0 FROM ALARM_SITUATIONS WHERE ALARM_SITUATIONS.SITUATION_ID = ALARMID) = " + (value ? "TRUE" : "FALSE") + " ";
    }

    @Override
    public String getParamSql() {
        return " (SELECT COUNT(*)>0 FROM ALARM_SITUATIONS WHERE ALARM_SITUATIONS.SITUATION_ID = ALARMID) = ? ";
    }

    @Override
    public int bindParam(PreparedStatement ps, int parameterIndex) throws SQLException {
        SQLType.BOOLEAN.bindParam(ps, parameterIndex, value);
        return 1;
    }

    @Override
    public String getDescription() {
        return String.format("%s=%s", TYPE, this.value);

    }

    @Override
    public String getTextDescription() {
        if (value) {
            return "Alarm is a situation";
        } else {
            return "Alarm is not a situation";
        }
    }

    @Override
    public Criterion getCriterion() {
        return Restrictions.sqlRestriction("(SELECT COUNT(*)>0 FROM ALARM_SITUATIONS WHERE ALARM_SITUATIONS.SITUATION_ID = {alias}.alarmId) = ? ", value, BooleanType.INSTANCE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SituationFilter that = (SituationFilter) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
