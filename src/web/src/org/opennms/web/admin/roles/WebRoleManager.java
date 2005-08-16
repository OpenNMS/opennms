//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.web.admin.roles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.groups.Role;

public class WebRoleManager {
    
    private GroupManager m_groupManager;
    
    class ManagedRole extends WebRole {
        Role m_role;
        ManagedRole(Role role) {
           m_role = role;
           super.setName(role.getName());
           super.setDescription(role.getDescription());
        }
    }

    public WebRoleManager(GroupManager groupManager) {
        m_groupManager = groupManager;
    }
    
    public Collection getRoles() {
        Collection roles = m_groupManager.getRoles();
        List webRoles = new ArrayList(roles.size());
        for (Iterator it = roles.iterator(); it.hasNext();) {
            Role role = (Role) it.next();
            webRoles.add(new ManagedRole(role));
        }
        return webRoles;
    }

    public void delete(String roleName) {
//        for (Iterator it = m_roles.iterator(); it.hasNext();) {
//            WebRole role = (WebRole) it.next();
//            if (roleName.equals(role.getName())) {
//                it.remove();
//                return;
//            }
//        }
        throw new RuntimeException("WebRoleManager.delete is not yet implemented!");
    }

    public WebRole getRole(String roleName) {
        Role role = m_groupManager.getRole(roleName);
        return (role == null ? null : new ManagedRole(role));
    }

    public void save() {
        // TODO Auto-generated method stub
        
    }

    public void addRole(WebRole role) {
        if (getRole(role.getName()) != null)
            throw new IllegalArgumentException("Role with name "+role.getName()+" already exists.");

        throw new RuntimeException("WebRoleManager.addRole not yet implemented!");
    }

}
