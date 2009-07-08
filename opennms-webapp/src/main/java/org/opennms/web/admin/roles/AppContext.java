/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2005-2008 The OpenNMS Group, Inc.  All rights reserved.
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
