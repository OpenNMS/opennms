/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.util.Collection;

/**
 * <p>WebGroup class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class WebGroup {

    private final String m_name;
    private Collection<WebUser> m_users;
    
    /**
     * <p>Constructor for WebGroup.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public WebGroup(String name) {
        m_name = name;
    }
    
    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return ""+getName();
    }
    
    /**
     * <p>getUsers</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<WebUser> getUsers() {
        return m_users;
    }
    
    /**
     * <p>setUsers</p>
     *
     * @param users a {@link java.util.Collection} object.
     */
    protected void setUsers(Collection<WebUser> users) {
        m_users = users;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WebGroup) {
            WebGroup u = (WebGroup)obj;
            return m_name.equals(u.m_name);
        }
        return false;
    }

    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    @Override
    public int hashCode() {
        return m_name.hashCode();
    }


}
