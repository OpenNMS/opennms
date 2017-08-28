/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.opennms.web.filter.OneArgFilter;
import org.opennms.web.filter.SQLType;

public class EventParmLikeFilter extends OneArgFilter<String> {

    public static final String TYPE = "parmmatchany";

    private final String key;

    public EventParmLikeFilter(String value) {
        this(EventParmLikeFilter.getKey(value), EventParmLikeFilter.getValue(value));
    }

    public EventParmLikeFilter(String key, String value) {
        super(TYPE, SQLType.STRING, "lastEventId", "lastEvent." + key, value);
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    @Override
    public String getSQLTemplate() {
        return " " + this.getSQLFieldName() + " IN (SELECT eventId FROM event_parameters WHERE name = '" + this.getKey() + "' AND value ILIKE '%s')";
    }

    @Override
    public Criterion getCriterion() {
        return Restrictions.sqlRestriction(" {alias}." + this.getSQLFieldName() + " IN (SELECT eventId FROM event_parameters WHERE name = ? AND value ILIKE ?)",
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
        return String.format("%s=\"%s\"", this.getKey(), this.getValue());
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
