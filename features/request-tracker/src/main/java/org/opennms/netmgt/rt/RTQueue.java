/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.rt;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

public class RTQueue implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 875365658091878358L;
	private long m_id;
    private String m_name;

    public RTQueue() {
    }

    public RTQueue(final long id, final String name) {
        m_id = id;
        m_name = name;
    }
    
    public long getId() {
        return m_id;
    }
    
    public void setId(final long id) {
        m_id = id;
    }
    
    public String getName() {
        return m_name;
    }
    
    public void setName(final String name) {
        m_name = name;
    }
    
    public boolean isAccessible() {
        return true;
    }

        @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", m_id)
            .append("name", m_name)
            .append("accessible", isAccessible())
            .toString();
    }
}
