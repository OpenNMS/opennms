/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

public class SystemIdFilter extends EqualsFilter<String> {
    /** Constant <code>TYPE="systemId"</code> */
    public static final String TYPE = "systemId";
    private String m_systemId;

    /**
     * <p>Constructor for SystemIdFilter.</p>
     *
     * @param systemId a string.
     */
    public SystemIdFilter(String systemId) {
        super(TYPE, SQLType.STRING, "EVENTS.SYSTEMID", "distPoller.id", systemId);
        m_systemId = systemId;
    }

    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getTextDescription() {
        return (TYPE + "=" + m_systemId);
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return ("<WebEventRepository.SystemIdFilter: " + getDescription() + ">");
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof SystemIdFilter)) return false;
        return (this.toString().equals(obj.toString()));
    }

}
