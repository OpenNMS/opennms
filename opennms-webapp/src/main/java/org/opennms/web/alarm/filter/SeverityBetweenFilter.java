/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
