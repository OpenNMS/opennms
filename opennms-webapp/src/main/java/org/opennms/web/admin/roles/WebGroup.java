/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2005-2006, 2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.web.admin.roles;

import java.util.Collection;

public class WebGroup {

    private String m_name;
    private Collection m_users;
    
    public WebGroup(String name) {
        m_name = name;
    }
    
    public String getName() {
        return m_name;
    }
    
    public String toString() {
        return ""+getName();
    }
    
    public Collection getUsers() {
        return m_users;
    }
    
    protected void setUsers(Collection users) {
        m_users = users;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof WebGroup) {
            WebGroup u = (WebGroup)obj;
            return m_name.equals(u.m_name);
        }
        return false;
    }

    public int hashCode() {
        return m_name.hashCode();
    }


}
