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
// Modifications:
//
// 2007 Jul 24: Formatting. - dj@opennms.org
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

import org.opennms.netmgt.config.GroupFactory;
import org.opennms.netmgt.config.UserFactory;

public class AppContext {
    private static Manager s_manager = null;
    
    public static void init() throws Exception {
        GroupFactory.init();
        UserFactory.init();
    }
    
    private static Manager getManager() {
        if (s_manager == null) {
            s_manager = new Manager(GroupFactory.getInstance(), UserFactory.getInstance());
        }
        
        return s_manager;
    }
    
    public static WebRoleManager getWebRoleManager() {
        return getManager();
    }

    public static WebUserManager getWebUserManager() {
        return getManager();
    }
    
    public static WebGroupManager getWebGroupManager() {
        return getManager();
    }

}
