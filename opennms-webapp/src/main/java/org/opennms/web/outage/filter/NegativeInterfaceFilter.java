/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.outage.filter;

import org.opennms.web.filter.NotEqualOrNullFilter;
import org.opennms.web.filter.SQLType;

/**
 * Encapsulates all interface filtering functionality.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class NegativeInterfaceFilter extends NotEqualOrNullFilter<String> {
    /** Constant <code>TYPE="intfnot"</code> */
    public static final String TYPE = "intfnot";

    /**
     * <p>Constructor for NegativeInterfaceFilter.</p>
     *
     * @param ipAddress a {@link java.lang.String} object.
     */
    public NegativeInterfaceFilter(String ipAddress) {
        super(TYPE, SQLType.STRING, "OUTAGES.IPADDR", "ipInterface.ipAddress", ipAddress);
    }

    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getTextDescription() {
        return ("interface is not " + getIpAddress());
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return ("<NegativeInterfaceFilter: " + this.getDescription() + ">");
    }

    /**
     * <p>getIpAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddress() {
        return getValue();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        return (this.toString().equals(obj.toString()));
    }
}
