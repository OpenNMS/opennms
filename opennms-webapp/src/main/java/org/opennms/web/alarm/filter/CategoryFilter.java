/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

public class CategoryFilter extends OneArgFilter<String> {
    public static final String TYPE = "category";

    public CategoryFilter(final String category) {
        super(TYPE, SQLType.STRING, "NODEID", "nodeid", category);
    }

    public String getSQLTemplate() {
        return " " + getSQLFieldName() + " IN (SELECT CN.NODEID FROM CATEGORY_NODE CN, CATEGORIES C WHERE CN.CATEGORYID = C.CATEGORYID AND C.CATEGORYNAME = %s) ";
    }

    @Override
    public Criterion getCriterion() {
        return Restrictions.sqlRestriction(" {alias}." + this.getSQLFieldName() + " IN (SELECT CN.NODEID FROM CATEGORY_NODE CN, CATEGORIES C WHERE CN.CATEGORYID = C.CATEGORYID AND C.CATEGORYNAME = ?)",
                new Object[]{this.getValue()},
                new Type[]{StringType.INSTANCE});
    }

    @Override
    public String getTextDescription() {
        return ("category is \"" + getValue() + "\"");
    }

    @Override
    public String toString() {
        return ("<AlarmFactory.CategoryFilter: " + this.getDescription() + ">");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof CategoryFilter)) return false;
        return (this.toString().equals(obj.toString()));
    }
}
