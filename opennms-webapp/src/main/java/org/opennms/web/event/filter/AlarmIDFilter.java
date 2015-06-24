/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.event.filter;

import org.opennms.web.filter.EqualsFilter;
import org.opennms.web.filter.SQLType;

/**
 * Encapsulates all node filtering functionality.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class AlarmIDFilter extends EqualsFilter<Integer> {
    /** Constant <code>TYPE="alarm"</code> */
    public static final String TYPE = "alarm";

    /**
     * <p>Constructor for AlarmIDFilter.</p>
     *
     * @param alarmId a int.
     */
    public AlarmIDFilter(int alarmId) {
        super(TYPE, SQLType.INT, "ALARMID", "alarm.id", alarmId);
    }

    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getTextDescription() {
        return ("event reduced by alarmID: " + getValue());
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return ("<WebEventRepository.AlarmIDFilter: " + this.getDescription() + ">");
    }

    /**
     * <p>getAlarmId</p>
     *
     * @return a int.
     */
    public int getAlarmId() {
        return getValue();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof AlarmIDFilter)) return false;
        return (this.toString().equals(obj.toString()));
    }
}
