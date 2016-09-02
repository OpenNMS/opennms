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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An uninstantiatable class that provides a servlet container-independent
 * interface to the authentication system and a list of useful constants.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="mailto:agalue@opennms.org">Alejandro Galue</A>
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
    public static final String ROLE_JMX = "ROLE_JMX";

    private static List<String> s_availableRoles = new ArrayList<String>();

    static {
        s_availableRoles.add(ROLE_USER);
        s_availableRoles.add(ROLE_ADMIN);
        s_availableRoles.add(ROLE_READONLY);
        s_availableRoles.add(ROLE_DASHBOARD);
        s_availableRoles.add(ROLE_RTC);
        s_availableRoles.add(ROLE_PROVISION);
        s_availableRoles.add(ROLE_REMOTING);
        s_availableRoles.add(ROLE_REST);
        s_availableRoles.add(ROLE_ASSET_EDITOR);
        s_availableRoles.add(ROLE_MOBILE);
        s_availableRoles.add(ROLE_JMX);
    }

    /** Private, empty constructor so this class cannot be instantiated. */
    private Authentication() {
    }

    public static List<String> getAvailableRoles() {
        return Collections.unmodifiableList(s_availableRoles);
    }

    public static boolean isValidRole(String role) {
        return s_availableRoles.contains(role);
    }

}
