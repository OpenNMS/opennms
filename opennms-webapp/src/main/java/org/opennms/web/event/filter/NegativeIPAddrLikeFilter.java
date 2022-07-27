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

package org.opennms.web.event.filter;

import org.opennms.web.filter.NegativeIPLikeFilter;

/**
 * Encapsulates all interface filtering functionality.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class NegativeIPAddrLikeFilter extends NegativeIPLikeFilter {
    /** Constant <code>TYPE="iplikeNot"</code> */
    public static final String TYPE = "iplikeNot";

    /**
     * <p>Constructor for NegativeIPAddrLikeFilter.</p>
     *
     * @param ipLikePattern a {@link String} object.
     */
    public NegativeIPAddrLikeFilter(String ipLikePattern) {
        super(TYPE, "IPADDR", "ipAddr", ipLikePattern);
    }

    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link String} object.
     */
    @Override
    public String getTextDescription() {
        return ("IP Address not like \"" + getValue() + "\"");
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link String} object.
     */
    @Override
    public String toString() {
        return ("<NegativeIPAddrLikeFilter: " + this.getDescription() + ">");
    }

    /**
     * <p>getIpLikePattern</p>
     *
     * @return a {@link String} object.
     */
    public String getIpLikePattern() {
        return getValue();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof NegativeIPAddrLikeFilter)) return false;
        return (this.toString().equals(obj.toString()));
    }
}
