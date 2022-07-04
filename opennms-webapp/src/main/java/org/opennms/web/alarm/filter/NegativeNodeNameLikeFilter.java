/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
        return " alarmid NOT IN (SELECT alarmid FROM alarms JOIN node ON alarms.nodeid=node.nodeid WHERE node.nodelabel ILIKE '%s') ";
    }

    /** {@inheritDoc} */
    @Override
    public Criterion getCriterion() {
        return Restrictions.sqlRestriction(" {alias}.alarmid NOT IN (SELECT alarmid FROM alarms JOIN node ON alarms.nodeid=node.nodeid WHERE node.nodelabel ILIKE ?)",
                new Object[]{this.getValue()}, new Type[]{StringType.INSTANCE});
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
