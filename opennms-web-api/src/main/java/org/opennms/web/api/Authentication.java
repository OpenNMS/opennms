/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.api;

import java.util.HashMap;
import java.util.Map;

/**
 * An uninstantiatable class that provides a servlet container-independent
 * interface to the authentication system and a list of useful constants.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 */
public final class Authentication extends Object {
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_READONLY = "ROLE_READONLY";
    public static final String ROLE_DASHBOARD = "ROLE_DASHBOARD";
    public static final String ROLE_RTC = "ROLE_RTC";
    public static final String ROLE_PROVISION = "ROLE_PROVISION";
    public static final String ROLE_REMOTING = "ROLE_REMOTING";
    public static final String ROLE_REST = "ROLE_REST";
    public static final String ROLE_ASSET_EDITOR = "ROLE_ASSET_EDITOR";
    public static final String ROLE_MOBILE = "ROLE_MOBILE";

    private static Map<String, String> s_oldToNewMap = new HashMap<String, String>();

    static {
        s_oldToNewMap.put("OpenNMS RTC Daemon", ROLE_RTC);
        s_oldToNewMap.put("OpenNMS Administrator", ROLE_ADMIN);
        s_oldToNewMap.put("OpenNMS Read-Only User", ROLE_READONLY);
        s_oldToNewMap.put("OpenNMS Dashboard User", ROLE_DASHBOARD);
        s_oldToNewMap.put("OpenNMS Provision User", ROLE_PROVISION);
        s_oldToNewMap.put("OpenNMS Remote Poller User", ROLE_REMOTING);
        s_oldToNewMap.put("OpenNMS REST User", ROLE_REST);
        s_oldToNewMap.put("OpenNMS Asset Editor", ROLE_ASSET_EDITOR);
        s_oldToNewMap.put("OpenNMS Mobile User", ROLE_MOBILE);

        // There is no entry for ROLE_USER, because all authenticated people are users
    }

    /** Private, empty constructor so this class cannot be instantiated. */
    private Authentication() {
    }

    /**
     * <p>getSpringSecuirtyRoleFromOldRoleName</p>
     *
     * @param oldRole a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getSpringSecurityRoleFromOldRoleName(String oldRole) {
        return s_oldToNewMap.get(oldRole);
    }

}
