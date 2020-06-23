/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
