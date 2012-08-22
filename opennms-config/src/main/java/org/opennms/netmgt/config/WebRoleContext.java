/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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


/**
 * <p>AppContext class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class WebRoleContext {
    private static WebRoleManagerImpl s_manager = null;
    
    /**
     * <p>init</p>
     *
     * @throws java.lang.Exception if any.
     */
    public static void init() throws Exception {
        GroupFactory.init();
        UserFactory.init();
    }
    
    private static WebRoleManagerImpl getManager() {
        if (s_manager == null) {
            s_manager = new WebRoleManagerImpl(GroupFactory.getInstance(), UserFactory.getInstance());
        }
        
        return s_manager;
    }
    
    /**
     * <p>getWebRoleManager</p>
     *
     * @return a {@link org.opennms.netmgt.config.WebRoleManager} object.
     */
    public static WebRoleManager getWebRoleManager() {
        return getManager();
    }

    /**
     * <p>getWebUserManager</p>
     *
     * @return a {@link org.opennms.netmgt.config.WebUserManager} object.
     */
    public static WebUserManager getWebUserManager() {
        return getManager();
    }
    
    /**
     * <p>getWebGroupManager</p>
     *
     * @return a {@link org.opennms.netmgt.config.WebGroupManager} object.
     */
    public static WebGroupManager getWebGroupManager() {
        return getManager();
    }

}
