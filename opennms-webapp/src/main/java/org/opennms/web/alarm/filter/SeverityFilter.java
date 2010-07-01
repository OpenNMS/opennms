//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Oct 04: Severity -> OnmsSeverity name change. - dj@opennms.org
// 2008 Sep 26: Use new Severity enum. - dj@opennms.org
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.alarm.filter;

import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.filter.EqualsFilter;
import org.opennms.web.filter.SQLType;

/**
 * Encapsulates severity filtering functionality.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class SeverityFilter extends EqualsFilter<Integer> {
    /** Constant <code>TYPE="severity"</code> */
    public static final String TYPE = "severity";

    /**
     * <p>Constructor for SeverityFilter.</p>
     *
     * @param severity a {@link org.opennms.netmgt.model.OnmsSeverity} object.
     */
    public SeverityFilter(OnmsSeverity severity) {
        super(TYPE, SQLType.INT, "SEVERITY", "severityId", severity.getId());
    }

    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTextDescription() {
        return (TYPE + "=" + OnmsSeverity.get(getValue()).getLabel());
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return ("<AlarmFactory.SeverityFilter: " + this.getDescription() + ">");
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
