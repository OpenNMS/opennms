/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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
import org.opennms.web.filter.NotEqualsFilter;
import org.opennms.web.filter.SQLType;

/**
 * Encapsulates negative severity filtering functionality, that is filtering OUT
 * this value instead of only filtering IN this value.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class NegativeSeverityFilter extends NotEqualsFilter<Integer> {
    
    /** Constant <code>TYPE="severitynot"</code> */
    public static final String TYPE = "severitynot";

    /**
     * <p>Constructor for NegativeSeverityFilter.</p>
     *
     * @param severity a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     */
    public NegativeSeverityFilter(OnmsSeverity severity) {
        super(TYPE, SQLType.INT, "SEVERITY", "severityId", severity.getId());
    }

    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTextDescription() {
        return ("severity is not " + OnmsSeverity.get(getValue()).getLabel());
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return ("<AlarmFactory.NegativeSeverityFilter: " + this.getDescription() + ">");
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
    public boolean equals(Object obj) {
        return (this.toString().equals(obj.toString()));
    }
}
